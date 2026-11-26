package com.ddaii.services;

import com.ddaii.domain.Producto;

import java.util.Map;

/**
 * Resumen de lo que le gusta a un usuario premium: marcas y categorías con un peso (1 a 5) según
 * cuántas vistas y favoritos acumuló. El recomendador lo usa como bonificación, no como reemplazo
 * del parecido por contenido: así no aparece cualquier producto de esa marca sin más.
 */
public class PerfilInteres {

    private static final PerfilInteres VACIO =
            new PerfilInteres(Map.of(), Map.of(), 0.0, 0.0);

    private final Map<String, Double> marcas;
    private final Map<String, Double> categorias;
    private final double bonoMarca;
    private final double bonoCategoria;

    public PerfilInteres(Map<String, Double> marcas, Map<String, Double> categorias,
                         double bonoMarca, double bonoCategoria) {
        this.marcas = marcas;
        this.categorias = categorias;
        this.bonoMarca = bonoMarca;
        this.bonoCategoria = bonoCategoria;
    }

    public static PerfilInteres vacio() { return VACIO; }

    /** Bonus 0..1 para una prenda, según coincida con marcas/categorías del perfil. */
    public double bono(Producto producto) {
        if (producto == null) return 0.0;
        double marca = peso(marcas, producto.getMarca()) * bonoMarca;
        double categoria = peso(categorias, producto.getCategoria()) * bonoCategoria;
        return Math.round((marca + categoria) * 100.0) / 100.0;
    }

    public boolean tieneInteres() {
        return !marcas.isEmpty() || !categorias.isEmpty();
    }

    /** Normaliza la clave para ignorar mayúsculas y espacios (igual que RecomendacionService.igual). */
    public static String clave(String valor) {
        return valor == null ? null : valor.trim().toUpperCase();
    }

    private double peso(Map<String, Double> pesos, String valor) {
        String clave = clave(valor);
        if (clave == null || clave.isEmpty()) return 0.0;
        Double peso = pesos.get(clave);
        return peso == null ? 0.0 : Math.min(1.0, Math.max(0.0, peso));
    }
}
