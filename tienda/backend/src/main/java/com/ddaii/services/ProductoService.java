package com.ddaii.services;

import com.ddaii.domain.Genero;
import com.ddaii.domain.Producto;
import com.ddaii.repositories.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(
            ProductoRepository productoRepository) {

        this.productoRepository = productoRepository;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Producto no encontrado: " + id
                        )
                );
    }

    public List<Producto> buscarPorTienda(Long tiendaId) {

        return productoRepository.findByTiendaId(tiendaId);
    }

    public Producto actualizarStock(
            Long id,
            Genero genero,
            String talle,
            Integer cantidad) {

        Producto producto = obtenerPorId(id);

        producto.actualizarStock(genero, talle, cantidad);

        return productoRepository.save(producto);
    }
}