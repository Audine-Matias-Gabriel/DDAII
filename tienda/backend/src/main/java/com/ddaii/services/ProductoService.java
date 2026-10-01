package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.repositories.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
<<<<<<< HEAD

=======
>>>>>>> luciano/backend_2&4
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(
            ProductoRepository productoRepository) {

        this.productoRepository = productoRepository;
    }

<<<<<<< HEAD
    public List<Producto> obtenerTodos() {
=======
    public List<Producto> listarProductos() {
>>>>>>> luciano/backend_2&4

        return productoRepository.findAll();
    }

<<<<<<< HEAD
    public Producto obtenerPorId(Long id) {
=======
    public Producto obtenerProducto(Long id) {
>>>>>>> luciano/backend_2&4

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
            Integer cantidad) {

<<<<<<< HEAD
        Producto producto = obtenerPorId(id);
=======
        Producto producto = obtenerProducto(id);
>>>>>>> luciano/backend_2&4

        producto.actualizarStock(cantidad);

        return productoRepository.save(producto);
    }
}