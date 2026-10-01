package com.ddaii.services;

import com.ddaii.domain.EstadoPedido;
import com.ddaii.domain.Pedido;
import com.ddaii.events.PedidoCreadoEvent;
import com.ddaii.events.PedidoCreadoEvent.ItemPedido;
import com.ddaii.repositories.PedidoRepository;
import com.ddaii.repositories.ProductoRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoCompraService {

    private final InventarioService inventarioService;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoCompraService(
            InventarioService inventarioService,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            ApplicationEventPublisher eventPublisher) {

        this.inventarioService = inventarioService;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Pedido confirmarCompra(Pedido pedido) {

        if (pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(
                    "El pedido debe tener al menos un detalle");
        }

        // 1. Validar todo el stock antes de confirmar.
        for (var detalle : pedido.getDetalles()) {
            inventarioService.verificarStock(
                    detalle.getProductoId(),
                    detalle.getCantidad()
            );
        }

        // 2. Calcular el total y marcar como confirmado.
        pedido.calcularTotal();
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // 3. Persistir el pedido para que tenga ID antes de publicar el
        // evento, que lo usa como clave del descuento de stock.
        pedido = pedidoRepository.save(pedido);

        // 4. Publicar el evento de pedido creado.
        List<ItemPedido> items = pedido.getDetalles()
                .stream()
                .map(detalle -> {
                    var producto = productoRepository
                            .findById(detalle.getProductoId())
                            .orElseThrow();

                    return new ItemPedido(
                            producto.getId(),
                            producto.getNombre(),
                            detalle.getCantidad()
                    );
                })
                .toList();

        eventPublisher.publishEvent(
                new PedidoCreadoEvent(pedido.getId(), items)
        );

        return pedido;
    }
}