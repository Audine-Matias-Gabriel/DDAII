package com.ddaii.ximilar;

import com.ddaii.domain.Producto;

import java.util.List;

public interface XimilarFacade {

    /**
     * Indexa un producto en la colección de Ximilar.
     * No hace nada si el producto no tiene imagen.
     */
    void indexarProducto(Producto producto);

    /**
     * Devuelve hasta `limite` IDs de productos visualmente similares.
     * Recibe el producto original para que la implementación pueda filtrar
     * por tienda si lo necesita.
     */
    List<String> buscarSimilares(Producto productoOriginal, int limite);
}