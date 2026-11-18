/**
 * Producto normalizado → evento ProductoScrapeado.
 * Contrato: tienda/backend/src/main/java/com/ddaii/events/ProductoScrapeado.java
 * (mismos nombres de campo; el backend hace upsert por nombre de tienda + idExterno).
 */
function toProductoScrapeadoEvent(producto, tienda) {
  if (!producto || producto.idExterno == null) {
    throw new Error("No se puede publicar un producto sin idExterno.");
  }
  const precio = Number(producto.precio);
  if (!Number.isFinite(precio)) {
    throw new Error(`Precio inválido en ${producto.idExterno}: ${producto.precio}`);
  }

  return {
    tipo: "ProductoScrapeado",
    tiendaId: Number(tienda?.id ?? producto.tiendaId),
    tienda: String(tienda?.nombre ?? producto.tienda ?? ""),
    idExterno: String(producto.idExterno),
    nombre: String(producto.nombre || ""),
    descripcion: String(producto.descripcion || ""),
    precio,
    categoria: String(producto.categoria || ""),
    genero: String(producto.genero || "Unisex"),
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
