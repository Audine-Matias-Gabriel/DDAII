package com.ddaii.repositories;
import com.ddaii.domain.DetallePedido;
import com.ddaii.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {


}
