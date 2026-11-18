package com.ddaii.controllers;

import com.ddaii.services.XimilarIndexadoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Carga del catálogo en la colección de Ximilar (requiere sesión).
 * POST /api/similares/indexar?maximo=100 sube hasta `maximo` imágenes pendientes
 * (0,25 créditos cada una) y devuelve cuántas subió y los créditos estimados.
 * Para subir todo el catálogo, repetir hasta que `pendientesDespues` sea 0.
 */
@RestController
@RequestMapping("/api/similares")
public class XimilarController {

    private final XimilarIndexadoService indexadoService;

    public XimilarController(XimilarIndexadoService indexadoService) {
        this.indexadoService = indexadoService;
    }

    @PostMapping("/indexar")
    public XimilarIndexadoService.Resumen indexar(@RequestParam(defaultValue = "100") int maximo) {
        return indexadoService.indexarPendientes(maximo);
    }
}
