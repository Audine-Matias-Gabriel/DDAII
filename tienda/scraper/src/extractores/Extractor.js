/**
 * Strategy: un extractor por PLATAFORMA (VTEX, Shopify...), no por tienda.
 *
 * extraer() devuelve "productos crudos":
 *   {
 *     idExterno, nombre, descripcion, marca,
 *     urlOriginal, imagenUrl,          // absolutas
 *     rutasCategoria: ["/Running/Mujer/Remeras/"],
 *     skus: [{ talle: "M", precio: 25999, disponible: true }]
 *   }
 * El extractor no decide categoría, género ni disponibilidad: eso lo hace
 * src/normalizacion, igual para todas las plataformas.
 */
class Extractor {
  constructor(tienda, config = {}) {
    if (new.target === Extractor) {
      throw new Error("Extractor es abstracta: usá un extractor de plataforma (ej. ExtractorVtex).");
    }
    this.tienda = tienda;
    this.config = config;
  }

  get prefijoLog() {
    return `[${this.tienda.nombre}]`;
  }

  async extraer() {
    throw new Error(`${this.constructor.name} debe implementar extraer().`);
  }

  esperar(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

module.exports = Extractor;
