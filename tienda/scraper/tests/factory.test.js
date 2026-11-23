const test = require("node:test");
const assert = require("node:assert/strict");
const { crearExtractor } = require("../src/factories/extractor.factory");
const ExtractorVtex = require("../src/extractores/ExtractorVtex");
const Extractor = require("../src/extractores/Extractor");
const { tiendas, seleccionarTiendas } = require("../src/config/scraper.config");

test("VTEX devuelve un ExtractorVtex", () => {
  const extractor = crearExtractor({ clave: "X", nombre: "X", plataforma: "VTEX", url: "https://x.test" }, { httpClient: {} });
  assert.ok(extractor instanceof ExtractorVtex);
  assert.ok(extractor instanceof Extractor);
});

test("una plataforma inexistente tira un error con las disponibles", () => {
  assert.throws(
    () => crearExtractor({ clave: "X", plataforma: "SHOPIFY", url: "https://x.test" }, {}),
    /Plataforma no soportada.*Disponibles: VTEX/
  );
});

test("todas las tiendas configuradas tienen un extractor", () => {
  for (const tienda of tiendas) {
    assert.ok(crearExtractor(tienda, { httpClient: {} }) instanceof Extractor, tienda.clave);
  }
  assert.equal(new Set(tiendas.map(t => t.clave)).size, tiendas.length);
  assert.equal(new Set(tiendas.map(t => t.id)).size, tiendas.length);
});

test("Newsport está configurada como tienda VTEX activa", () => {
  const tienda = tiendas.find(t => t.clave === "NEWSPORT");
  assert.ok(tienda, "falta la entrada NEWSPORT en tiendas.js");
  assert.equal(tienda.id, 3);
  assert.equal(tienda.nombre, "Newsport");
  assert.equal(tienda.plataforma, "VTEX");
  assert.equal(tienda.url, "https://www.newsport.com.ar");
  assert.equal(tienda.activa, true);
  assert.ok(crearExtractor(tienda, { httpClient: {} }) instanceof ExtractorVtex);
});

test("TIENDAS elige qué tiendas corren", () => {
  assert.deepEqual(seleccionarTiendas("sportline").map(t => t.clave), ["SPORTLINE"]);
  assert.deepEqual(seleccionarTiendas("newsport").map(t => t.clave), ["NEWSPORT"]);
  assert.deepEqual(seleccionarTiendas("").map(t => t.clave), tiendas.filter(t => t.activa).map(t => t.clave));
  assert.throws(() => seleccionarTiendas("LASMARGARITAS"), /Tienda no configurada/);
});
