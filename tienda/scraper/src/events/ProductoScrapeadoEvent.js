/**
 * Mapea el modelo interno Producto al contrato compartido con el backend (P2).
 * Mantener estos nombres sincronizados con ProductoScrapeadoEvent.java.
 */
function toProductoScrapeadoEvent(producto, storeName) {
  if (!producto || producto.idExterno == null) {
    throw new Error("No se puede publicar un producto sin idExterno.");
  }

  return {
    tienda: String(storeName || producto.tienda || ""),
    idExterno: String(producto.idExterno),
    nombre: String(producto.nombre || ""),
    precio: Number(producto.precio || 0),
    categoria: String(producto.categoria || "Sin categoría"),
    genero: String(producto.genero || "No especificado"),
    marca: String(producto.marca || "Sin marca"),
    urlOriginal: String(producto.urlOriginal || ""),
    imagenUrl: String(producto.imagenUrl || ""),
    disponible: Boolean(producto.disponible),
    tallesDisponibles: Array.isArray(producto.tallesDisponibles)
      ? [...new Set(producto.tallesDisponibles.map(String))]
      : []
  };
}

module.exports = { toProductoScrapeadoEvent };
