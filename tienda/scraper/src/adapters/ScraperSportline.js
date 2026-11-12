const ScraperTienda = require("../interfaces/ScraperTienda");
const { cleanText, absoluteUrl, sleep } = require("../utils/helpers");

class ScraperSportline extends ScraperTienda {
  constructor(config) {
    super(config);
    if (config.httpClient) {
      this.http = config.httpClient;
    } else {
      const axios = require("axios");
      this.http = axios.create({
        timeout: 30000,
        headers: { Accept: "application/json", "User-Agent": "KNOWFIT-catalog-scraper/1.0" }
      });
    }
  }

  async fetchPage(from) {
    const to = from + this.config.vtex.pageSize - 1;
    const response = await this.http.get(this.config.vtex.searchUrl, {
      params: { _from: from, _to: to }
    });
    if (!Array.isArray(response.data)) {
      throw new Error("La respuesta de VTEX no es un array de productos.");
    }
    return response.data;
  }

  async fetchCatalog() {
    const all = [];
    let from = 0;
    while (all.length < this.config.vtex.maxProducts) {
      const to = from + this.config.vtex.pageSize - 1;
      console.log(`[Sportline] Consultando rango ${from}-${to}`);
      const page = await this.fetchPage(from);
      if (!page.length) break;
      all.push(...page);
      if (page.length < this.config.vtex.pageSize) break;
      from += this.config.vtex.pageSize;
      await sleep(this.config.vtex.requestDelayMs);
    }
    return all.slice(0, this.config.vtex.maxProducts);
  }

  normalizeProduct(raw) {
    if (!raw || raw.productId == null || !raw.productName || !Array.isArray(raw.items)) return null;

    const skuData = raw.items.map(item => {
      const offer = (item.sellers || [])
        .map(s => s.commertialOffer || s.commercialOffer)
        .find(Boolean);
      const sizeVariation = (item.variations || []).find(v =>
        /talle|tamaño|size/i.test(cleanText(v.name))
      );
      const availableQuantity = Number(offer?.AvailableQuantity || 0);
      const available = offer?.IsAvailable !== false && availableQuantity > 0;
      return {
        price: Number(offer?.Price ?? offer?.ListPrice ?? 0),
        available,
        sizes: available ? (sizeVariation?.values || []).map(cleanText).filter(Boolean) : [],
        images: (item.images || []).map(img => img.imageUrl).filter(Boolean)
      };
    });

    const availableSkus = skuData.filter(s => s.available);
    const prices = availableSkus.map(s => s.price).filter(p => p > 0);
    const image = skuData.flatMap(s => s.images)[0] || "";
    const categories = Array.isArray(raw.categories) ? raw.categories : [];
    const category = categories.length
      ? cleanText(categories[categories.length - 1].split("/").filter(Boolean).pop())
      : "Sin categoría";

    return {
      idExterno: String(raw.productId),
      nombre: cleanText(raw.productName),
      descripcion: cleanText(raw.description),
      precio: prices.length ? Math.min(...prices) : 0,
      categoria: category,
      genero: this.inferGender(raw),
      marca: cleanText(raw.brand) || "Sin marca",
      tienda: this.config.store.id,
      urlOriginal: raw.link ? absoluteUrl(this.config.store.url, raw.link) : this.config.store.url,
      imagenUrl: absoluteUrl(this.config.store.url, image),
      disponible: availableSkus.length > 0,
      tallesDisponibles: [...new Set(availableSkus.flatMap(s => s.sizes))]
    };
  }

  inferGender(product) {
    const text = [product.productName, product.description, ...(product.categories || [])]
      .join(" ").toLowerCase();
    if (/\b(mujer|femenin[oa]s?|women|woman|dama)\b/.test(text)) return "Mujer";
    if (/\b(hombre|masculin[oa]s?|men|man|caballero)\b/.test(text)) return "Hombre";
    if (/\bunisex\b/.test(text)) return "Unisex";
    if (/\b(niñ[oa]s?|infantil|kids|junior)\b/.test(text)) return "Infantil";
    return "No especificado";
  }

}
module.exports = ScraperSportline;

