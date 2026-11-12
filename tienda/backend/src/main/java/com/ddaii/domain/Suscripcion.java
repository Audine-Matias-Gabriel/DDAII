package com.ddaii.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Suscripción al plan premium. Se crea al pagar con Mercado Pago (sandbox).
 */
@Entity
@Table(name = "suscripcion")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String estado;

    /** Id del pago en Mercado Pago. Queda vacío hasta que se confirma el pago. */
    private String idPago;

    /** Id de la preferencia de Checkout Pro creada al iniciar el pago. */
    private String preferenciaId;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Suscripcion() {}

    public Suscripcion(Usuario usuario, String estado, String idPago) {
        this.usuario = usuario;
        this.estado = estado;
        this.idPago = idPago;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }
    public String getPreferenciaId() { return preferenciaId; }
    public void setPreferenciaId(String preferenciaId) { this.preferenciaId = preferenciaId; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
