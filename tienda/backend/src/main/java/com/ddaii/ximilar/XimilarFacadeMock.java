package com.ddaii.ximilar;

import com.ddaii.domain.Producto;
import com.ddaii.repositories.ProductoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("mock")
public class XimilarFacadeMock implements XimilarFacade {

    private final ProductoRepository productoRepository;

    public XimilarFacadeMock(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void indexarProducto(Producto producto) {
        // no-op: el mock no habla con Ximilar
    }

    @Override
    public List<String> buscarSimilares(Producto productoOriginal, int limite) {
        Long tiendaOriginal = productoOriginal.getTiendaId();

        return productoRepository.findAll().stream()
                .filter(p -> p.getTiendaId() != null
                        && !p.getTiendaId().equals(tiendaOriginal))
                .limit(limite)
                .map(p -> String.valueOf(p.getId()))
                .toList();
    }
}