package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.dto.ProductoSimilarDTO;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.integrations.XimilarFacade;
import com.ddaii.repositories.ProductoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Prendas similares de otras tiendas mediante Ximilar (docs/PLAN-INTEGRACION.md 2.4 y 6).
 */
@Service
@Transactional(readOnly = true)
public class SimilaresService {

    private final ProductoRepository productoRepository;
    private final XimilarFacade ximilarFacade;

    public SimilaresService(ProductoRepository productoRepository, XimilarFacade ximilarFacade) {
        this.productoRepository = productoRepository;
        this.ximilarFacade = ximilarFacade;
    }

    public List<ProductoSimilarDTO> buscarSimilares(Long productoId, int limite) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        Long tiendaId = producto.getTienda() != null ? producto.getTienda().getId() : null;
        List<Long> idsXimilar = ximilarFacade.buscarSimilares(producto.getImagenUrl(), limite);

        List<Producto> candidatos = idsXimilar != null && !idsXimilar.isEmpty()
                ? productoRepository.findByIdInAndTienda_IdNot(idsXimilar, tiendaId)
                : List.of();

        if (candidatos.isEmpty()) {
            // Perfil mock / fallback: catálogo local de otras tiendas.
            candidatos = productoRepository.findByTienda_IdNotAndIdNot(
                    tiendaId, producto.getId(), PageRequest.of(0, limite));
        }

        return candidatos.stream()
                .limit(limite)
                .map(this::toDto)
                .toList();
    }

    private ProductoSimilarDTO toDto(Producto p) {
        return new ProductoSimilarDTO(
                p.getId(), p.getNombre(), ProductoService.toTiendaDto(p.getTienda()),
                p.getImagenUrl(), p.getUrlOriginal(), p.getPrecio()
        );
    }
}
