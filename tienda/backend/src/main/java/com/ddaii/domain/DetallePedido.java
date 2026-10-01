package com.ddaii.domain;

import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetallePedido {
    @Id
    private long id;
    private long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public BigDecimal calcularSubtotal(){
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
