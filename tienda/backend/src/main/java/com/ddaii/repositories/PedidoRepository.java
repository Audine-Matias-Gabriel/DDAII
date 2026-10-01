package com.ddaii.repositories;

import com.ddaii.domain.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Trae los detalles junto con el pedido: open-in-view=false, así que
    // el controller no podría cargarlos lazy al serializar el JSON.
    @EntityGraph(attributePaths = "detalles")
    List<Pedido> findByClienteId(Long clienteId);
}
