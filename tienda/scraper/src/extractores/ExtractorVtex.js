const Extractor = require("./Extractor");
const { limpiar, urlAbsoluta } = require("../normalizacion/texto");

const USER_AGENT = "WebDeRopa-catalog-scraper/1.0";
const VARIACION_TALLE = /talle|tamaño|tamano|size|numero|número/i;

/**
 * Extractor para cualquier tienda VTEX, por la API pública catalog_system:
 *   GET {url}/api/catalog_system/pub/products/search?_from=0&_to=49
 */
class ExtractorVtex extends Extractor {
  constructor(tienda, config = {}) {
    super(tienda, config);
    const limites = config.limites || {};
    this.tamanoPagina = Math.min(Math.max(Number(limites.tamanoPagina) || 50, 1), 50);
    this.maxProductos = Number(tienda.maxProductos || limites.maxProductos || 100);
    this.demoraMs = Number(limites.demoraMs ?? 700);
    this.base = String(tienda.url).replace(/\/+$/, "");
    this.urlApi = `${this.base}/api/catalog_system/pub/products/search`;

    if (config.httpClient) {
      this.http = config.httpClient;
    } else {
      const axios = require("axios");
      this.http = axios.create({
        timeout: limites.timeoutMs || 30000,
        headers: { Accept: "application/json", "User-Agent": USER_AGENT }
      });
    }
  }

  async pagina(desde, cantidad) {
    const respuesta = await this.http.get(this.urlApi, {
      params: { _from: desde, _to: desde + cantidad - 1 }
    });
    if (!Array.isArray(respuesta.data)) {
      throw new Error(`${this.prefijoLog} la respuesta de VTEX no es un array de productos.`);
    }
    return respuesta.data;
  }

  async extraer() {
    const productos = [];
    let desde = 0;

    while (productos.length < this.maxProductos) {
      const cantidad = Math.min(this.tamanoPagina, this.maxProductos - productos.length);
      console.log(`${this.prefijoLog} VTEX rango ${desde}-${desde + cantidad - 1}`);
      const pagina = await this.pagina(desde, cantidad);
      if (!pagina.length) break;
      productos.push(...pagina);
      if (pagina.length < cantidad) break;
      desde += cantidad;
      if (this.demoraMs) await this.esperar(this.demoraMs);
    }

    return productos
      .slice(0, this.maxProductos)
      .map(raw => this.aCrudo(raw))
      .filter(Boolean);
  }

  /** Producto de la API catalog_system → producto crudo. */
  aCrudo(raw) {
    if (!raw || raw.productId == null || !raw.productName) return null;
    const items = Array.isArray(raw.items) ? raw.items : [];

    const primeraImagen = items
      .flatMap(item => item.images || [])
      .map(img => img && img.imageUrl)
      .find(Boolean);
    const link = raw.link || (raw.linkText ? `/${raw.linkText}/p` : "");

    return {
      idExterno: String(raw.productId),
      nombre: limpiar(raw.productName),
      descripcion: raw.description || raw.metaTagDescription || "",
      marca: limpiar(raw.brand),
      urlOriginal: urlAbsoluta(this.base, link) || this.base,
      imagenUrl: urlAbsoluta(this.base, primeraImagen),
      rutasCategoria: Array.isArray(raw.categories) ? raw.categories : [],
      skus: items.map(item => aSku(item))
    };
  }
}

/** Un SKU por item: talle, precio y si hay stock. */
function aSku(item) {
  const oferta = ofertaDe(item);
  const cantidad = Number(oferta?.AvailableQuantity || 0);
  return {
    talle: tallesDeItem(item)[0] || null,
    precio: Number(oferta?.Price || 0),
    disponible: Boolean(oferta) && oferta.IsAvailable !== false && cantidad > 0
  };
}

function ofertaDe(item) {
  const sellers = Array.isArray(item?.sellers) ? item.sellers : [];
  const vendedor = sellers.find(s => s && s.sellerDefault) || sellers[0];
  const candidatos = [vendedor, ...sellers].filter(Boolean);
  for (const s of candidatos) {
    const oferta = s.commertialOffer || s.commercialOffer;
    if (oferta) return oferta;
  }
  return null;
}

/**
 * En catalog_system `item.variations` es un array de strings (["Talle"]) y los
 * valores están en item["Talle"] (["M"]). Se deja el formato { name, values }
 * como respaldo.
 */
function tallesDeItem(item) {
  const talles = [];
  for (const variacion of item?.variations || []) {
    if (typeof variacion === "string") {
      if (VARIACION_TALLE.test(variacion) && Array.isArray(item[variacion])) {
        talles.push(...item[variacion]);
      }
    } else if (variacion && VARIACION_TALLE.test(variacion.name || "")) {
      talles.push(...(variacion.values || []));
    }
  }
  return [...new Set(talles.map(limpiar).filter(Boolean))];
}

ExtractorVtex.tallesDeItem = tallesDeItem;
module.exports = ExtractorVtex;
