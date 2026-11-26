package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Recomendacion;
import com.ddaii.dto.RecomendacionDTO;
import com.ddaii.events.RecomendacionSolicitada;
import com.ddaii.exceptions.AccesoPremiumException;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.messaging.EventPublisher;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.RecomendacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Componente de IA: recomendaciones por contenido (docs/ia.md).
 *
 * Modelo: un puntaje por prenda candidata (0 a 1) que suma afinidad de categoría, género, marca
 * y precio. Dos estrategias (patrón Strategy) que elige RecomendacionFactory (patrón Factory):
 * - "contenido": las prendas más parecidas a la de entrada.
 * - "complementaria": arma un conjunto con las categorías que combinan (arriba, abajo y calzado).
 *
 * Solo recomienda productos disponibles y nunca la misma prenda.
 */
@Service
public class RecomendacionService {

    public static final int LIMITE_POR_DEFECTO = 5;
    public static final int LIMITE_MAXIMO = 20;

    private final ProductoRepository productoRepository;
    private final EventPublisher eventPublisher;
    private final RecomendacionRepository recomendacionRepository;
    private final InteresService interesService;

    public RecomendacionService(ProductoRepository productoRepository,
                                EventPublisher eventPublisher,
                                RecomendacionRepository recomendacionRepository,
                                InteresService interesService) {
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
        this.recomendacionRepository = recomendacionRepository;
        this.interesService = interesService;
    }

    @Transactional(readOnly = true)
    public List<RecomendacionDTO> recomendar(Long productoId, Integer limite) {
        return recomendar(productoId, limite, "contenido", null);
    }

    @Transactional(readOnly = true)
    public List<RecomendacionDTO> recomendar(Long productoId, Integer limite, String estrategia) {
        return recomendar(productoId, limite, estrategia, null);
    }

    /**
     * Igual que {@link #recomendar(Long, Integer, String)} pero, si el usuario es premium, suma un
     * bono a las prendas de las marcas/categorías que ya vio o marcó como favorito.
     */
    @Transactional(readOnly = true)
    public List<RecomendacionDTO> recomendar(Long productoId, Integer limite, String estrategia,
                                             Long usuarioId) {
        Producto entrada = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        List<Producto> candidatos = productoRepository.findByDisponibleTrue().stream()
                .filter(p -> !p.getId().equals(entrada.getId()))
                .collect(Collectors.toList());

        PerfilInteres intereses = interesService.perfil(usuarioId);
        return RecomendacionFactory.crear(estrategia)
                .recomendar(entrada, candidatos, normalizarLimite(limite), intereses);
    }

