package com.ddaii.repositories;

import com.ddaii.domain.InteresProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InteresProductoRepository extends JpaRepository<InteresProducto, Long> {

    Optional<InteresProducto> findByUsuarioIdAndProductoIdAndTipo(
            Long usuarioId, Long productoId, InteresProducto.Tipo tipo);

    List<InteresProducto> findByUsuarioIdAndFechaAfterOrderByFechaDesc(
            Long usuarioId, LocalDateTime desde);
}
