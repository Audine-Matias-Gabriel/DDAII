package com.tporopa.backend.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "EquivalenciaTallesRequest", namespace = "http://tporopa.com/webropa/soap/talles")
@XmlType(name = "EquivalenciaTallesRequest", namespace = "http://tporopa.com/webropa/soap/talles",
         propOrder = {"prenda", "talleOrigen", "sistemaOrigen", "sistemaDestino"})
@XmlAccessorType(XmlAccessType.FIELD)
public class EquivalenciaTallesRequest {

    @XmlElement(required = true)
    private String prenda;

    @XmlElement(required = true)
    private String talleOrigen;

    @XmlElement(required = true)
    private String sistemaOrigen;

    @XmlElement(required = true)
    private String sistemaDestino;

    public EquivalenciaTallesRequest() {}

    public EquivalenciaTallesRequest(String prenda, String talleOrigen,
                                     String sistemaOrigen, String sistemaDestino) {
        this.prenda = prenda;
        this.talleOrigen = talleOrigen;
        this.sistemaOrigen = sistemaOrigen;
        this.sistemaDestino = sistemaDestino;
    }

    public String getPrenda() { return prenda; }
    public void setPrenda(String prenda) { this.prenda = prenda; }
    public String getTalleOrigen() { return talleOrigen; }
    public void setTalleOrigen(String talleOrigen) { this.talleOrigen = talleOrigen; }
    public String getSistemaOrigen() { return sistemaOrigen; }
    public void setSistemaOrigen(String sistemaOrigen) { this.sistemaOrigen = sistemaOrigen; }
    public String getSistemaDestino() { return sistemaDestino; }
    public void setSistemaDestino(String sistemaDestino) { this.sistemaDestino = sistemaDestino; }
}
