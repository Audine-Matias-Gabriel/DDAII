package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Tienda;
import com.ddaii.dto.ProductoDTO;
import com.ddaii.dto.TiendaDTO;
import com.ddaii.events.ProductoScrapeado;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.TiendaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de catálogo (docs/PLAN-INTEGRACION.md 2.1 y 4.2).
 */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final TiendaRepository tiendaRepository;

    public ProductoService(ProductoRepository productoRepository, TiendaRepository tiendaRepository) {
        this.productoRepository = productoRepository;
        this.tiendaRepository = tiendaRepository;
    }

    public Page<ProductoDTO> filtrar(String categoria, Long tiendaId, String genero,
                                     Double precioMin, Double precioMax, String texto,
                                     Pageable pageable) {
        return productoRepository.filtrar(categoria, tiendaId, genero, precioMin, precioMax, texto, pageable)
                .map(this::toDto);
    }

    public ProductoDTO obtenerPorId(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
        return toDto(p);
    }

    public List<ProductoDTO> disponibles() {
        return productoRepository.findByDisponibleTrue().stream().map(this::toDto).toList();
    }

    public List<ProductoDTO> porCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria).stream().map(this::toDto).toList();
    }

    /**
     * Consumidor del evento ProductoScrapeado: upsert por tienda + idExterno.
     */
    @Transactional
    public Producto upsertDesdeScrapeado(ProductoScrapeado evento) {
        Tienda tienda = resolverTienda(evento);
        String idExterno = evento.getIdExterno();

        Producto p = productoRepository.findByTienda_IdAndIdExterno(tienda.getId(), idExterno)
                .orElseGet(() -> {
                    Producto nuevo = new Producto();
                    nuevo.setTienda(tienda);
                    nuevo.setIdExterno(idExterno);
                    return nuevo;
                });

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

        return productoRepository.save(p);
    }

    private Tienda resolverTienda(ProductoScrapeado evento) {
        // El nombre es la clave estable; el id puede variar según el seed/orden de inserción.
        if (evento.getTienda() != null) {
            Tienda porNombre = tiendaRepository.findByNombre(evento.getTienda()).orElse(null);
            if (porNombre != null) {
                return porNombre;
            }
            if (evento.getTiendaId() != null) {
                Tienda porId = tiendaRepository.findById(evento.getTiendaId()).orElse(null);
                if (porId != null) {
                    return porId;
                }
            }
            return tiendaRepository.save(
                    new Tienda(evento.getTienda(), "https://" + evento.getTienda() + ".com.ar", true));
        }
        if (evento.getTiendaId() != null) {
            return tiendaRepository.findById(evento.getTiendaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Tienda no encontrada: " + evento.getTiendaId()));
        }
        throw new RecursoNoEncontradoException("Tienda no informada en ProductoScrapeado");
    }

    public ProductoDTO toDto(Producto p) {
        return new ProductoDTO(
                p.getId(), p.getIdExterno(), toTiendaDto(p.getTienda()), p.getNombre(),
                p.getDescripcion(), p.getPrecio(), p.getCategoria(), p.getGenero(),
                p.getMarca(), p.getUrlOriginal(), p.getImagenUrl(),
                p.getDisponible(), p.getTallesDisponibles()
        );
    }

    public static TiendaDTO toTiendaDto(Tienda t) {
        if (t == null) {
            return null;
        }
        return new TiendaDTO(t.getId(), t.getNombre(), t.getUrlBase(), t.getActiva());
    }
}
