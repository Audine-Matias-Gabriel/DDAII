package com.ddaii.dto;

import com.ddaii.domain.Producto;

public record ProductoSimilarResponse(
        Long id,
        String nombre,
        String imagenUrl,
        String urlOriginal,
        String tienda,
        Double precio
) {
    public static ProductoSimilarResponse from(Producto p) {
        return new ProductoSimilarResponse(
                p.getId(),
                p.getNombre(),
                p.getImagenUrl(),
                p.getUrlBase(),
                p.getTiendaId() != null ? p.getTiendaId().getNombre() : null,
                p.getPrecio() != null ? p.getPrecio().doubleValue() : null
        );
    }
}