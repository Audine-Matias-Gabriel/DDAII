package com.ddaii.repositories;

import com.ddaii.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Pedido, Long> {
}
