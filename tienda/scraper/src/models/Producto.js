/**
 * Producto final, ya normalizado. Es lo que se guarda en output/<tienda>.json
 * y lo que se convierte en el evento ProductoScrapeado.
 */
function crearProducto(datos) {
  return {
    idExterno: String(datos.idExterno),
    nombre: String(datos.nombre || ""),
    descripcion: String(datos.descripcion || ""),
    precio: Number(datos.precio),
    categoria: datos.categoria,
    genero: datos.genero,
    marca: datos.marca || "Sin marca",
    tiendaId: datos.tiendaId,
    tienda: datos.tienda,
    urlOriginal: datos.urlOriginal || "",
    imagenUrl: datos.imagenUrl || "",
    disponible: Boolean(datos.disponible),
    tallesDisponibles: Array.isArray(datos.tallesDisponibles)
      ? [...new Set(datos.tallesDisponibles.map(String))]
      : []
  };
}

module.exports = { crearProducto };
