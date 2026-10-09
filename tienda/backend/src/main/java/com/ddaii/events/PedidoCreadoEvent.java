package com.ddaii.events;

import com.ddaii.domain.Genero;

import java.util.List;

public record PedidoCreadoEvent(
    Long pedidoId,
    List<ItemPedido> items 
) {
    public PedidoCreadoEvent(Long pedidoId, List<ItemPedido> items){
        this.pedidoId = pedidoId;
        this.items = List.copyOf(items);

    }

    public record ItemPedido(
        Long productoId,
        String nombre,
        Genero genero,
        String talle,
        int cantidad
    ) {}
    
}
