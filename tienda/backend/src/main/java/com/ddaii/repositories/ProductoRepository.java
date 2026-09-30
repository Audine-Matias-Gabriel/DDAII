package com.ddaii.repositories;
import com.ddaii.domain.DetallePedido;
import com.ddaii.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {

    Optional<Producto> findById(Long id);

    List<Producto> findByTiendaId(Long tiendaId);

    List<Producto> findAll();

    Producto save(Producto producto);

    void delete(Long id);


}
