package com.ddaii.domain;

import jakarta.persistence.Entity;
import lombok.Data;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Pedido{

    @Id
    private Long id;
    private long clientId;
    private long tiendaId;
    private LocalDateTime fecha;
    private EstadoPedido estadoPedido;
    private BigDecimal total;

    private List<DetallePedido> detalles = new ArrayList<>();

    public void agregarDetalle(DetallePedido detalle){
        detalles.add(detalle);
    }

    public List<DetallePedido> getDetalles(){
        return detalles;
    }

    public long getId(){
        return this.id;
    }

    public void calcularTotal(){}

    public void setEstado(String hecho){}



}
