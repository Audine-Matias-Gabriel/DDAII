/**
 * Contrato común para los scrapers de KNOWFIT.
 *
 * Strategy: cada tienda concreta implementa fetchCatalog() y normalizeProduct().
 * Esta clase concentra el flujo compartido de normalización y deduplicación.
 */
class ScraperTienda {
  constructor(config) {
    if (new.target === ScraperTienda) {
      throw new Error("ScraperTienda es una interfaz/estrategia abstracta y no puede instanciarse.");
    }
    this.config = config;
  }

  async fetchCatalog() {
    throw new Error(`${this.constructor.name} debe implementar fetchCatalog().`);
  }

  normalizeProduct(_rawProduct) {
    throw new Error(`${this.constructor.name} debe implementar normalizeProduct().`);
  }

  async getNormalizedCatalog() {
    const rawProducts = await this.fetchCatalog();
    const uniqueProducts = new Map();

    for (const rawProduct of rawProducts) {
      const product = this.normalizeProduct(rawProduct);
      if (product && product.idExterno != null) {
        uniqueProducts.set(String(product.idExterno), product);
      }
    }

    return [...uniqueProducts.values()];
  }
}

module.exports = ScraperTienda;
