package com.ddaii.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ProductoTest {

    private Producto productoConStock(Integer stock) {
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

    @Test
    void actualizarStock_descuenta_la_cantidad_indicada() {
        Producto producto = productoConStock(10);

        producto.actualizarStock(3);

        assertThat(producto.getStock()).isEqualTo(7);
    }

    @Test
    void actualizarStock_cantidadCero_lanzaIllegalArgumentException() {
        Producto producto = productoConStock(10);

        assertThatThrownBy(() -> producto.actualizarStock(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a 0");
    }

    @Test
    void actualizarStock_cantidadNegativa_lanzaIllegalArgumentException() {
        Producto producto = productoConStock(10);

        assertThatThrownBy(() -> producto.actualizarStock(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarStock_cantidadNull_lanzaIllegalArgumentException() {
        Producto producto = productoConStock(10);

        assertThatThrownBy(() -> producto.actualizarStock(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarStock_cantidadMayorQueStock_lanzaIllegalStateException() {
        Producto producto = productoConStock(2);

        assertThatThrownBy(() -> producto.actualizarStock(5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void actualizarStock_stockNull_lanzaIllegalStateException() {
        Producto producto = productoConStock(null);

        assertThatThrownBy(() -> producto.actualizarStock(1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void estaDisponible_conStockPositivo_esTrue() {
        assertThat(productoConStock(1).estaDisponible()).isTrue();
    }

    @Test
    void estaDisponible_conStockCero_esFalse() {
        assertThat(productoConStock(0).estaDisponible()).isFalse();
    }

    @Test
    void estaDisponible_conStockNull_esFalse() {
        assertThat(productoConStock(null).estaDisponible()).isFalse();
    }
}
