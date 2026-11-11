package com.tporopa.backend.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "EquivalenciaTallesResponse", namespace = "http://tporopa.com/webropa/soap/talles")
@XmlType(name = "EquivalenciaTallesResponse", namespace = "http://tporopa.com/webropa/soap/talles",
         propOrder = {"prenda", "talleOrigen", "sistemaOrigen", "sistemaDestino", "talleDestino", "equivalencia"})
@XmlAccessorType(XmlAccessType.FIELD)
public class EquivalenciaTallesResponse {

    @XmlElement(required = true)
    private String prenda;

    @XmlElement(required = true)
    private String talleOrigen;

    @XmlElement(required = true)
    private String sistemaOrigen;

    @XmlElement(required = true)
    private String sistemaDestino;

    @XmlElement(required = true)
    private String talleDestino;

    @XmlElement(required = true)
    private String equivalencia;

    public EquivalenciaTallesResponse() {}

    public EquivalenciaTallesResponse(String prenda, String talleOrigen, String sistemaOrigen,
                                      String sistemaDestino, String talleDestino, String equivalencia) {
        this.prenda = prenda;
        this.talleOrigen = talleOrigen;
        this.sistemaOrigen = sistemaOrigen;
        this.sistemaDestino = sistemaDestino;
        this.talleDestino = talleDestino;
        this.equivalencia = equivalencia;
    }

    public String getPrenda() { return prenda; }
    public void setPrenda(String prenda) { this.prenda = prenda; }
    public String getTalleOrigen() { return talleOrigen; }
    public void setTalleOrigen(String talleOrigen) { this.talleOrigen = talleOrigen; }
    public String getSistemaOrigen() { return sistemaOrigen; }
    public void setSistemaOrigen(String sistemaOrigen) { this.sistemaOrigen = sistemaOrigen; }
    public String getSistemaDestino() { return sistemaDestino; }
    public void setSistemaDestino(String sistemaDestino) { this.sistemaDestino = sistemaDestino; }
    public String getTalleDestino() { return talleDestino; }
    public void setTalleDestino(String talleDestino) { this.talleDestino = talleDestino; }
    public String getEquivalencia() { return equivalencia; }
    public void setEquivalencia(String equivalencia) { this.equivalencia = equivalencia; }
}
