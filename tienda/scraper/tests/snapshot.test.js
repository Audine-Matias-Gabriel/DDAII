const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("fs/promises");
const os = require("os");
const path = require("path");
const { correrTienda, guardarSnapshot } = require("../src/services/catalog.service");

const tienda = { clave: "PRUEBA", id: 9, nombre: "Prueba", plataforma: "VTEX", url: "https://x.test" };

const crudo = {
  idExterno: "1",
  nombre: "Remera Hombre",
  rutasCategoria: [],
  skus: [{ talle: "M", precio: 100, disponible: true }]
};

async function dirTemporal() {
  return fs.mkdtemp(path.join(os.tmpdir(), "scraper-"));
}

test("si la extracción anda, guarda output/<clave>.json y publica", async () => {
  const dirSalida = await dirTemporal();
  const publicados = [];
  const resumen = await correrTienda(tienda, {}, {
    dirSalida,
    crearExtractor: () => ({ extraer: async () => [crudo] }),
    publicar: async productos => { publicados.push(...productos); return productos.length; }
  });

  assert.equal(resumen.origen, "api");
  assert.equal(resumen.confirmados, 1);
  assert.equal(publicados[0].categoria, "REMERA");
  const guardado = JSON.parse(await fs.readFile(path.join(dirSalida, "prueba.json"), "utf8"));
  assert.equal(guardado.totalProductos, 1);
});

test("si la extracción falla, publica el último snapshot", async () => {
  const dirSalida = await dirTemporal();
  await guardarSnapshot(tienda, [{ idExterno: "viejo", nombre: "Remera", precio: 50, categoria: "REMERA" }], dirSalida);

  const resumen = await correrTienda(tienda, {}, {
    dirSalida,
    crearExtractor: () => ({ extraer: async () => { throw new Error("getaddrinfo ENOTFOUND"); } }),
    publicar: async productos => productos.length
  });

  assert.equal(resumen.origen, "snapshot");
  assert.equal(resumen.confirmados, 1);
  assert.equal(resumen.productos[0].idExterno, "viejo");
});

test("sin extracción ni snapshot, falla", async () => {
  const dirSalida = await dirTemporal();
  await assert.rejects(
    correrTienda(tienda, {}, {
      dirSalida,
      crearExtractor: () => ({ extraer: async () => { throw new Error("caída"); } }),
      publicar: async () => 0
    }),
    /ENOENT/
  );
});
