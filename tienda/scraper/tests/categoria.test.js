const test = require("node:test");
const assert = require("node:assert/strict");
const { normalizarCategoria, REGLAS_CATEGORIA } = require("../src/normalizacion/categoria");

test("una prenda por categoría, por el nombre", () => {
  assert.equal(normalizarCategoria("Zapatillas adidas Runfalcon 6 Hombre"), "CALZADO");
  assert.equal(normalizarCategoria("Botines Nike Phantom"), "CALZADO");
  assert.equal(normalizarCategoria("Vestido largo floreado"), "VESTIDO");
  assert.equal(normalizarCategoria("Calza Larga Puma Mujer"), "PANTALON");
  assert.equal(normalizarCategoria("REMERA NBA CHICAGO BULLS HOMBRE"), "REMERA");
  assert.equal(normalizarCategoria("Camiseta De Fútbol Boca Juniors"), "REMERA");
});

test("lo que no es una categoría del front devuelve null", () => {
  assert.equal(normalizarCategoria("Mochila Nike Brasilia"), null);
  assert.equal(normalizarCategoria("Gorra adidas Baseball"), null);
  assert.equal(normalizarCategoria("Mochila", ["/Accesorios/Mochilas/"]), null);
  assert.equal(normalizarCategoria(""), null);
});

test("si el nombre no alcanza, la categoría sale de la ruta más específica", () => {
  assert.equal(
    normalizarCategoria("Crocband Clog", ["/UNISEX/CALZADOS/SANDALIAS/", "/UNISEX/"]),
    "CALZADO"
  );
  assert.equal(normalizarCategoria("Air Jordan Essentials", ["/Indumentaria/Remeras/"]), "REMERA");
});

test("el nombre tiene prioridad sobre la ruta y las reglas mantienen su orden", () => {
  assert.equal(normalizarCategoria("Short running", ["/Running/Remeras/"]), "PANTALON");
  assert.equal(normalizarCategoria("Zapatilla con top de malla"), "CALZADO");
});

test("ignora tildes, mayúsculas y plurales", () => {
  assert.equal(normalizarCategoria("Pantalón cargo"), "PANTALON");
  assert.equal(normalizarCategoria("PANTALONES DE JEAN"), "PANTALON");
  assert.equal(normalizarCategoria("Ojotas Puma"), "CALZADO");
});

test("usa límites de palabra (no matchea dentro de otra palabra)", () => {
  assert.equal(normalizarCategoria("Topper Boris"), null); // "top"
  assert.equal(normalizarCategoria("Calzado"), null); // "calza"
  assert.equal(normalizarCategoria("Monograma"), null); // "mono"
});

test("las reglas se exportan como datos", () => {
  assert.deepEqual(
    REGLAS_CATEGORIA.map(r => r.categoria),
    ["CALZADO", "VESTIDO", "PANTALON", "REMERA"]
  );
  assert.ok(REGLAS_CATEGORIA.every(r => Array.isArray(r.palabras) && r.palabras.length));
});
