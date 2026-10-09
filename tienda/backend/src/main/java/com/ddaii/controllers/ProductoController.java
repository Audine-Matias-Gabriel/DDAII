package com.ddaii.controllers;

import com.ddaii.domain.Genero;
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
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:4173"
})
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(
            ProductoService productoService) {

        this.productoService = productoService;
    }

    /**
     * GET /api/productos
     */
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {

        return ResponseEntity.ok(
                productoService.obtenerTodos()
        );
    }

    /**
     * GET /api/productos/{id}
     */
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

    /**
     * GET /api/productos/tienda/{tiendaId}
     */
    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<List<Producto>> buscarPorTienda(
            @PathVariable Long tiendaId) {

        return ResponseEntity.ok(
                productoService.buscarPorTienda(tiendaId)
        );
    }

    /**
     * PUT /api/productos/{id}/stock?genero=UNISEX&talle=M&cantidad=2
     *
     * Descuenta "cantidad" unidades de la celda género + talle.
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(
            @PathVariable Long id,
            @RequestParam Genero genero,
            @RequestParam String talle,
            @RequestParam Integer cantidad) {

        try {

            Producto producto =
                    productoService.actualizarStock(
                            id,
                            genero,
                            talle,
                            cantidad
                    );

            return ResponseEntity.ok(producto);

        } catch (NoSuchElementException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));
        }
    }
}