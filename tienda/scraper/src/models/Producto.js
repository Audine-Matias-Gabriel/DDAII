function createProducto(data) {
  return {
    id: null,
    idExterno: String(data.idExterno),
    nombre: data.nombre || "",
    descripcion: data.descripcion || "",
    precio: Number(data.precio || 0),
    categoria: data.categoria || "Sin categoría",
    genero: data.genero || "No especificado",
    marca: data.marca || "Sin marca",
    tienda: data.tienda,
    urlOriginal: data.urlOriginal || "",
    imagenUrl: data.imagenUrl || "",
    disponible: Boolean(data.disponible),
    tallesDisponibles: Array.isArray(data.tallesDisponibles)
      ? [...new Set(data.tallesDisponibles.map(String))]
      : []
  };
}
module.exports = { createProducto };
