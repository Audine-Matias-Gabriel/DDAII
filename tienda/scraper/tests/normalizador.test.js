const test = require("node:test");
const assert = require("node:assert/strict");
const { normalizar, evaluar } = require("../src/normalizacion/normalizador");
const { normalizarCatalogo } = require("../src/services/catalog.service");

const tienda = { clave: "SPORTLINE", id: 1, nombre: "Sportline" };

function crudo(cambios = {}) {
  return {
    idExterno: "123",
    nombre: "Remera Running Dry Fit Mujer",
    descripcion: "<p>Remera   <b>liviana</b></p>",
    marca: "Nike",
    urlOriginal: "https://www.sportline.com.ar/remera/p",
    imagenUrl: "https://cdn.test/remera.jpg",
    rutasCategoria: ["/Running/Mujer/Remeras/"],
    skus: [
      { talle: "S", precio: 30000, disponible: true },
      { talle: "M", precio: 25999, disponible: true },
      { talle: "L", precio: 19999, disponible: false },
      { talle: "M", precio: 25999, disponible: true }
    ],
    ...cambios
  };
}

test("normaliza un producto crudo", () => {
  const p = normalizar(crudo(), tienda);
  assert.equal(p.idExterno, "123");
  assert.equal(p.categoria, "REMERA");
  assert.equal(p.genero, "Mujer");
  assert.equal(p.tiendaId, 1);
  assert.equal(p.tienda, "Sportline");
  assert.equal(p.descripcion, "Remera liviana");
  assert.equal(p.disponible, true);
});

test("talles únicos, solo de los SKUs disponibles", () => {
  assert.deepEqual(normalizar(crudo(), tienda).tallesDisponibles, ["S", "M"]);
});

test("precio = mínimo de los SKUs disponibles", () => {
  assert.equal(normalizar(crudo(), tienda).precio, 25999);
});

test("sin stock: disponible false, sin talles y precio de todos los SKUs", () => {
  const p = normalizar(
    crudo({ skus: [{ talle: "M", precio: 0, disponible: false }, { talle: "L", precio: 18000, disponible: false }] }),
    tienda
  );
  assert.equal(p.disponible, false);
  assert.deepEqual(p.tallesDisponibles, []);
  assert.equal(p.precio, 18000);
});

test("descarta sin categoría", () => {
  const sinCategoria = crudo({ nombre: "Mochila Nike", rutasCategoria: ["/Accesorios/Mochilas/"] });
  assert.equal(normalizar(sinCategoria, tienda), null);
  assert.equal(evaluar(sinCategoria, tienda).motivo, "sin categoría");
});

test("descarta sin precio", () => {
  const sinPrecio = crudo({ skus: [{ talle: "M", precio: 0, disponible: true }] });
  assert.equal(normalizar(sinPrecio, tienda), null);
  assert.equal(evaluar(sinPrecio, tienda).motivo, "sin precio");
});

test("marca por defecto y descripción cortada a 1000", () => {
  const p = normalizar(crudo({ marca: "", descripcion: "x".repeat(1500) }), tienda);
  assert.equal(p.marca, "Sin marca");
  assert.equal(p.descripcion.length, 1000);
});

test("usa el generoPorDefecto de la tienda", () => {
  const p = normalizar(crudo({ nombre: "Remera básica", rutasCategoria: [] }), { ...tienda, generoPorDefecto: "Mujer" });
  assert.equal(p.genero, "Mujer");
});

test("el catálogo deduplica por idExterno y cuenta los descartes", () => {
  const { productos, descartes } = normalizarCatalogo(
    [
      crudo(),
      crudo(),
      crudo({ idExterno: "2", nombre: "Gorra", rutasCategoria: [] }),
      crudo({ idExterno: "3", skus: [] })
    ],
    tienda
  );
  assert.equal(productos.length, 1);
  assert.equal(descartes.duplicado, 1);
  assert.equal(descartes["sin categoría"], 1);
  assert.equal(descartes["sin precio"], 1);
});
