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

        Producto c3 = producto("PANTALON", "Mujer", "Otra", 5000.0);
        candidatos.add(c3);

        List<RecomendacionDTO> resultado =
                new RecomendacionService.RecomendacionPorContenido().recomendar(e, candidatos, 3);

        // c1 (todo igual) primero, c3 (solo el género) después; c2 no tiene nada en común: no se recomienda.
        assertEquals(2, resultado.size());
        assertEquals("REMERA", resultado.get(0).getCategoria());
        assertEquals("Mujer", resultado.get(1).getGenero());
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

    private long siguienteId = 1;

    private Producto conId(String categoria, String genero, String marca, Double precio) {
        Producto p = producto(categoria, genero, marca, precio);
        p.setId(siguienteId++);
        return p;
    }

    @Test
    void factoryRechazaUnaEstrategiaDesconocida() {
        assertThrows(IllegalArgumentException.class,
                () -> RecomendacionService.RecomendacionFactory.crear("magia"));
        assertTrue(RecomendacionService.RecomendacionFactory.crear(null)
                instanceof RecomendacionService.RecomendacionPorContenido);
    }

    @Test
    void unisexCombinaConCualquierGeneroPeroMenosQueElMismo() {
        Producto e = producto("REMERA", "Hombre", "Nike", 1000.0);
        double mismo = RecomendacionService.puntaje(e, producto("REMERA", "Hombre", "X", 9000.0));
        double unisex = RecomendacionService.puntaje(e, producto("REMERA", "Unisex", "X", 9000.0));
        double otro = RecomendacionService.puntaje(e, producto("REMERA", "Mujer", "X", 9000.0));
        assertTrue(mismo > unisex && unisex > otro);
    }

    @Test
    void sinStockNoSeRecomienda() {
        Producto e = producto("REMERA", "Mujer", "Nike", 1000.0);
        Producto sinStock = producto("REMERA", "Mujer", "Nike", 1000.0);
        sinStock.setDisponible(false);
        assertEquals(0.0, RecomendacionService.puntaje(e, sinStock));
    }

    @Test
    void anteEmpateGanaElPrecioMasCercano() {
        Producto e = conId("REMERA", "Mujer", "Nike", 1000.0);
        Producto lejos = conId("REMERA", "Mujer", "Nike", 1250.0); // mismo puntaje (precio <= 30 %)
        Producto cerca = conId("REMERA", "Mujer", "Nike", 1050.0);

        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionPorContenido()
                .recomendar(e, List.of(lejos, cerca), 2);

        assertEquals(cerca.getId(), r.get(0).getId());
    }

    @Test
    void complementariaArmaUnConjuntoConUnaPrendaDeCadaCategoria() {
        Producto remera = conId("REMERA", "Hombre", "Nike", 30000.0);
        List<Producto> candidatos = List.of(
                conId("CALZADO", "Hombre", "Nike", 30000.0),
                conId("CALZADO", "Hombre", "Nike", 31000.0),
                conId("CALZADO", "Hombre", "Puma", 30000.0),
                conId("PANTALON", "Hombre", "Adidas", 30000.0),
                conId("PANTALON", "Mujer", "Nike", 30000.0),   // no combina por género
                conId("REMERA", "Hombre", "Nike", 30000.0));   // misma categoría: no va

        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionComplementaria()
                .recomendar(remera, candidatos, 3);

        // Primero un pantalón y un calzado (el conjunto), después el segundo calzado.
        assertEquals(List.of("PANTALON", "CALZADO", "CALZADO"),
                r.stream().map(RecomendacionDTO::getCategoria).toList());
        assertTrue(r.stream().noneMatch(d -> "Mujer".equals(d.getGenero())));
    }

    @Test
    void vestidoSeCombinaConCalzado() {
        Producto vestido = conId("VESTIDO", "Mujer", "Zara", 20000.0);
        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionComplementaria().recomendar(vestido,
                List.of(conId("REMERA", "Mujer", "Zara", 20000.0), conId("CALZADO", "Mujer", "Otra", 25000.0)), 5);
        assertEquals(List.of("CALZADO"), r.stream().map(RecomendacionDTO::getCategoria).toList());
    }

    @Test
    void elLimiteSeValida() {
        assertEquals(5, RecomendacionService.normalizarLimite(null));
        assertEquals(20, RecomendacionService.normalizarLimite(20));
        assertThrows(IllegalArgumentException.class, () -> RecomendacionService.normalizarLimite(0));
        assertThrows(IllegalArgumentException.class, () -> RecomendacionService.normalizarLimite(21));
    }
}
