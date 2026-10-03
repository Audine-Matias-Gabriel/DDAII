package com.ddaii.service;



import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Pedido;
import com.ddaii.events.PedidoCreadoEvent;
import com.ddaii.events.PedidoCreadoEvent.ItemPedido;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.events.StockActualizadoEvent;


@Service
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InventarioService(
            ProductoRepository productoRepository,
            ApplicationEventPublisher eventPublisher) {

        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
    }


    public synchronized void verificarStock(
            Long productoId,
            int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero");
        }

        Producto producto = productoRepository
                .findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto inexistente: " + productoId));

        if (producto.getStock() < cantidad) {
            throw new IllegalStateException(
                    "Stock insuficiente para: "
                            + producto.getNombre()
                            + ". Disponible: "
                            + producto.getStock());
        }
    }

   
    public synchronized StockActualizadoEvent actualizarStock(
            Long pedidoId,
            Long productoId,
            int cantidad) {

        verificarStock(productoId, cantidad);

        Producto producto = productoRepository
                .findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto inexistente: " + productoId));

        int stockAnterior = producto.getStock();
        int stockActual = stockAnterior - cantidad;

        producto.setStock(stockActual);
        productoRepository.save(producto);

        StockActualizadoEvent evento =
                new StockActualizadoEvent(
                        pedidoId,
                        producto.getId(),
                        producto.getNombre(),
                        cantidad,
                        stockAnterior,
                        stockActual,
                        LocalDateTime.now()
                );

        eventPublisher.publishEvent(evento);

        return evento;
    }

    
    @EventListener
    public synchronized void alCrearPedido(
            PedidoCreadoEvent evento) {

        
        Map<Long, Integer> cantidades = new HashMap<>();

        for (var item : evento.items()) {
            cantidades.merge(
                    item.productoId(),
                    item.cantidad(),
                    Integer::sum
            );
        }


        for (var entry : cantidades.entrySet()) {
            verificarStock(entry.getKey(), entry.getValue());
        }

        for (var entry : cantidades.entrySet()) {
            actualizarStock(
                    evento.pedidoId(),
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }
}
