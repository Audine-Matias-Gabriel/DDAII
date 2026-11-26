package com.ddaii.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Historial de interés de un usuario premium. Es distinto de {@link Favorito}: el favorito es el
 * estado actual (se puede desmarcar), mientras que acá se acumula lo que el usuario ya miró o
 * marcó para que el recomendador sepa qué marcas y categorías le interesan.
 */
@Entity
@Table(name = "interes_producto", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "producto_id", "tipo"})
})
public class InteresProducto {

    public enum Tipo { VISTA, FAVORITO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    /** Cuántas veces se repitió la señal: cada vista suma 1 y un favorito vale 3. */
    @Column(nullable = false)
    private int peso = 1;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public InteresProducto() {}

    public InteresProducto(Usuario usuario, Producto producto, Tipo tipo, int peso) {
        this.usuario = usuario;
        this.producto = producto;
        this.tipo = tipo;
        this.peso = peso;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public int getPeso() { return peso; }
    public void setPeso(int peso) { this.peso = peso; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
