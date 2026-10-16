package com.ddaii.services;

import com.ddaii.domain.EstadoPedido;
import com.ddaii.domain.Pedido;
import com.ddaii.domain.Producto;
import com.ddaii.events.PedidoCreadoEvent;
import com.ddaii.events.PedidoCreadoEvent.ItemPedido;
import com.ddaii.repositories.PedidoRepository;
import com.ddaii.repositories.ProductoRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        // 1. Validar cada detalle y resolver el precio desde la base: el
        //    cliente manda productoId, cantidad, talle y género, nunca el precio.
        Map<Long, Producto> productos = new LinkedHashMap<>();

        for (var detalle : pedido.getDetalles()) {

            if (detalle.getProductoId() == null) {
                throw new IllegalArgumentException(
                        "Cada detalle debe indicar el productoId");
            }

            if (detalle.getCantidad() == null
                    || detalle.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor a cero para el producto "
                                + detalle.getProductoId());
            }

            if (detalle.getGenero() == null) {
                throw new IllegalArgumentException(
                        "Cada detalle debe indicar el género del producto "
                                + detalle.getProductoId());
            }

            if (detalle.getTalle() == null
                    || detalle.getTalle().isBlank()) {

                throw new IllegalArgumentException(
                        "Cada detalle debe indicar el talle del producto "
                                + detalle.getProductoId());
            }

            Producto producto = productoRepository
                    .findById(detalle.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto inexistente: "
                                    + detalle.getProductoId()));

            if (!producto.existeVariante(
                    detalle.getGenero(),
                    detalle.getTalle())) {

                throw new IllegalArgumentException(
                        "Combinación género/talle inexistente para el producto "
                                + detalle.getProductoId()
                                + ": " + detalle.getGenero()
                                + " " + detalle.getTalle());
            }

            detalle.setPrecioUnitario(producto.getPrecio());

            productos.put(producto.getId(), producto);
        }

        // 2. Verificar todo el stock antes de confirmar.
        for (var detalle : pedido.getDetalles()) {
            inventarioService.verificarStock(
                    detalle.getProductoId(),
                    detalle.getGenero(),
                    detalle.getTalle(),
                    detalle.getCantidad()
            );
        }

        // 3. Calcular el total y tomar la tienda del primer producto.
        pedido.calcularTotal();
        pedido.setTiendaId(
                productos.values().iterator().next().getTiendaId());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // 4. Persistir el pedido para que tenga ID antes de publicar el
        // evento, que lo usa como clave del descuento de stock.
        Pedido guardado = pedidoRepository.save(pedido);

        // 5. Publicar el evento de pedido creado.
        List<ItemPedido> items = pedido.getDetalles()
                .stream()
                .map(detalle -> new ItemPedido(
                        detalle.getProductoId(),
                        productos.get(
                                detalle.getProductoId()
                        ).getNombre(),
                        detalle.getGenero(),
                        detalle.getTalle(),
                        detalle.getCantidad()
                ))
                .toList();

        eventPublisher.publishEvent(
                new PedidoCreadoEvent(guardado.getId(), items)
        );

        return guardado;
    }
}
