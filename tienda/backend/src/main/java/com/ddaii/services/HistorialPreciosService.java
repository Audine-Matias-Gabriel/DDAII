package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.ProductoSnapshot;
import com.ddaii.dto.PrecioHistorialDTO;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.ProductoSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Guarda el precio diario de cada producto cuando llega un evento del scraper
 * y expone la serie temporal para el gráfico premium.
 */
@Service
@Transactional(readOnly = true)
public class HistorialPreciosService {

    private final ProductoSnapshotRepository snapshotRepository;
    private final ProductoRepository productoRepository;

    public HistorialPreciosService(ProductoSnapshotRepository snapshotRepository,
                                   ProductoRepository productoRepository) {
        this.snapshotRepository = snapshotRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Un snapshot por producto y día: si el scraper corre varias veces el mismo
     * día se actualiza el precio de ese día en lugar de agregar filas.
     */
    @Transactional
    public void registrarSiCorresponde(Producto producto) {
        if (producto == null || producto.getId() == null || producto.getPrecio() == null) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        ProductoSnapshot snapshot = snapshotRepository
                .findByProductoIdAndFecha(producto.getId(), hoy)
                .orElseGet(() -> new ProductoSnapshot(producto, hoy, producto.getPrecio()));
        snapshot.setPrecio(producto.getPrecio());
        snapshotRepository.save(snapshot);
    }

    public List<PrecioHistorialDTO> historial(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new RecursoNoEncontradoException("Producto no encontrado: " + productoId);
        }
        return snapshotRepository.findByProductoIdOrderByFechaAsc(productoId).stream()
                .map(s -> new PrecioHistorialDTO(s.getFecha(), s.getPrecio()))
                .toList();
    }
}
