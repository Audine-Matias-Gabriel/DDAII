package com.ddaii.integrations;

import com.ddaii.domain.Producto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Fachada de Ximilar (patrón Facade): el resto del backend habla de productos,
 * no de registros, _id ni filtros de la API.
 *
 * Con XIMILAR_MOCK=true no llama a la API ni gasta créditos: subir "anda" y la
 * búsqueda devuelve vacío, así SimilaresService usa su respaldo local.
 *
 * Plan gratuito: 1.000 créditos por mes. Subir una imagen = 0,25; buscar por _id = 0,25.
 * No se usa tagging (cuesta 15 créditos por foto).
 */
@Service
public class XimilarFacade {

    private final XimilarClient ximilarClient;
    private final boolean mockMode;

    public XimilarFacade(XimilarClient ximilarClient,
                         @Value("${ximilar.mock:false}") boolean mockMode) {
        this.ximilarClient = ximilarClient;
        this.mockMode = mockMode;
    }

    /** Hay credenciales y no está en modo mock: se usa la API real. */
    public boolean activo() {
        return !mockMode && ximilarClient.configurado();
    }

    public boolean modoMock() {
        return mockMode;
    }

    /** IDs de productos de otra tienda parecidos al dado, del más parecido al menos. */
    public List<Long> buscarSimilares(Producto producto, int limite) {
        if (!activo()) return List.of();
        Long tiendaId = producto.getTienda() != null ? producto.getTienda().getId() : null;
        return ximilarClient.buscarSimilares(
                producto.getId(), producto.getImagenUrl(), producto.indexadoEnXimilar(), tiendaId, limite);
    }

    /**
     * Sube hasta 10 productos. Los que ya estaban con otra imagen se borran antes.
     * Devuelve los ids que quedaron en la colección.
     */
    public Set<Long> subir(List<Producto> productos) {
        if (!activo()) {
            // En mock o sin credenciales no se marca nada como subido.
            return Set.of();
        }
        List<Long> conImagenVieja = productos.stream()
                .filter(p -> p.getXimilarImagenUrl() != null)
                .map(Producto::getId)
                .toList();
        if (!conImagenVieja.isEmpty()) {
            ximilarClient.eliminar(conImagenVieja);
        }
        List<XimilarClient.Registro> registros = productos.stream()
                .map(p -> new XimilarClient.Registro(
                        p.getId(), p.getImagenUrl(), p.getTienda() != null ? p.getTienda().getId() : null))
                .toList();
        return ximilarClient.insertar(registros).subidos();
    }
}
