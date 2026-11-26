package com.ddaii.repositories;

import com.ddaii.domain.ProductoSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductoSnapshotRepository extends JpaRepository<ProductoSnapshot, Long> {

    List<ProductoSnapshot> findByProductoIdOrderByFechaAsc(Long productoId);

    Optional<ProductoSnapshot> findByProductoIdAndFecha(Long productoId, LocalDate fecha);
}