    static int normalizarLimite(Integer limite) {
        if (limite == null) return LIMITE_POR_DEFECTO;
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new IllegalArgumentException("'limite' tiene que estar entre 1 y " + LIMITE_MAXIMO);
        }
        return limite;
    }

    /**
     * Publica la solicitud y responde 202. El consumidor procesa y guarda el resultado.
     */
    @Transactional
    public String solicitarPorCola(Long productoId, Integer limite) {
        return solicitarPorCola(productoId, limite, null);
    }

    /** La solicitud por cola viaja con el usuario (si lo hay) para personalizar también ese camino. */
    @Transactional
    public String solicitarPorCola(Long productoId, Integer limite, Long usuarioId) {
        if (!productoRepository.existsById(productoId)) {
            throw new RecursoNoEncontradoException("Producto no encontrado: " + productoId);
        }
        String solicitudId = UUID.randomUUID().toString();
        eventPublisher.publicarRecomendacionSolicitada(
                solicitudId, productoId, normalizarLimite(limite), usuarioId);
        return solicitudId;
    }

    /** Registra una vista de producto para personalizar recomendaciones futuras (solo premium). */
    @Transactional
    public void registrarVista(String usuarioIdStr, Long productoId) {
        Long usuarioId = parsearUsuario(usuarioIdStr);
        if (usuarioId == null) {
            throw new AccesoPremiumException("La personalización es exclusiva para usuarios premium.");
        }
        interesService.registrarVista(usuarioId, productoId);
    }

    public static Long parsearUsuario(String usuarioIdStr) {
        if (usuarioIdStr == null || usuarioIdStr.isBlank()) return null;
        try {
            return Long.valueOf(usuarioIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Consumidor de RecomendacionSolicitada: calcula y persiste el resultado (idempotente).
     */
    @Transactional
    public void procesarSolicitud(RecomendacionSolicitada evento) {
        if (recomendacionRepository.findBySolicitudId(evento.getSolicitudId()).isPresent()) {
            return;
        }

        List<RecomendacionDTO> resultado = recomendar(
                evento.getProductoId(), evento.getLimite(), "contenido", evento.getUsuarioId());
        Recomendacion r = new Recomendacion(
                evento.getSolicitudId(), evento.getProductoId(), evento.getUsuarioId());
        r.setProductosRecomendados(resultado.stream().map(RecomendacionDTO::getId).toList());
        recomendacionRepository.save(r);
    }

    /** Resultado de una solicitud por cola, o 404 si el consumidor todavía no la procesó. */
    @Transactional(readOnly = true)
    public Map<String, Object> resultadoDeSolicitud(String solicitudId) {
        Recomendacion r = recomendacionRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La solicitud " + solicitudId + " todavía no se procesó (o no existe)"));
        Producto entrada = productoRepository.findById(r.getProductoId()).orElse(null);
        PerfilInteres intereses = interesService.perfil(r.getUsuarioId());
        Map<Long, Producto> porId = productoRepository.findAllById(r.getProductosRecomendados()).stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));
        List<RecomendacionDTO> productos = r.getProductosRecomendados().stream()
                .map(porId::get)
                .filter(Objects::nonNull)
                .map(p -> entrada != null
                        ? toDto(entrada, p, puntajeFinal(entrada, p, intereses))
                        : toDto(p, p, 0))
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("solicitudId", r.getSolicitudId());
        body.put("productoId", r.getProductoId());
        body.put("procesadaEn", r.getFecha());
        body.put("recomendaciones", productos);
        return body;
    }

    // ------------------------------------------------------------------ modelo

    /** Categorías que combinan con cada una para armar un conjunto. */
    static final Map<String, List<String>> COMPLEMENTARIAS = Map.of(
            "REMERA", List.of("PANTALON", "CALZADO"),
            "PANTALON", List.of("REMERA", "CALZADO"),
            "CALZADO", List.of("REMERA", "PANTALON"),
            "VESTIDO", List.of("CALZADO"));

    static boolean igual(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    /** 0,2 mismo género; 0,1 si uno es Unisex (le queda a cualquiera); 0 si no combinan. */
    static double afinidadGenero(String a, String b) {
        if (igual(a, b)) return 0.2;
        if (igual(a, "Unisex") || igual(b, "Unisex")) return 0.1;
        return 0.0;
    }

    /** 0,2 si el precio difiere hasta 30 %; 0,1 hasta 60 %; 0 si no. */
    static double afinidadPrecio(Double a, Double b) {
        if (a == null || b == null) return 0.0;
        double relativa = Math.abs(a - b) / Math.max(a, 1.0);
        if (relativa <= 0.3) return 0.2;
        if (relativa <= 0.6) return 0.1;
        return 0.0;
    }

    /**
     * Puntaje de parecido (0 a 1): categoría 0,4 + género 0,2 + marca 0,2 + precio 0,2.
     * Una prenda sin stock vale 0 (no se recomienda).
     */
    public static double puntaje(Producto entrada, Producto candidata) {
        if (Boolean.FALSE.equals(candidata.getDisponible())) return 0.0;
        double score = 0.0;
        if (igual(entrada.getCategoria(), candidata.getCategoria())) score += 0.4;
        score += afinidadGenero(entrada.getGenero(), candidata.getGenero());
        if (igual(entrada.getMarca(), candidata.getMarca())) score += 0.2;
        score += afinidadPrecio(entrada.getPrecio(), candidata.getPrecio());
        return redondear(score);
    }

    /**
     * Puntaje para completar un conjunto (0 a 1): la categoría ya es la complementaria, así que
     * pesan género 0,5 (que combine), marca 0,3 y precio parecido 0,2.
     */
    public static double puntajeConjunto(Producto entrada, Producto candidata) {
        if (Boolean.FALSE.equals(candidata.getDisponible())) return 0.0;
        double score = afinidadGenero(entrada.getGenero(), candidata.getGenero()) * 2.5;
        if (igual(entrada.getMarca(), candidata.getMarca())) score += 0.3;
        score += afinidadPrecio(entrada.getPrecio(), candidata.getPrecio());
        return redondear(score);
    }

    /**
     * Puntaje final con personalización. El bono no cambia el modelo base (0 a 1) y se limita a 1,
     * pero se usa además como desempate para que dos prendas con el mismo puntaje respeten el interés.
     */
    static double puntajeFinal(Producto entrada, Producto candidata, PerfilInteres intereses) {
        double bono = intereses == null ? 0.0 : intereses.bono(candidata);
        return redondear(Math.min(1.0, puntaje(entrada, candidata) + bono));
    }

    static double puntajeConjuntoFinal(Producto entrada, Producto candidata, PerfilInteres intereses) {
        double bono = intereses == null ? 0.0 : intereses.bono(candidata);
        return redondear(Math.min(1.0, puntajeConjunto(entrada, candidata) + bono));
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Desempate: mayor puntaje, después el precio más cercano, después el id (orden estable). */
    static Comparator<Producto> orden(Producto entrada, java.util.function.ToDoubleFunction<Producto> score) {
        return orden(entrada, score, PerfilInteres.vacio());
    }

    static Comparator<Producto> orden(Producto entrada, java.util.function.ToDoubleFunction<Producto> score,
                                      PerfilInteres intereses) {
        double precio = entrada.getPrecio() != null ? entrada.getPrecio() : 0.0;
        Comparator<Producto> comparador = Comparator.comparingDouble(score).reversed();
        if (intereses != null && intereses.tieneInteres()) {
            // Segundo criterio: aunque ambas prendas lleguen a 1.0, gana la de la marca/categoría
            // que el usuario premium ya miró o marcó como favorito.
            comparador = comparador.thenComparing(
                    Comparator.comparingDouble(intereses::bono).reversed());
        }
        return comparador
                .thenComparingDouble(p -> p.getPrecio() != null ? Math.abs(p.getPrecio() - precio) : Double.MAX_VALUE)
                .thenComparing(Producto::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public interface EstrategiaRecomendacion {
        /** Las pruebas y el uso anónimo siguen funcionando sin perfil de interés. */
        default List<RecomendacionDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite) {
            return recomendar(entrada, candidatos, limite, PerfilInteres.vacio());
        }

        List<RecomendacionDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite,
                                          PerfilInteres intereses);
    }

    /** Nombre para comparar variantes (la misma prenda en otro color tiene otro id pero el mismo nombre). */
    static String claveNombre(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            return "id:" + p.getId() + ":" + System.identityHashCode(p); // sin nombre: no se compara
        }
        return p.getNombre().trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /** Saca variantes repetidas y la misma prenda de entrada, respetando el orden. */
    static List<Producto> sinRepetidos(Producto entrada, List<Producto> ordenados) {
        java.util.Set<String> vistos = new java.util.HashSet<>();
        vistos.add(claveNombre(entrada));
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : ordenados) {
            if (vistos.add(claveNombre(p))) resultado.add(p);
        }
        return resultado;
    }

    /**
     * Las prendas más parecidas a la de entrada. El género tiene que combinar (mismo o Unisex):
     * a una calza de mujer no se le recomienda un short de hombre aunque sea de la misma marca.
     */
    public static class RecomendacionPorContenido implements EstrategiaRecomendacion {
        @Override
        public List<RecomendacionDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite,
                                                  PerfilInteres intereses) {
            PerfilInteres perfil = intereses == null ? PerfilInteres.vacio() : intereses;
            List<Producto> ordenados = candidatos.stream()
                    .filter(p -> !Boolean.FALSE.equals(p.getDisponible()))
                    // Un producto del interés del usuario entra aunque no se parezca a la prenda actual.
                    .filter(p -> puntaje(entrada, p) > 0 || perfil.bono(p) > 0)
                    .filter(p -> entrada.getGenero() == null || afinidadGenero(entrada.getGenero(), p.getGenero()) > 0)
                    .sorted(orden(entrada, p -> puntajeFinal(entrada, p, perfil), perfil))
                    .toList();
            return sinRepetidos(entrada, ordenados).stream()
                    .limit(limite)
                    .map(p -> toDto(entrada, p, puntajeFinal(entrada, p, perfil)))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Arma un conjunto: la mejor prenda de cada categoría que combina (por ejemplo, para una remera:
     * un pantalón y un calzado), después la segunda de cada una, y así hasta el límite.
     */
    public static class RecomendacionComplementaria implements EstrategiaRecomendacion {
        @Override
        public List<RecomendacionDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite,
                                                  PerfilInteres intereses) {
            PerfilInteres perfil = intereses == null ? PerfilInteres.vacio() : intereses;
            String categoria = entrada.getCategoria() != null ? entrada.getCategoria().toUpperCase() : "";
            List<String> combinan = COMPLEMENTARIAS.getOrDefault(categoria,
                    List.of("REMERA", "PANTALON", "CALZADO").stream().filter(c -> !c.equals(categoria)).toList());

            List<List<Producto>> porCategoria = new ArrayList<>();
            for (String c : combinan) {
                porCategoria.add(sinRepetidos(entrada, candidatos.stream()
                        .filter(p -> !Boolean.FALSE.equals(p.getDisponible()))
                        .filter(p -> igual(p.getCategoria(), c))
                        .filter(p -> afinidadGenero(entrada.getGenero(), p.getGenero()) > 0)
                        .sorted(orden(entrada, p -> puntajeConjuntoFinal(entrada, p, perfil), perfil))
                        .toList()));
            }

            List<RecomendacionDTO> resultado = new ArrayList<>();
            for (int i = 0; resultado.size() < limite; i++) {
                boolean agrego = false;
                for (List<Producto> lista : porCategoria) {
                    if (i < lista.size() && resultado.size() < limite) {
                        Producto p = lista.get(i);
                        resultado.add(toDto(entrada, p, puntajeConjuntoFinal(entrada, p, perfil)));
                        agrego = true;
                    }
                }
                if (!agrego) break;
            }
            return resultado;
        }
    }

    public static class RecomendacionFactory {
        public static EstrategiaRecomendacion crear(String tipo) {
            if (tipo == null || tipo.isBlank() || "contenido".equalsIgnoreCase(tipo)) {
                return new RecomendacionPorContenido();
            }
            if ("complementaria".equalsIgnoreCase(tipo)) {
                return new RecomendacionComplementaria();
            }
            throw new IllegalArgumentException("Estrategia desconocida: " + tipo + " (usar contenido o complementaria)");
        }
    }

    private static RecomendacionDTO toDto(Producto entrada, Producto p, double puntaje) {
        RecomendacionDTO dto = new RecomendacionDTO(p.getId(), p.getNombre(), p.getCategoria(),
                p.getPrecio(), p.getImagenUrl(), puntaje);
        dto.setGenero(p.getGenero());
        dto.setMarca(p.getMarca());
        dto.setTienda(ProductoService.toTiendaDto(p.getTienda()));
        return dto;
    }
}
