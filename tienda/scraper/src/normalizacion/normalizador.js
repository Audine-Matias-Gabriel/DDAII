const { normalizarCategoria } = require("./categoria");
const { normalizarGenero } = require("./genero");
const { limpiar, quitarHtml, recortar } = require("./texto");
const { crearProducto } = require("../models/Producto");

const MAX_DESCRIPCION = 1000; // columna descripcion del backend (length = 1000)
const MAX_TEXTO = 255; // columnas varchar por defecto del backend

/** Si una URL no entra en la columna, se le saca la query (?v=...). */
function urlQueEntra(url) {
  if (!url || url.length <= MAX_TEXTO) return url || "";
  const sinQuery = url.split("?")[0];
  return sinQuery.length <= MAX_TEXTO ? sinQuery : "";
}

/**
 * Normaliza un producto crudo de cualquier extractor.
 * Devuelve { producto } o { motivo } ("sin categoría" | "sin precio" | "inválido").
 */
function evaluar(crudo, tienda) {
  if (!crudo || crudo.idExterno == null || !limpiar(crudo.nombre)) {
    return { motivo: "inválido" };
  }

  const rutas = Array.isArray(crudo.rutasCategoria) ? crudo.rutasCategoria : [];
  const categoria = normalizarCategoria(crudo.nombre, rutas);
  if (!categoria) return { motivo: "sin categoría" };

  const skus = Array.isArray(crudo.skus) ? crudo.skus : [];
  const skusDisponibles = skus.filter(s => s && s.disponible);
  const preciosDe = lista => lista.map(s => Number(s.precio)).filter(p => Number.isFinite(p) && p > 0);
  const precios = skusDisponibles.length ? preciosDe(skusDisponibles) : preciosDe(skus);
  if (!precios.length) return { motivo: "sin precio" };

  const talles = skusDisponibles.map(s => limpiar(s.talle)).filter(Boolean);

  return {
    producto: crearProducto({
      idExterno: crudo.idExterno,
      nombre: recortar(crudo.nombre, MAX_TEXTO),
      descripcion: recortar(quitarHtml(crudo.descripcion), MAX_DESCRIPCION),
      precio: Math.min(...precios),
      categoria,
      genero: normalizarGenero(crudo.nombre, rutas, tienda.generoPorDefecto),
      marca: recortar(crudo.marca, MAX_TEXTO) || "Sin marca",
      tiendaId: tienda.id,
      tienda: tienda.nombre,
      urlOriginal: urlQueEntra(crudo.urlOriginal),
      imagenUrl: urlQueEntra(crudo.imagenUrl),
      disponible: skusDisponibles.length > 0,
      tallesDisponibles: [...new Set(talles)]
    })
  };
}

/** Producto normalizado, o null si se descarta. */
function normalizar(crudo, tienda) {
  return evaluar(crudo, tienda).producto || null;
}

module.exports = { normalizar, evaluar };
