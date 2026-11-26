package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Usuario;
import com.ddaii.dto.ResultadoVisualDTO;
import com.ddaii.exceptions.AccesoPremiumException;
import com.ddaii.exceptions.SinCoincidenciasException;
import com.ddaii.integrations.XimilarClient;
import com.ddaii.integrations.XimilarFacade;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Búsqueda visual premium: una foto subida por el usuario se envía a la colección de
 * product-similarity de Ximilar y se devuelven los productos del catálogo más parecidos.
 *
 * Ximilar siempre devuelve los k vecinos más cercanos, incluso si la foto no es una prenda.
 * Por eso se descartan los resultados cuya distancia supera {@code maxDistancia}. Si después
 * de {@code maxIntentos} búsquedas no hay ninguno válido se corta con error: cada intento
 * consume créditos y no tiene sentido seguir probando.
 */
@Service
@Transactional(readOnly = true)
public class BusquedaVisualService {

    private final XimilarFacade ximilarFacade;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoService productoService;
    private final int maxIntentos;
    private final int resultadosPorIntento;
    private final double maxDistancia;
    private final long maxImagenBytes;

    public BusquedaVisualService(XimilarFacade ximilarFacade,
                                 ProductoRepository productoRepository,
                                 UsuarioRepository usuarioRepository,
                                 ProductoService productoService,
                                 @Value("${ximilar.visual-search.max-intentos:3}") int maxIntentos,
                                 @Value("${ximilar.visual-search.resultados-por-intento:5}") int resultadosPorIntento,
                                 @Value("${ximilar.visual-search.max-distancia:1.0}") double maxDistancia,
                                 @Value("${ximilar.visual-search.max-imagen-bytes:5242880}") long maxImagenBytes) {
        this.ximilarFacade = ximilarFacade;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoService = productoService;
        this.maxIntentos = maxIntentos;
        this.resultadosPorIntento = resultadosPorIntento;
        this.maxDistancia = maxDistancia;
        this.maxImagenBytes = maxImagenBytes;
    }

    public List<ResultadoVisualDTO> buscar(String usuarioIdStr, MultipartFile imagen, int limite) {
        verificarPremium(usuarioIdStr);
        byte[] bytes = validarImagen(imagen);
        int tope = Math.max(1, Math.min(limite, 20));
        String mime = imagen.getContentType();

        List<XimilarClient.Coincidencia> validas = List.of();
        int intentos = 0;
        for (int intento = 1; intento <= Math.max(1, maxIntentos); intento++) {
            // Cada intento pide más vecinos: si el filtro de catálogo deja pocos registros,
            // con un k mayor puede aparecer alguno. Se corta en cuanto hay una coincidencia.
            int k = Math.min(30, Math.max(1, Math.min(resultadosPorIntento, 30) * intento));
            List<XimilarClient.Coincidencia> encontradas = ximilarFacade.buscarPorImagen(bytes, mime, k);
            intentos++;
            validas = encontradas.stream()
                    .filter(c -> c.productoId() != null)
                    .filter(c -> esParecido(c.distancia()))
                    .toList();
            if (!validas.isEmpty()) break;
        }

        if (validas.isEmpty()) {
            throw new SinCoincidenciasException(
                    "La imagen no se corresponde con ninguna prenda del catálogo. "
                            + "Se probaron " + intentos + " búsquedas y se detuvo para no gastar más créditos.");
        }

        List<Long> ids = validas.stream().map(XimilarClient.Coincidencia::productoId).distinct().limit(tope).toList();
        Map<Long, Producto> porId = productoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        List<ResultadoVisualDTO> resultado = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        for (XimilarClient.Coincidencia c : validas) {
            if (resultado.size() >= tope) break;
            Producto producto = porId.get(c.productoId());
            if (producto == null || !vistos.add(c.productoId())) continue;
            resultado.add(new ResultadoVisualDTO(productoService.toDto(producto), c.distancia()));
        }
        if (resultado.isEmpty()) {
            throw new SinCoincidenciasException(
                    "La imagen no se corresponde con ningún producto disponible del catálogo.");
        }
        return resultado;
    }

    /** Ximilar no informa distancia en algunas respuestas: en ese caso se acepta el resultado. */
    private boolean esParecido(Double distancia) {
        return distancia == null || maxDistancia <= 0 || distancia <= maxDistancia;
    }

    private void verificarPremium(String usuarioIdStr) {
        Long usuarioId = null;
        try {
            usuarioId = usuarioIdStr == null ? null : Long.valueOf(usuarioIdStr);
        } catch (NumberFormatException ignored) {
            // Un principal inválido se trata como falta de permiso, no como error 500.
        }
        if (usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            if (usuario != null && Boolean.TRUE.equals(usuario.getPremium())) return;
        }
        throw new AccesoPremiumException("La búsqueda visual es exclusiva para usuarios premium.");
    }

    private byte[] validarImagen(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("Debe subir una imagen en el campo 'imagen'.");
        }
        String mime = imagen.getContentType();
        if (mime == null || !mime.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen (PNG, JPG o TIFF).");
        }
        if (imagen.getSize() > maxImagenBytes) {
            throw new IllegalArgumentException("La imagen supera el tamaño máximo de "
                    + (maxImagenBytes / 1024 / 1024) + " MB.");
        }
        try {
            return imagen.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer la imagen subida.");
        }
    }
}
