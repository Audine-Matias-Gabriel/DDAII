package com.ddaii.domain;

import com.ddaii.converters.StockMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "productos")
@Data
public class Producto {

    @Id
    private Long id;

    private String nombre;

    private String descripcion;

    private BigDecimal precio;

    // Stock por género y talle: stock[genero][talle] -> cantidad.
    // Se persiste como JSON en una columna text (ver StockMapConverter),
    // porque JPA no soporta mapas anidados con @ElementCollection.
    @Convert(converter = StockMapConverter.class)
    @Column(columnDefinition = "text")
    private Map<Genero, Map<String, Integer>> stock;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    @Enumerated(EnumType.STRING)
    private EstadoProducto estado = EstadoProducto.NUEVO;

    // EAGER porque open-in-view=false: los controllers devuelven la entidad
    // y Jackson no podría inicializar la colección al serializar el JSON.
    @ElementCollection(fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<String> talles = new ArrayList<>();

    private Long tiendaId;

    private String imagenUrl;

    private LocalDateTime creadoEn = LocalDateTime.now();

    public Producto() {
    }

    public Producto(
            Long id,
            String nombre,
            String descripcion,
            BigDecimal precio,
            Map<Genero, Map<String, Integer>> stock,
            Categoria categoria,
            Long tiendaId,
            String imagenUrl) {

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.tiendaId = tiendaId;
        this.imagenUrl = imagenUrl;
    }

    /**
     * Cantidad disponible en la celda género + talle. Devuelve 0 si la
     * combinación no existe (no explota).
     */
    public int stockDe(Genero genero, String talle) {

        if (genero == null || talle == null || stock == null) {
            return 0;
        }

        Map<String, Integer> porTalle = stock.get(genero);

        if (porTalle == null) {
            return 0;
        }

        Integer cantidad = porTalle.get(talle);

        return cantidad == null ? 0 : cantidad;
    }

    /**
     * Indica si la combinación género + talle existe en el mapa de stock.
     */
    public boolean existeVariante(Genero genero, String talle) {

        if (genero == null || talle == null || stock == null) {
            return false;
        }

        Map<String, Integer> porTalle = stock.get(genero);

        return porTalle != null && porTalle.containsKey(talle);
    }

    public boolean estaDisponible() {

        if (stock == null) {
            return false;
        }

        for (Map<String, Integer> porTalle : stock.values()) {

            if (porTalle == null) {
                continue;
            }

            for (Integer cantidad : porTalle.values()) {
                if (cantidad != null && cantidad > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Descuenta de la celda género + talle la cantidad indicada.
     */
    public void actualizarStock(Genero genero, String talle, Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a 0");
        }

        if (genero == null || talle == null) {
            throw new IllegalArgumentException(
                    "El género y el talle son obligatorios");
        }

        int disponible = stockDe(genero, talle);

        if (cantidad > disponible) {
            throw new IllegalStateException(
                    "Stock insuficiente para el producto " + id
                            + " (" + genero + " " + talle + ")");
        }

        stock.get(genero).put(talle, disponible - cantidad);
    }
}
