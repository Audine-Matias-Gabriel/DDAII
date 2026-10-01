package com.ddaii.domain;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
<<<<<<< HEAD
@Table(name = "pedidos")
public class Pedido {
=======
public class Pedido{
    private long id;
    private long clientId;
    private long tiendaId;
    private LocalDateTime fecha;
    private EstadoPedido estadoPedido;
    private BigDecimal total;
>>>>>>> luciano/backend_2&4

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

<<<<<<< HEAD
    public void calcularTotal() {
        BigDecimal suma = BigDecimal.ZERO;
        for (DetallePedido detalle : detalles) {
            detalle.setSubtotal(detalle.calcularSubtotal());
            suma = suma.add(detalle.getSubtotal());
        }
        this.total = suma;
    }
=======
    public List<DetallePedido> getDetalles(){
        return detalles;
    }

    public long getId(){
        return this.id;
    }

    public void calcularTotal(){}

    public void setEstado(String hecho){}



>>>>>>> luciano/backend_2&4
}
