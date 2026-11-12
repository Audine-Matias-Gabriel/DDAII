package com.ddaii.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "EquivalenciaRequest", namespace = "http://ddaii.com/equivalencias")
@XmlType(name = "EquivalenciaRequest", namespace = "http://ddaii.com/equivalencias",
         propOrder = {"pais", "tipoPrenda", "talle"})
@XmlAccessorType(XmlAccessType.FIELD)
public class EquivalenciaRequest {

    @XmlElement(required = true)
    private String pais;

    @XmlElement(required = true)
    private String tipoPrenda;

    @XmlElement(required = true)
    private String talle;

    public EquivalenciaRequest() {}

    public EquivalenciaRequest(String pais, String tipoPrenda, String talle) {
        this.pais = pais;
        this.tipoPrenda = tipoPrenda;
        this.talle = talle;
    }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getTipoPrenda() { return tipoPrenda; }
    public void setTipoPrenda(String tipoPrenda) { this.tipoPrenda = tipoPrenda; }
    public String getTalle() { return talle; }
    public void setTalle(String talle) { this.talle = talle; }
}
