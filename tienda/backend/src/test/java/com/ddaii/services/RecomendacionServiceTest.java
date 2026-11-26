package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.dto.RecomendacionDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    void contenidoNoMezclaGenerosQueNoCombinan() {
        // Caso real de la validación: a una calza adidas de mujer se le recomendaba un short adidas de hombre.
        Producto calzaMujer = conId("PANTALON", "Mujer", "ADIDAS", 59999.0);
        Producto shortHombre = conId("PANTALON", "Hombre", "ADIDAS", 49999.0);
        Producto calzaMujerOtra = conId("PANTALON", "Mujer", "Atomik", 44900.0);
        Producto shortUnisex = conId("PANTALON", "Unisex", "Topper", 15599.0);

        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionPorContenido()
                .recomendar(calzaMujer, List.of(shortHombre, calzaMujerOtra, shortUnisex), 5);

        assertEquals(List.of(calzaMujerOtra.getId(), shortUnisex.getId()),
                r.stream().map(RecomendacionDTO::getId).toList());
    }

    @Test
    void noRepiteVariantesConElMismoNombreNiLaPrendaDeEntrada() {
        Producto entrada = conId("REMERA", "Hombre", "NBA", 24999.0);
        entrada.setNombre("REMERA NBA CHICAGO BULLS HOMBRE");
        Producto mismaOtroColor = conId("REMERA", "Hombre", "NBA", 24999.0);
        mismaOtroColor.setNombre("Remera NBA Chicago Bulls Hombre");
        Producto lakers1 = conId("REMERA", "Hombre", "NBA", 24999.0);
        lakers1.setNombre("REMERA NBA LOS ANGELES LAKERS HOMBRE");
        Producto lakers2 = conId("REMERA", "Hombre", "NBA", 24999.0);
        lakers2.setNombre("REMERA NBA LOS ANGELES LAKERS HOMBRE");

        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionPorContenido()
                .recomendar(entrada, List.of(mismaOtroColor, lakers1, lakers2), 5);

        assertEquals(List.of(lakers1.getId()), r.stream().map(RecomendacionDTO::getId).toList());
    }

    @Test
    void laMarcaDeInteresDelPremiumOrdenaPrimeroAunqueTengaElMismoPuntajeBase() {
        Producto entrada = conId("REMERA", "Mujer", "Otra", 1000.0);
        Producto deOtraMarca = conId("PANTALON", "Mujer", "Adidas", 1000.0);
        Producto deLaMarcaFavorita = conId("PANTALON", "Mujer", "Nike", 1000.0);
        PerfilInteres perfil = new PerfilInteres(Map.of("NIKE", 1.0), Map.of(), 0.15, 0.10);

        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionPorContenido()
                .recomendar(entrada, List.of(deOtraMarca, deLaMarcaFavorita), 2, perfil);

        assertEquals(List.of(deLaMarcaFavorita.getId(), deOtraMarca.getId()),
                r.stream().map(RecomendacionDTO::getId).toList());
        assertTrue(r.get(0).getPuntaje() > r.get(1).getPuntaje());
    }

    @Test
    void laCategoriaDeInteresDelPremiumTambienSuma() {
        Producto entrada = conId("REMERA", "Hombre", "Otra", 1000.0);
        Producto pantalon = conId("PANTALON", "Hombre", "Otra", 1000.0);
        Producto calzado = conId("CALZADO", "Hombre", "Otra", 1000.0);
        PerfilInteres perfil = new PerfilInteres(Map.of(), Map.of("CALZADO", 1.0), 0.15, 0.10);

        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionPorContenido()
                .recomendar(entrada, List.of(pantalon, calzado), 2, perfil);

        assertEquals(calzado.getId(), r.get(0).getId());
    }

    @Test
    void sinPerfilElRecomendadorSigueSiendoElDeSiempre() {
        Producto entrada = conId("REMERA", "Hombre", "Otra", 1000.0);
        Producto pantalon = conId("PANTALON", "Hombre", "Otra", 1000.0);
        Producto calzado = conId("CALZADO", "Hombre", "Otra", 1000.0);

        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionPorContenido()
                .recomendar(entrada, List.of(calzado, pantalon), 2);

        // Empate: gana el id menor (pantalón), sin sesgo de intereses.
        assertEquals(List.of(pantalon.getId(), calzado.getId()),
                r.stream().map(RecomendacionDTO::getId).toList());
    }

    @Test
    void elInteresNoResucitaProductosSinStock() {
        Producto entrada = conId("REMERA", "Mujer", "Otra", 1000.0);
        Producto sinStock = conId("PANTALON", "Mujer", "Nike", 1000.0);
        sinStock.setDisponible(false);
        PerfilInteres perfil = new PerfilInteres(Map.of("NIKE", 1.0), Map.of(), 0.15, 0.10);

        List<RecomendacionDTO> r = new RecomendacionService.RecomendacionPorContenido()
                .recomendar(entrada, List.of(sinStock), 5, perfil);

        assertTrue(r.isEmpty());
    }

    @Test
    void elPrincipalSeConvierteEnUsuarioIdSoloSiEsValido() {
        assertEquals(7L, RecomendacionService.parsearUsuario("7"));
        assertNull(RecomendacionService.parsearUsuario(null));
        assertNull(RecomendacionService.parsearUsuario("no-es-un-id"));
    }

    @Test
    void elLimiteSeValida() {
        assertEquals(5, RecomendacionService.normalizarLimite(null));
        assertEquals(20, RecomendacionService.normalizarLimite(20));
        assertThrows(IllegalArgumentException.class, () -> RecomendacionService.normalizarLimite(0));
        assertThrows(IllegalArgumentException.class, () -> RecomendacionService.normalizarLimite(21));
    }
}
