package com.ddaii.services;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.ddaii.domain.Genero;
import com.ddaii.domain.Producto;
import com.ddaii.events.PedidoCreadoEvent;
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

    /**
     * Verifica que exista stock suficiente en la celda género + talle.
     * No modifica el inventario.
     */
    public synchronized void verificarStock(
            Long productoId,
            Genero genero,
            String talle,
            int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero");
        }

        Producto producto = productoRepository
                .findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto inexistente: " + productoId));

        int disponible = producto.stockDe(genero, talle);

        if (disponible < cantidad) {
            throw new IllegalStateException(
                    "Stock insuficiente para: "
                            + producto.getNombre()
                            + " (" + genero + " " + talle + ")"
                            + ". Disponible: "
                            + disponible);
        }
    }

    /**
     * Descuenta unidades de la celda género + talle y publica
     * un evento con el resultado.
     */
    public synchronized StockActualizadoEvent actualizarStock(
            Long pedidoId,
            Long productoId,
            Genero genero,
            String talle,
            int cantidad) {

        verificarStock(productoId, genero, talle, cantidad);

        Producto producto = productoRepository
                .findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto inexistente: " + productoId));

        int stockAnterior = producto.stockDe(genero, talle);
        int stockActual = stockAnterior - cantidad;

        producto.actualizarStock(genero, talle, cantidad);
        productoRepository.save(producto);

        StockActualizadoEvent evento =
                new StockActualizadoEvent(
                        pedidoId,
                        producto.getId(),
                        producto.getNombre(),
                        genero,
                        talle,
                        cantidad,
                        stockAnterior,
                        stockActual,
                        LocalDateTime.now()
                );

        eventPublisher.publishEvent(evento);

        return evento;
    }

    /**
     * Observer: escucha los pedidos confirmados.
     * Se ejecuta sincrónicamente al publicar el evento.
     */
    @EventListener
    public synchronized void alCrearPedido(
            PedidoCreadoEvent evento) {

        // Agrupa ítems de la misma celda (producto + género + talle).
        // Distinto talle o género NO se agrupan: son celdas distintas.
        Map<Variante, Integer> cantidades = new HashMap<>();

        for (var item : evento.items()) {
            Variante clave = new Variante(
                    item.productoId(),
                    item.genero(),
                    item.talle());

            cantidades.merge(
                    clave,
                    item.cantidad(),
                    Integer::sum
            );
        }

        // Primera pasada: validar todas las celdas.
        for (var entry : cantidades.entrySet()) {
            Variante clave = entry.getKey();

            verificarStock(
                    clave.productoId(),
                    clave.genero(),
                    clave.talle(),
                    entry.getValue()
            );
        }

        // Segunda pasada: actualizar el inventario.
        for (var entry : cantidades.entrySet()) {
            Variante clave = entry.getKey();

            actualizarStock(
                    evento.pedidoId(),
                    clave.productoId(),
                    clave.genero(),
                    clave.talle(),
                    entry.getValue()
            );
        }
    }

    private record Variante(Long productoId, Genero genero, String talle) {
    }
}
