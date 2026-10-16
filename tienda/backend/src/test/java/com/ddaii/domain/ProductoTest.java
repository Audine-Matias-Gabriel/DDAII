package com.ddaii.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ProductoTest {

    private Producto producto(Map<Genero, Map<String, Integer>> stock) {
        return new Producto(
                1L,
                "Remera Nike",
                "Remera deportiva",
                new BigDecimal("10000"),
                stock,
                Categoria.REMPERA,
                1L,
                "remera.png");
    }

    private Map<Genero, Map<String, Integer>> stockDePrueba() {
        Map<Genero, Map<String, Integer>> stock = new EnumMap<>(Genero.class);
        stock.put(Genero.HOMBRE, new HashMap<>(Map.of("M", 5, "L", 5)));
        stock.put(Genero.MUJER, new HashMap<>(Map.of("M", 2)));
        return stock;
    }

    @Test
    void stockDe_devuelveLaCantidadDeLaCelda() {
        Producto producto = producto(stockDePrueba());

        assertThat(producto.stockDe(Genero.HOMBRE, "M")).isEqualTo(5);
        assertThat(producto.stockDe(Genero.MUJER, "M")).isEqualTo(2);
    }

    @Test
    void stockDe_combinacionInexistente_devuelveCero() {
        Producto producto = producto(stockDePrueba());

        assertThat(producto.stockDe(Genero.HOMBRE, "XL")).isZero();
        assertThat(producto.stockDe(Genero.INFANTIL, "M")).isZero();
    }

    @Test
    void stockDe_generoONull_devuelveCero() {
        Producto producto = producto(stockDePrueba());

        assertThat(producto.stockDe(null, "M")).isZero();
        assertThat(producto.stockDe(Genero.HOMBRE, null)).isZero();
    }

    @Test
    void existeVariante_celdaPresente_esTrue() {
        Producto producto = producto(stockDePrueba());

        assertThat(producto.existeVariante(Genero.HOMBRE, "M")).isTrue();
        assertThat(producto.existeVariante(Genero.HOMBRE, "XL")).isFalse();
        assertThat(producto.existeVariante(Genero.INFANTIL, "M")).isFalse();
    }

    @Test
    void actualizarStock_descuentaSoloLaCeldaIndicada() {
        Producto producto = producto(stockDePrueba());

        producto.actualizarStock(Genero.HOMBRE, "M", 3);

        assertThat(producto.stockDe(Genero.HOMBRE, "M")).isEqualTo(2);
        assertThat(producto.stockDe(Genero.HOMBRE, "L")).isEqualTo(5);
        assertThat(producto.stockDe(Genero.MUJER, "M")).isEqualTo(2);
    }

    @Test
    void actualizarStock_cantidadCero_lanzaIllegalArgumentException() {
        Producto producto = producto(stockDePrueba());

        assertThatThrownBy(() -> producto.actualizarStock(Genero.HOMBRE, "M", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a 0");
    }

    @Test
    void actualizarStock_cantidadNegativa_lanzaIllegalArgumentException() {
        Producto producto = producto(stockDePrueba());

        assertThatThrownBy(() -> producto.actualizarStock(Genero.HOMBRE, "M", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarStock_cantidadNull_lanzaIllegalArgumentException() {
        Producto producto = producto(stockDePrueba());

        assertThatThrownBy(() -> producto.actualizarStock(Genero.HOMBRE, "M", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarStock_generoOTalleNull_lanzaIllegalArgumentException() {
        Producto producto = producto(stockDePrueba());

        assertThatThrownBy(() -> producto.actualizarStock(null, "M", 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> producto.actualizarStock(Genero.HOMBRE, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarStock_cantidadMayorQueLaCelda_lanzaIllegalStateException() {
        Producto producto = producto(stockDePrueba());

        assertThatThrownBy(() -> producto.actualizarStock(Genero.MUJER, "M", 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(producto.stockDe(Genero.MUJER, "M")).isEqualTo(2);
    }

    @Test
    void estaDisponible_conAlgunaCeldaPositiva_esTrue() {
        Producto producto = producto(stockDePrueba());

        assertThat(producto.estaDisponible()).isTrue();
    }

    @Test
    void estaDisponible_conTodasLasCeldasEnCero_esFalse() {
        Map<Genero, Map<String, Integer>> stock = new EnumMap<>(Genero.class);
        stock.put(Genero.UNISEX, new HashMap<>(Map.of("M", 0, "L", 0)));
        Producto producto = producto(stock);

        assertThat(producto.estaDisponible()).isFalse();
    }

    @Test
    void estaDisponible_conStockNull_esFalse() {
        Producto producto = producto(null);

        assertThat(producto.estaDisponible()).isFalse();
    }
}
