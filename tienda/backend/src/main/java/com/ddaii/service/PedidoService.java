package com.ddaii.service;

import com.ddaii.domain.EstadoPedido;
import com.ddaii.domain.Pedido;
import com.ddaii.events.PedidoCreadoEvent;
import com.ddaii.events.PedidoCreadoEvent.ItemPedido;
import com.ddaii.repositories.ProductoRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoService(
            InventarioService inventarioService,
            ProductoRepository productoRepository,
            ApplicationEventPublisher eventPublisher) {

        this.inventarioService = inventarioService;
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
    }

    public Pedido confirmarCompra(Pedido pedido) {

        
        for (var detalle : pedido.getDetalles()) {
            inventarioService.verificarStock(
                    detalle.getProductoId(),
                    detalle.getCantidad()
            );
        }

      
        pedido.calcularTotal();
        pedido.setEstado(EstadoPedido.PENDIENTE);

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
