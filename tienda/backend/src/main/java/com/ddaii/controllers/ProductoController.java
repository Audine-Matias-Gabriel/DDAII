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
<<<<<<< HEAD
    }

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {

        return ResponseEntity.ok(
                productoService.obtenerTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
=======

    }

    /**
     * GET /api/productos
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {

        return ResponseEntity.ok(
                productoService.listarProductos()
        );
    }

    /**
     * GET /api/productos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(
>>>>>>> luciano/backend_2&4
            @PathVariable Long id) {

        try {

<<<<<<< HEAD
            return ResponseEntity.ok(
                    productoService.obtenerPorId(id)
            );
=======
            Producto producto =
                    productoService.obtenerProducto(id);

            return ResponseEntity.ok(producto);
>>>>>>> luciano/backend_2&4

        } catch (NoSuchElementException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));
        }
    }

<<<<<<< HEAD
    @GetMapping("/tienda/{tiendaId}")
    public ResponseEntity<List<Producto>> buscarPorTienda(
            @PathVariable Long tiendaId) {

        return ResponseEntity.ok(
                productoService.buscarPorTienda(tiendaId)
        );
    }
=======
    /**
     * GET /api/productos/tienda/{id}
     */
    @GetMapping("/tienda/{id}")
    public ResponseEntity<List<Producto>> buscarPorTienda(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productoService.buscarPorTienda(id)
        );
    }

    /**
     * PUT /api/productos/{id}/stock?cantidad=2
     *
     * Descuenta "cantidad" unidades del stock.
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {

        try {

            Producto producto =
                    productoService.actualizarStock(
                            id,
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
>>>>>>> luciano/backend_2&4
}



