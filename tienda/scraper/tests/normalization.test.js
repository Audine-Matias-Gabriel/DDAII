const test = require("node:test");
const assert = require("node:assert/strict");
const { createProducto } = require("../src/models/Producto");
const ScraperTienda = require("../src/interfaces/ScraperTienda");
const ScraperSportline = require("../src/adapters/ScraperSportline");
const { createScraper } = require("../src/factories/scraper.factory");

const config = {
  store: { id: 1, nombre: "Sportline", url: "https://www.sportline.com.ar" },
  httpClient: {},
  vtex: {
    searchUrl: "https://www.sportline.com.ar/api/catalog_system/pub/products/search",
    pageSize: 50,
    maxProducts: 150,
    requestDelayMs: 0
  }
};

test("ScraperTienda no se puede instanciar directamente", () => {
  assert.throws(() => new ScraperTienda(config), /no puede instanciarse/);
});

test("la factory crea la estrategia Sportline", () => {
  const scraper = createScraper("SPORTLINE", config);
  assert.ok(scraper instanceof ScraperSportline);
  assert.ok(scraper instanceof ScraperTienda);
});

test("la estrategia Sportline normaliza un producto VTEX", () => {
  const scraper = new ScraperSportline(config);
  const raw = {
    productId: "2024612",
    productName: "Remera deportiva hombre",
    description: "Remera de entrenamiento",
    brand: "Marca X",
    categories: ["/Indumentaria/Hombre/Remeras/"],
    link: "/remera-deportiva/p",
    items: [{
      variations: [{ name: "Talle", values: ["M", "L"] }],
      images: [{ imageUrl: "https://cdn.example.com/remera.jpg" }],
      sellers: [{ commertialOffer: { Price: 25999, AvailableQuantity: 3, IsAvailable: true } }]
    }]
  };
  const product = scraper.normalizeProduct(raw);
  assert.equal(product.idExterno, "2024612");
  assert.equal(product.tienda, 1);
  assert.equal(product.precio, 25999);
  assert.equal(product.disponible, true);
  assert.deepEqual(product.tallesDisponibles, ["M", "L"]);
  assert.equal(product.urlOriginal, "https://www.sportline.com.ar/remera-deportiva/p");
});

test("el modelo común Producto conserva el contrato de KNOWFIT", () => {
  const product = createProducto({
    idExterno: 123, nombre: "Zapatilla", precio: 100,
    tienda: 1, disponible: true, tallesDisponibles: ["40", "41"]
  });
  assert.equal(product.id, null);
  assert.equal(product.idExterno, "123");
  assert.equal(product.precio, 100);
  assert.equal(product.tienda, 1);
  assert.deepEqual(product.tallesDisponibles, ["40", "41"]);
});

test("la factory informa estrategias no registradas", () => {
  assert.throws(() => createScraper("DESCONOCIDA", config), /Scraper no registrado/);
});

const { toProductoScrapeadoEvent } = require("../src/events/ProductoScrapeadoEvent");

test("el evento ProductoScrapeado respeta exactamente el contrato del backend", () => {
  const event = toProductoScrapeadoEvent({
    id: null, idExterno: 2024612, nombre: "Remera", descripcion: "No se publica",
    precio: 25999.5, categoria: "Remeras", genero: "Hombre", marca: "Marca X",
    tienda: 1, urlOriginal: "https://store.test/p", imagenUrl: "https://cdn.test/p.jpg",
    disponible: true, tallesDisponibles: ["M", "L", "M"]
  }, "Sportline");

  assert.deepEqual(Object.keys(event), [
    "tienda", "idExterno", "nombre", "precio", "categoria", "genero", "marca",
    "urlOriginal", "imagenUrl", "disponible", "tallesDisponibles"
  ]);
  assert.equal(event.tienda, "Sportline");
  assert.equal(event.idExterno, "2024612");
  assert.equal(event.precio, 25999.5);
  assert.deepEqual(event.tallesDisponibles, ["M", "L"]);
  assert.equal("descripcion" in event, false);
  assert.equal("id" in event, false);
});

test("el evento rechaza productos sin idExterno", () => {
  assert.throws(() => toProductoScrapeadoEvent({ nombre: "Sin ID" }, "Sportline"), /idExterno/);
});
