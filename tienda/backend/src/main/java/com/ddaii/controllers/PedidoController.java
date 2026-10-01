package com.ddaii.controllers;

import com.ddaii.domain.Pedido;
import com.ddaii.services.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService){
        this.pedidoService = pedidoService;

    }
    @GetMapping("/{clienteId}/pedidos")
    public ResponseEntity<?> obtenerPedidos(@PathVariable Long clienteId){
        if(clienteId == null || clienteId <= 0){
            return ResponseEntity.badRequest().body("ID invalido");
        }
        try{
            List<Pedido> pedidos = pedidoService.obtenerPedido(clienteId);
            return ResponseEntity.ok(pedidos);
        }
        catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        }
        catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


}

