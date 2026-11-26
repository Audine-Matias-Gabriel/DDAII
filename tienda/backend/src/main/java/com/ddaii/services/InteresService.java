package com.ddaii.services;

import com.ddaii.domain.InteresProducto;
import com.ddaii.domain.Producto;
import com.ddaii.domain.Usuario;
import com.ddaii.exceptions.AccesoPremiumException;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.repositories.InteresProductoRepository;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registra las señales de interés (vistas y favoritos) y arma el perfil que usa el recomendador.
 * Solo acumula historial de usuarios premium: para los demás el recomendador sigue siendo genérico.
 */
@Service
@Transactional(readOnly = true)
public class InteresService {

    private static final int PESO_VISTA = 1;
    private static final int PESO_FAVORITO = 3;
    private static final int PESO_MAXIMO = 20;
    private static final int PESO_PARA_SATURAR = 5;

    private final InteresProductoRepository interesRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final int diasVigencia;
    private final int maxRegistros;
    private final double bonoMarca;
    private final double bonoCategoria;

    public InteresService(InteresProductoRepository interesRepository,
                          UsuarioRepository usuarioRepository,
                          ProductoRepository productoRepository,
                          @Value("${recomendaciones.intereses.dias-vigencia:30}") int diasVigencia,
                          @Value("${recomendaciones.intereses.max-registros:50}") int maxRegistros,
                          @Value("${recomendaciones.intereses.bono-marca:0.15}") double bonoMarca,
                          @Value("${recomendaciones.intereses.bono-categoria:0.10}") double bonoCategoria) {
        this.interesRepository = interesRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.diasVigencia = Math.max(1, diasVigencia);
        this.maxRegistros = Math.max(1, maxRegistros);
        this.bonoMarca = Math.max(0.0, bonoMarca);
        this.bonoCategoria = Math.max(0.0, bonoCategoria);
    }

    /** Cada vista suma una señal; la misma prenda vista varias veces pesa más (hasta un tope). */
    @Transactional
    public void registrarVista(Long usuarioId, Long productoId) {
        registrar(usuarioId, productoId, InteresProducto.Tipo.VISTA, PESO_VISTA, true);
    }

    /**
     * Un favorito es una señal más fuerte que una vista. Se llama desde FavoritoService cuando el
     * premium marca la prenda; si el usuario no es premium, simplemente no se acumula historial.
     */
    @Transactional
    public void registrarFavorito(Long usuarioId, Long productoId) {
        registrar(usuarioId, productoId, InteresProducto.Tipo.FAVORITO, PESO_FAVORITO, false);
    }

    /** Perfil de marcas/categorías para personalizar. Usuario null o no premium => perfil vacío. */
    public PerfilInteres perfil(Long usuarioId) {
        if (usuarioId == null) return PerfilInteres.vacio();
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getPremium())) {
            return PerfilInteres.vacio();
        }

        LocalDateTime desde = LocalDateTime.now().minusDays(diasVigencia);
        Map<String, Double> marcas = new HashMap<>();
        Map<String, Double> categorias = new HashMap<>();
        interesRepository.findByUsuarioIdAndFechaAfterOrderByFechaDesc(usuarioId, desde).stream()
                .limit(maxRegistros)
                .filter(i -> i.getProducto() != null)
                .forEach(i -> {
                    double peso = Math.min(1.0, Math.max(0, i.getPeso()) / (double) PESO_PARA_SATURAR);
                    acumular(marcas, i.getProducto().getMarca(), peso);
                    acumular(categorias, i.getProducto().getCategoria(), peso);
                });
        return new PerfilInteres(marcas, categorias, bonoMarca, bonoCategoria);
    }

    private void registrar(Long usuarioId, Long productoId, InteresProducto.Tipo tipo,
                           int peso, boolean exigirPremium) {
        if (usuarioId == null) {
            throw new AccesoPremiumException("La personalización es exclusiva para usuarios premium.");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));
        if (!Boolean.TRUE.equals(usuario.getPremium())) {
            if (exigirPremium) {
                throw new AccesoPremiumException(
                        "El historial de intereses es exclusivo para usuarios premium.");
            }
            return;
        }
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        Optional<InteresProducto> anterior =
                interesRepository.findByUsuarioIdAndProductoIdAndTipo(usuarioId, productoId, tipo);
        if (anterior.isPresent()) {
            InteresProducto actual = anterior.get();
            actual.setPeso(Math.min(PESO_MAXIMO, actual.getPeso() + peso));
            actual.setFecha(LocalDateTime.now());
            interesRepository.save(actual);
        } else {
            interesRepository.save(new InteresProducto(usuario, producto, tipo, peso));
        }
    }

    private void acumular(Map<String, Double> destino, String valor, double peso) {
        String clave = PerfilInteres.clave(valor);
        if (clave == null || clave.isEmpty()) return;
        destino.merge(clave, peso, Double::sum);
    }
}
