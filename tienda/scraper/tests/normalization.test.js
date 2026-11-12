const test = require("node:test");
const assert = require("node:assert/strict");
const { createProducto } = require("../src/models/Producto");

test("normaliza el contrato Producto de KNOWFIT", () => {
  const p = createProducto({
    idExterno: 123, nombre: "Zapatilla", precio: 100,
    tienda: 1, disponible: true, tallesDisponibles: ["40", "41"]
  });
  assert.equal(p.id, null);
  assert.equal(p.idExterno, "123");
  assert.equal(p.precio, 100);
  assert.equal(p.tienda, 1);
  assert.deepEqual(p.tallesDisponibles, ["40", "41"]);
});
