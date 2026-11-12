package com.ddaii.repositories;

import com.ddaii.domain.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    @Query("DELETE FROM Favorito f WHERE f.usuario.id = :usuarioId AND f.producto.id = :productoId")
    void deleteByUsuarioIdAndProductoId(@Param("usuarioId") Long usuarioId,
                                        @Param("productoId") Long productoId);
}
