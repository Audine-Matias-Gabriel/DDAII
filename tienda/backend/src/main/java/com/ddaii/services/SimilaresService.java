package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.dto.ProductoSimilarDTO;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.integrations.XimilarFacade;
import com.ddaii.repositories.ProductoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Prendas similares de otras tiendas mediante Ximilar (docs/PLAN-INTEGRACION.md 2.4 y 6).
 *
 * Con Ximilar activo devuelve los productos visualmente más parecidos de otra tienda, en el
 * orden de Ximilar ({@code origen = "ximilar"}). Si Ximilar no está configurado, está en mock o
 * no encuentra nada, usa el respaldo: productos de otra tienda de la misma categoría
 * ({@code origen = "catalogo"}).
 */
@Service
@Transactional(readOnly = true)
public class SimilaresService {

    public static final String ORIGEN_XIMILAR = "ximilar";
    public static final String ORIGEN_CATALOGO = "catalogo";

    private final ProductoRepository productoRepository;
    private final XimilarFacade ximilarFacade;

    public SimilaresService(ProductoRepository productoRepository, XimilarFacade ximilarFacade) {
        this.productoRepository = productoRepository;
        this.ximilarFacade = ximilarFacade;
    }

    public List<ProductoSimilarDTO> buscarSimilares(Long productoId, int limite) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));
        int tope = Math.max(1, Math.min(limite, 20));
        Long tiendaId = producto.getTienda() != null ? producto.getTienda().getId() : null;

        List<Producto> deXimilar = enOrden(ximilarFacade.buscarSimilares(producto, tope)).stream()
                .filter(p -> !p.getId().equals(producto.getId()))
                .filter(p -> tiendaId == null || p.getTienda() == null || !tiendaId.equals(p.getTienda().getId()))
                .limit(tope)
                .toList();
        if (!deXimilar.isEmpty()) {
            return deXimilar.stream().map(p -> toDto(p, ORIGEN_XIMILAR)).toList();
        }

        return respaldo(producto, tiendaId, tope).stream().map(p -> toDto(p, ORIGEN_CATALOGO)).toList();
    }

    /** Productos de los ids dados, en ese mismo orden (findAllById no lo respeta). */
    private List<Producto> enOrden(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        Map<Long, Producto> porId = productoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
        return ids.stream().map(porId::get).filter(Objects::nonNull).toList();
    }

    /** Sin Ximilar: misma categoría en otra tienda y, si no alcanza, cualquier producto de otra tienda. */
    private List<Producto> respaldo(Producto producto, Long tiendaId, int tope) {
        List<Producto> candidatos = new ArrayList<>(productoRepository.findByTienda_IdNotAndIdNot(
                tiendaId, producto.getId(), PageRequest.of(0, 200)));
        List<Producto> resultado = new ArrayList<>(candidatos.stream()
                .filter(p -> Objects.equals(p.getCategoria(), producto.getCategoria()))
                .limit(tope)
                .toList());
        for (Producto p : candidatos) {
            if (resultado.size() >= tope) break;
            if (!resultado.contains(p)) resultado.add(p);
        }
        return resultado;
    }

    private ProductoSimilarDTO toDto(Producto p, String origen) {
        ProductoSimilarDTO dto = new ProductoSimilarDTO(
                p.getId(), p.getNombre(), ProductoService.toTiendaDto(p.getTienda()),
                p.getImagenUrl(), p.getUrlOriginal(), p.getPrecio()
        );
        dto.setOrigen(origen);
        return dto;
    }
}
