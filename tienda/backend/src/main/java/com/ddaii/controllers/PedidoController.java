package com.ddaii.controllers;

import com.ddaii.domain.Pedido;
import com.ddaii.services.PedidoCompraService;
import com.ddaii.services.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:5173")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoCompraService pedidoCompraService;

    public PedidoController(
            PedidoService pedidoService,
            PedidoCompraService pedidoCompraService) {

        this.pedidoService = pedidoService;
        this.pedidoCompraService = pedidoCompraService;
    }

    @GetMapping("/{clienteId}/pedidos")
    public ResponseEntity<?> obtenerPedidos(@PathVariable Long clienteId) {
        if (clienteId == null || clienteId <= 0) {
            return ResponseEntity.badRequest().body("ID invalido");
        }
        try {
            List<Pedido> pedidos = pedidoService.obtenerPedido(clienteId);
            return ResponseEntity.ok(pedidos);
        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/pedidos
     *
     * Confirma la compra: valida stock, persiste el pedido y descuenta
     * inventario publicando PedidoCreadoEvent.
     */
    @PostMapping
    public ResponseEntity<?> confirmarCompra(@RequestBody Pedido pedido) {
        try {
            Pedido confirmado = pedidoCompraService.confirmarCompra(pedido);
            return ResponseEntity.status(HttpStatus.CREATED).body(confirmado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}