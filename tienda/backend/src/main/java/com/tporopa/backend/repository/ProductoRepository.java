package com.tporopa.backend.repository;

import com.tporopa.backend.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de productos (Día 2-3, P2).
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByTienda(String tienda);

    List<Producto> findByDisponibleTrue();

    List<Producto> findByCategoria(String categoria);

    @Query("SELECT p FROM Producto p " +
           "WHERE (:categoria IS NULL OR p.categoria = :categoria) " +
           "AND (:tienda IS NULL OR p.tienda = :tienda) " +
           "AND (:genero IS NULL OR p.genero = :genero) " +
           "AND (:precioMin IS NULL OR p.precio >= :precioMin) " +
           "AND (:precioMax IS NULL OR p.precio <= :precioMax) " +
           "AND (:texto IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "AND p.disponible = true")
    Page<Producto> filtrar(@Param("categoria") String categoria,
                           @Param("tienda") String tienda,
                           @Param("genero") String genero,
                           @Param("precioMin") Double precioMin,
                           @Param("precioMax") Double precioMax,
                           @Param("texto") String texto,
                           Pageable pageable);

    boolean existsByTiendaAndIdExterno(String tienda, String idExterno);
}
