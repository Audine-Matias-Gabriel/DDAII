package com.ddaii.controllers;

import com.ddaii.domain.Producto;
import com.ddaii.services.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(
            ProductoService productoService) {

        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {

        return ResponseEntity.ok(
                productoService.obtenerTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    productoService.obtenerPorId(id)
            );

        } catch (NoSuchElementException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));
        }
    }

    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<List<Producto>> buscarPorTienda(
            @PathVariable Long tiendaId) {

        return ResponseEntity.ok(
                productoService.buscarPorTienda(tiendaId)
        );
    }
}



