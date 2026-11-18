const { normalizarTexto } = require("./texto");
const { segmentosPorEspecificidad } = require("./categoria");

/**
 * Reglas de género en orden de prioridad, sobre texto sin tildes (niña → nina).
 * "Niño", "kids", "infantil" y "junior" sin mención de niña quedan Unisex:
 * el front solo distingue Niña.
 */
const REGLAS_GENERO = [
  { genero: "Niña", regex: /\b(nina|nena)s?\b/ },
  { genero: "Mujer", regex: /\b(mujer|mujeres|femenin[oa]s?|damas?|women|woman|wmns|wns)\b/ },
  { genero: "Hombre", regex: /\b(hombres?|masculin[oa]s?|caballeros?|men|man)\b/ },
  { genero: "Unisex", regex: /\bunisex\b/ },
  { genero: "Unisex", regex: /\b(ninos?|kids?|infantil(es)?|junior|jr)\b/ }
];

const GENEROS_VALIDOS = ["Mujer", "Hombre", "Unisex", "Niña"];

function generoDeTexto(texto) {
  const normalizado = normalizarTexto(texto);
  if (!normalizado) return null;
  const regla = REGLAS_GENERO.find(r => r.regex.test(normalizado));
  return regla ? regla.genero : null;
}

/**
 * Mujer | Hombre | Unisex | Niña. Mira el nombre, después las rutas
 * (de la más específica a la menos) y si no hay nada usa el de la tienda.
 */
function normalizarGenero(nombre, rutasCategoria = [], generoPorDefecto) {
  const porNombre = generoDeTexto(nombre);
  if (porNombre) return porNombre;

  for (const segmento of segmentosPorEspecificidad(rutasCategoria)) {
    const porRuta = generoDeTexto(segmento);
    if (porRuta) return porRuta;
  }

  return GENEROS_VALIDOS.includes(generoPorDefecto) ? generoPorDefecto : "Unisex";
}

module.exports = { REGLAS_GENERO, GENEROS_VALIDOS, normalizarGenero };
