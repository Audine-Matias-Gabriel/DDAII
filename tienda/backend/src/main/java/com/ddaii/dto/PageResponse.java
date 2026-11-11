package com.ddaii.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Respuesta paginada con los metadatos del contrato REST (docs/PLAN-INTEGRACION.md 2.1).
 */
public class PageResponse<T> {

    private List<T> contenido;
    private int pagina;
    private int tamano;
    private long total;

    public PageResponse() {}

    public PageResponse(List<T> contenido, int pagina, int tamano, long total) {
        this.contenido = contenido;
        this.pagina = pagina;
        this.tamano = tamano;
        this.total = total;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    public List<T> getContenido() { return contenido; }
    public void setContenido(List<T> contenido) { this.contenido = contenido; }
    public int getPagina() { return pagina; }
    public void setPagina(int pagina) { this.pagina = pagina; }
    public int getTamano() { return tamano; }
    public void setTamano(int tamano) { this.tamano = tamano; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
