const ExtractorVtex = require("../extractores/ExtractorVtex");

/**
 * Factory: elige el extractor según `tienda.plataforma`.
 * Sumar una plataforma = escribir su extractor y agregar una línea acá.
 */
const EXTRACTORES = {
  VTEX: ExtractorVtex
};

function crearExtractor(tienda, config) {
  const plataforma = String(tienda?.plataforma || "").toUpperCase();
  const Clase = EXTRACTORES[plataforma];
  if (!Clase) {
    throw new Error(
      `Plataforma no soportada: "${tienda?.plataforma}" (tienda ${tienda?.clave}). ` +
        `Disponibles: ${Object.keys(EXTRACTORES).join(", ")}`
    );
  }
  return new Clase(tienda, config);
}

module.exports = { crearExtractor, EXTRACTORES };
