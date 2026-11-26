package com.ddaii.controllers;

import com.ddaii.dto.ResultadoVisualDTO;
import com.ddaii.services.BusquedaVisualService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Búsqueda visual premium (solo usuarios con plan premium).
 * POST /api/premium/busqueda-visual con multipart/form-data: campo "imagen" y "limite" opcional.
 */
@RestController
@RequestMapping("/api/premium")
public class BusquedaVisualController {

    private final BusquedaVisualService busquedaVisualService;

    public BusquedaVisualController(BusquedaVisualService busquedaVisualService) {
        this.busquedaVisualService = busquedaVisualService;
    }

    @PostMapping(value = "/busqueda-visual", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ResultadoVisualDTO> buscarVisual(@AuthenticationPrincipal String usuarioIdStr,
                                                 @RequestPart("imagen") MultipartFile imagen,
                                                 @RequestParam(defaultValue = "8") int limite) {
        return busquedaVisualService.buscar(usuarioIdStr, imagen, limite);
    }
}
