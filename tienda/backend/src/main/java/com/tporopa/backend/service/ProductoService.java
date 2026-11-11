package com.tporopa.backend.service;

import com.tporopa.backend.dto.ProductoDTO;
import com.tporopa.backend.event.ProductoScrapeado;
import com.tporopa.backend.model.Producto;
import com.tporopa.backend.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de catálogo (P2).
 * Día 3: GET /api/productos con filtros y paginación, GET /api/productos/{id}, GET /api/tiendas, seed.
 * Día 5: consumidor de ProductoScrapeado (upsert por tienda + idExterno).
 */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Page<ProductoDTO> filtrar(String categoria, String tienda, String genero,
                                     Double precioMin, Double precioMax, String texto,
                                     Pageable pageable) {
        return productoRepository.filtrar(categoria, tienda, genero, precioMin, precioMax, texto, pageable)
                .map(this::toDto);
    }

    public ProductoDTO obtenerPorId(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        return toDto(p);
    }

    public List<ProductoDTO> disponibles() {
        return productoRepository.findByDisponibleTrue().stream().map(this::toDto).toList();
    }

    public List<ProductoDTO> porCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria).stream().map(this::toDto).toList();
    }

    /**
     * Consumidor del evento ProductoScrapeado (P3 publica, P2 consume, Día 5).
     * Crea o actualiza el producto por tienda + idExterno.
     */
    @Transactional
    public void upsertDesdeScrapeado(ProductoScrapeado evento) {
        String tienda = evento.getTienda();
        String idExterno = evento.getIdExterno();

        Producto existente = productoRepository.findByTienda(tienda).stream()
                .filter(p -> idExterno.equals(p.getIdExterno()))
                .findFirst()
                .orElse(null);

        Producto p;
        if (existente != null) {
            p = existente;
        } else {
            p = new Producto();
            p.setTienda(tienda);
            p.setIdExterno(idExterno);
        }

        p.setNombre(evento.getNombre());
        p.setDescripcion(evento.getDescripcion());
        p.setPrecio(evento.getPrecio());
        p.setCategoria(evento.getCategoria());
        p.setGenero(evento.getGenero());
        p.setMarca(evento.getMarca());
        p.setUrlOriginal(evento.getUrlOriginal());
        p.setImagenUrl(evento.getImagenUrl());
        p.setDisponible(evento.getDisponible());
        p.setTallesDisponibles(evento.getTallesDisponibles() != null ? evento.getTallesDisponibles() : List.of());

        productoRepository.save(p);
    }

    private ProductoDTO toDto(Producto p) {
        return new ProductoDTO(
                p.getId(), p.getIdExterno(), p.getTienda(), p.getNombre(),
                p.getDescripcion(), p.getPrecio(), p.getCategoria(), p.getGenero(),
                p.getMarca(), p.getUrlOriginal(), p.getImagenUrl(),
                p.getDisponible(), p.getTallesDisponibles()
        );
    }
}
