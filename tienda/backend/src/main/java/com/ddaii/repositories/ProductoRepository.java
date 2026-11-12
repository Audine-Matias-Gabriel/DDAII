package com.ddaii.repositories;

import com.ddaii.domain.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de productos (docs/PLAN-INTEGRACION.md 2.1).
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByTienda_IdAndIdExterno(Long tiendaId, String idExterno);

    List<Producto> findByTienda_Id(Long tiendaId);

    List<Producto> findByDisponibleTrue();

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByIdInAndTienda_IdNot(Collection<Long> ids, Long tiendaId);

    List<Producto> findByTienda_IdNotAndIdNot(Long tiendaId, Long id, Pageable pageable);

    @Query("SELECT p FROM Producto p " +
           "WHERE (:categoria IS NULL OR p.categoria = :categoria) " +
           "AND (:tiendaId IS NULL OR p.tienda.id = :tiendaId) " +
           "AND (:genero IS NULL OR p.genero = :genero) " +
           "AND (:precioMin IS NULL OR p.precio >= :precioMin) " +
           "AND (:precioMax IS NULL OR p.precio <= :precioMax) " +
           // CAST a String: si :texto llega null, Postgres lo tipa como bytea y LOWER() falla
           "AND (CAST(:texto AS String) IS NULL " +
           "     OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:texto AS String), '%')) " +
           "     OR LOWER(p.marca) LIKE LOWER(CONCAT('%', CAST(:texto AS String), '%')) " +
           "     OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', CAST(:texto AS String), '%'))) " +
           "AND p.disponible = true")
    Page<Producto> filtrar(@Param("categoria") String categoria,
                           @Param("tiendaId") Long tiendaId,
                           @Param("genero") String genero,
                           @Param("precioMin") Double precioMin,
                           @Param("precioMax") Double precioMax,
                           @Param("texto") String texto,
                           Pageable pageable);
}
