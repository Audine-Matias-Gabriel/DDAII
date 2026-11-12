package com.ddaii.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "EquivalenciaResponse", namespace = "http://ddaii.com/equivalencias")
@XmlType(name = "EquivalenciaResponse", namespace = "http://ddaii.com/equivalencias",
         propOrder = {"pais", "tipoPrenda", "talle", "equivalencias"})
@XmlAccessorType(XmlAccessType.FIELD)
public class EquivalenciaResponse {

    @XmlElement(required = true)
    private String pais;

    @XmlElement(required = true)
    private String tipoPrenda;

    @XmlElement(required = true)
    private String talle;

    @XmlElement(name = "equivalencias")
    private Equivalencias equivalencias = new Equivalencias();

    public EquivalenciaResponse() {}

    public EquivalenciaResponse(String pais, String tipoPrenda, String talle, List<Equivalencia> equivalencias) {
        this.pais = pais;
        this.tipoPrenda = tipoPrenda;
        this.talle = talle;
        this.equivalencias = new Equivalencias(equivalencias);
    }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getTipoPrenda() { return tipoPrenda; }
    public void setTipoPrenda(String tipoPrenda) { this.tipoPrenda = tipoPrenda; }
    public String getTalle() { return talle; }
    public void setTalle(String talle) { this.talle = talle; }
    public Equivalencias getEquivalencias() { return equivalencias; }
    public void setEquivalencias(Equivalencias equivalencias) { this.equivalencias = equivalencias; }

    @XmlType(name = "Equivalencias", namespace = "http://ddaii.com/equivalencias",
             propOrder = {"equivalencia"})
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Equivalencias {

        @XmlElement(name = "equivalencia")
        private List<Equivalencia> equivalencia = new ArrayList<>();

        public Equivalencias() {}

        public Equivalencias(List<Equivalencia> equivalencia) {
            this.equivalencia = equivalencia != null ? equivalencia : new ArrayList<>();
        }

        public List<Equivalencia> getEquivalencia() { return equivalencia; }
        public void setEquivalencia(List<Equivalencia> equivalencia) { this.equivalencia = equivalencia; }
    }

    @XmlType(name = "Equivalencia", namespace = "http://ddaii.com/equivalencias",
             propOrder = {"paisDestino", "talle"})
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Equivalencia {

        @XmlElement(required = true)
        private String paisDestino;

        @XmlElement(required = true)
        private String talle;

        public Equivalencia() {}

        public Equivalencia(String paisDestino, String talle) {
            this.paisDestino = paisDestino;
            this.talle = talle;
        }

        public String getPaisDestino() { return paisDestino; }
        public void setPaisDestino(String paisDestino) { this.paisDestino = paisDestino; }
        public String getTalle() { return talle; }
        public void setTalle(String talle) { this.talle = talle; }
    }
}
