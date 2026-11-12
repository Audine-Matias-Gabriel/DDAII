const fs = require("fs/promises");
const path = require("path");
const { createTienda } = require("../models/Tienda");
const { createProducto } = require("../models/Producto");

async function buildCatalog(adapter, config) {
  const tienda = createTienda(config.store);
  const products = await adapter.getNormalizedCatalog();
  return {
    generadoEn: new Date().toISOString(),
    tienda,
    totalProductos: products.length,
    productos: products.map(createProducto)
  };
}
async function saveCatalog(catalog, outputFile) {
  const target = path.resolve(process.cwd(), outputFile);
  await fs.mkdir(path.dirname(target), { recursive: true });
  await fs.writeFile(target, JSON.stringify(catalog, null, 2), "utf8");
  return target;
}
module.exports = { buildCatalog, saveCatalog };
