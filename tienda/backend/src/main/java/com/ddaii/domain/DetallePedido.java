package com.ddaii.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalles_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @JsonIgnore evita la recursión infinita Pedido -> detalles -> pedido.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Pedido pedido;


    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public void setProductoId(long productoId){
        this.productoId = productoId;
    }

    public void setCantidad(Integer cantidad){
        this.cantidad = cantidad;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario){
        this.precioUnitario = precioUnitario;
    }

    public long getProductoId(){
        return this.productoId;
    }

    public Integer getCantidad(){
        return this.cantidad;
    }
}
