package com.ddaii.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetallePedido {
    private long id;
    private long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public BigDecimal calcularSubtotal(){
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
