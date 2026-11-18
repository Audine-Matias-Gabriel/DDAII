const test = require("node:test");
const assert = require("node:assert/strict");
const { normalizarGenero } = require("../src/normalizacion/genero");

test("Mujer, Hombre y Unisex por el nombre", () => {
  assert.equal(normalizarGenero("Zapatillas Puma Rebound Mujer"), "Mujer");
  assert.equal(normalizarGenero("Remera Dama Básica"), "Mujer");
  assert.equal(normalizarGenero("Remera Topper Hombre"), "Hombre");
  assert.equal(normalizarGenero("Buzo Caballero"), "Hombre");
  assert.equal(normalizarGenero("Sandalias Crocs Crocband Unisex"), "Unisex");
});

test("niña y nena → Niña", () => {
  assert.equal(normalizarGenero("Vestido Niña Estampado"), "Niña");
  assert.equal(normalizarGenero("Calza nena"), "Niña");
});

test("niño, kids, infantil o junior sin mención de niña → Unisex", () => {
  assert.equal(normalizarGenero("Zapatillas Atomik Japan Niño"), "Unisex");
  assert.equal(normalizarGenero("Remera Kids"), "Unisex");
  assert.equal(normalizarGenero("Short infantil"), "Unisex");
});

test("si el nombre no dice nada, mira las rutas", () => {
  assert.equal(normalizarGenero("Zapatillas Runfalcon", ["/HOMBRE/CALZADOS/ZAPATILLAS/"]), "Hombre");
  assert.equal(normalizarGenero("Ojotas Cool Cat", ["/MUJER/CALZADOS/OJOTAS/"]), "Mujer");
  assert.equal(normalizarGenero("Zapatillas Japan", ["/NIÑOS/CALZADOS/ZAPATILLAS/"]), "Unisex");
});

test("sin datos usa el generoPorDefecto de la tienda, o Unisex", () => {
  assert.equal(normalizarGenero("Remera básica", [], "Mujer"), "Mujer");
  assert.equal(normalizarGenero("Remera básica", []), "Unisex");
  assert.equal(normalizarGenero("Remera básica", [], "No especificado"), "Unisex");
});
