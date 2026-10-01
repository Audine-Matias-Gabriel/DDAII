package com.ddaii.services;

import com.ddaii.domain.DetallePedido;
import com.ddaii.domain.Pedido;
import com.ddaii.domain.Producto;
import com.ddaii.repositories.PedidoRepository;
import com.ddaii.repositories.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;


@Service("pedidoQueryService")
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository){
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    public List<Pedido> obtenerPedido(Long clienteId){
        // Obtener todos los pedidos de un cliente
        if(clienteId == null || clienteId <= 0){ // Validar que el id exista
            throw new IllegalArgumentException("Id de cliente invalido");
        }

        List<Pedido> pedidos = pedidoRepository.findByClienteId(clienteId);

        if(pedidos.isEmpty()){ // Validar que el cliente tenga pedidos
            throw  new NoSuchElementException("El cliente no tiene pedidos");
        }
        return pedidos;
    }

    @Transactional
    public void agregarProducto(Long pedidoId, Long productoId, Integer cantidad) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();

        Producto producto = productoRepository.findById(productoId).orElseThrow();

        DetallePedido detalle = new DetallePedido();

        detalle.setProductoId(producto.getId());
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());

        pedido.agregarDetalle(detalle);
        pedido.calcularTotal();

        pedidoRepository.save(pedido);
    }
    
}



