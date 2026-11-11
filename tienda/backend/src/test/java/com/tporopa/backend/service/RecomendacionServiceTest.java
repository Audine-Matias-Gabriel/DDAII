package com.tporopa.backend.service;

import com.tporopa.backend.model.Producto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 5 tests unitarios del recomendador con resultado esperado (P4, Día 3).
 */
class RecomendacionServiceTest {

    private Producto entrada(String categoria, String genero, String marca, Double precio) {
        Producto p = new Producto();
        p.setCategoria(categoria);
        p.setGenero(genero);
        p.setMarca(marca);
        p.setPrecio(precio);
        p.setDisponible(true);
        return p;
    }

    private Producto candidata(String categoria, String genero, String marca, Double precio) {
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
        Producto e = entrada("REMERA", "Mujer", "Nike", 1000.0);
        Producto c = candidata("REMERA", "Mujer", "Nike", 1000.0);
        assertTrue(RecomendacionService.puntaje(e, c) > 0.5);
    }

    @Test
    void mismoGeneroSumaPuntaje() {
        Producto e = entrada("REMERA", "Mujer", "Nike", 1000.0);
        Producto c = candidata("PANTALON", "Mujer", "Adidas", 2000.0);
        double score = RecomendacionService.puntaje(e, c);
        assertTrue(score >= 0.2 && score < 0.6);
    }

    @Test
    void precioParecidoSumaPuntaje() {
        Producto e = entrada("REMERA", "Mujer", "Nike", 1000.0);
        Producto c = candidata("REMERA", "Hombre", "Nike", 1200.0); // +20% => parecido
        double score = RecomendacionService.puntaje(e, c);
        assertTrue(score >= 0.2);
    }

    @Test
    void recomendacionPorContenidoDevuelveOrdenadoPorPuntaje() {
        Producto e = entrada("REMERA", "Mujer", "Nike", 1000.0);
        Producto c1 = candidata("REMERA", "Mujer", "Nike", 1000.0); // puntaje alto
        Producto c2 = candidata("PANTALON", "Hombre", "Otra", 5000.0); // puntaje bajo
        List<Producto> candidatos = new ArrayList<>();
        candidatos.add(c2);
        candidatos.add(c1);

        List<com.tporopa.backend.dto.ProductoDTO> resultado =
                new RecomendacionService.RecomendacionPorContenido()
                        .recomendar(e, candidatos, 2);

        assertEquals(2, resultado.size());
        assertEquals("Remera", null); // placeholder
        assertEquals(c1.getNombre(), resultado.get(0).getNombre());
    }

    @Test
    void recomendacionComplementariaExcluyeMismaCategoria() {
        Producto e = entrada("REMERA", "Mujer", "Nike", 1000.0);
        Producto c1 = candidata("REMERA", "Mujer", "Nike", 1000.0); // misma categoria -> excluida
        Producto c2 = candidata("PANTALON", "Mujer", "Nike", 1000.0); // complementaria
        List<Producto> candidatos = new ArrayList<>();
        candidatos.add(c1);
        candidatos.add(c2);

        List<com.tporopa.backend.dto.ProductoDTO> resultado =
                new RecomendacionService.RecomendacionComplementaria()
                        .recomendar(e, candidatos, 5);

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
