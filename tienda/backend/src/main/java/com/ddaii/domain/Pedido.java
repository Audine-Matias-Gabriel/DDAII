package com.ddaii.domain;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "tienda_id")
    private Long tiendaId;

    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    private BigDecimal total = BigDecimal.ZERO;

    @Setter(AccessLevel.NONE) // la deserialización la maneja reemplazarDetalles
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    void antesDePersistir() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this); // mantiene sincronizado el lado dueño (FK)
        detalles.add(detalle);
    }

    /**
     * Jackson deserializa "detalles" por acá en lugar del setDetalleList que
     * genera Lombok: este delega en agregarDetalle para no dejar el lado
     * dueño (FK) en null, que el @ManyToOne(optional = false) rechaza.
     */
    @JsonSetter("detalles")
    public void reemplazarDetalles(List<DetallePedido> nuevos) {
        detalles.clear();
        if (nuevos != null) {
            for (DetallePedido detalle : nuevos) {
                agregarDetalle(detalle);
            }
        }
    }

    public void calcularTotal() {
        BigDecimal suma = BigDecimal.ZERO;
        for (DetallePedido detalle : detalles) {
            detalle.setSubtotal(detalle.calcularSubtotal());
            suma = suma.add(detalle.getSubtotal());
        }
        this.total = suma;
    }
}