const { normalizarTexto } = require("./texto");

/**
 * Reglas de tipo de prenda, en orden de prioridad (gana la primera que matchea).
 * Las palabras van sin tildes y en singular: se comparan con límite de palabra
 * y aceptan plural (zapatilla → zapatillas, pantalon → pantalones).
 *
 * Son las categorías que filtra el frontend. Agregar una (BUZO, CAMPERA)
 * requiere sumarla también en el select del front.
 */
const REGLAS_CATEGORIA = [
  {
    categoria: "CALZADO",
    palabras: ["zapatilla", "botin", "zapato", "sandalia", "ojota", "chinela", "bota", "pantufla"]
  },
  { categoria: "VESTIDO", palabras: ["vestido", "enterito", "mono"] },
  {
    categoria: "PANTALON",
    palabras: ["pantalon", "jogger", "calza", "short", "bermuda", "jean", "babucha"]
  },
  {
    categoria: "REMERA",
    palabras: ["remera", "camiseta", "musculosa", "top", "chomba", "camisa", "polo"]
  }
];

function escapar(palabra) {
  return palabra.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

const REGLAS_COMPILADAS = REGLAS_CATEGORIA.map(regla => ({
  categoria: regla.categoria,
  regex: new RegExp(`\\b(${regla.palabras.map(escapar).join("|")})(s|es)?\\b`)
}));

/** Categoría según un texto suelto, o null. */
function categoriaDeTexto(texto) {
  const normalizado = normalizarTexto(texto);
  if (!normalizado) return null;
  const regla = REGLAS_COMPILADAS.find(r => r.regex.test(normalizado));
  return regla ? regla.categoria : null;
}

/**
 * Segmentos de las rutas de categoría de la más específica a la menos:
 * ["/Hombre/Calzado/Zapatillas/", "/Hombre/"] → ["Zapatillas", "Calzado", "Hombre"].
 */
function segmentosPorEspecificidad(rutasCategoria) {
  const rutas = (Array.isArray(rutasCategoria) ? rutasCategoria : [])
    .map(r => String(r || "").split("/").map(s => s.trim()).filter(Boolean))
    .sort((a, b) => b.length - a.length);

  const segmentos = [];
  for (const ruta of rutas) {
    for (let i = ruta.length - 1; i >= 0; i--) {
      if (!segmentos.includes(ruta[i])) segmentos.push(ruta[i]);
    }
  }
  return segmentos;
}

/**
 * REMERA | PANTALON | CALZADO | VESTIDO, o null si no es una prenda que filtra el front.
 * Primero mira el nombre; si no alcanza, las rutas de categoría.
 */
function normalizarCategoria(nombre, rutasCategoria = []) {
  const porNombre = categoriaDeTexto(nombre);
  if (porNombre) return porNombre;

  for (const segmento of segmentosPorEspecificidad(rutasCategoria)) {
    const porRuta = categoriaDeTexto(segmento);
    if (porRuta) return porRuta;
  }
  return null;
}

module.exports = { REGLAS_CATEGORIA, normalizarCategoria, segmentosPorEspecificidad };
