package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.dto.RecomendacionDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del recomendador con resultado esperado (docs/ia.md).
 */
class RecomendacionServiceTest {

    private Producto producto(String categoria, String genero, String marca, Double precio) {
        Producto p = new Producto();
        p.setCategoria(categoria);
        p.setGenero(genero);
        p.setMarca(marca);
        p.setPrecio(precio);
        p.setDisponible(true);
        return p;
    }

    @Test
    void mismaCategoriaSumaPuntaje() {
        Producto e = producto("REMERA", "Mujer", "Nike", 1000.0);
        Producto c = producto("REMERA", "Mujer", "Nike", 1000.0);
        assertTrue(RecomendacionService.puntaje(e, c) > 0.5);
    }

    @Test
    void mismoGeneroSumaPuntaje() {
        Producto e = producto("REMERA", "Mujer", "Nike", 1000.0);
        Producto c = producto("PANTALON", "Mujer", "Adidas", 2000.0);
        double score = RecomendacionService.puntaje(e, c);
        assertTrue(score >= 0.2 && score < 0.6);
    }

    @Test
    void precioParecidoSumaPuntaje() {
        Producto e = producto("REMERA", "Mujer", "Nike", 1000.0);
        Producto c = producto("REMERA", "Hombre", "Nike", 1200.0);
        assertTrue(RecomendacionService.puntaje(e, c) >= 0.2);
    }

    @Test
    void recomendacionPorContenidoDevuelveOrdenadoPorPuntaje() {
        Producto e = producto("REMERA", "Mujer", "Nike", 1000.0);
        Producto c1 = producto("REMERA", "Mujer", "Nike", 1000.0);
        Producto c2 = producto("PANTALON", "Hombre", "Otra", 5000.0);
        List<Producto> candidatos = new ArrayList<>();
        candidatos.add(c2);
        candidatos.add(c1);

        List<RecomendacionDTO> resultado =
                new RecomendacionService.RecomendacionPorContenido().recomendar(e, candidatos, 2);

        assertEquals(2, resultado.size());
        assertEquals("REMERA", resultado.get(0).getCategoria());
    }

    @Test
    void recomendacionComplementariaExcluyeMismaCategoria() {
        Producto e = producto("REMERA", "Mujer", "Nike", 1000.0);
        Producto c1 = producto("REMERA", "Mujer", "Nike", 1000.0);
        Producto c2 = producto("PANTALON", "Mujer", "Nike", 1000.0);
        List<Producto> candidatos = new ArrayList<>();
        candidatos.add(c1);
        candidatos.add(c2);

        List<RecomendacionDTO> resultado =
                new RecomendacionService.RecomendacionComplementaria().recomendar(e, candidatos, 5);

        assertEquals(1, resultado.size());
        assertEquals("PANTALON", resultado.get(0).getCategoria());
    }

    @Test
    void factoryCreaEstrategiaContenidoPorDefecto() {
        RecomendacionService.EstrategiaRecomendacion s =
                RecomendacionService.RecomendacionFactory.crear("contenido");
        assertTrue(s instanceof RecomendacionService.RecomendacionPorContenido);
    }

    @Test
    void factoryCreaEstrategiaComplementaria() {
        RecomendacionService.EstrategiaRecomendacion s =
                RecomendacionService.RecomendacionFactory.crear("complementaria");
        assertTrue(s instanceof RecomendacionService.RecomendacionComplementaria);
    }
}
