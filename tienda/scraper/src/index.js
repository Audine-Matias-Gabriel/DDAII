const config = require("./config/scraper.config");
const Tienda1Adapter = require("./adapters/tienda1.adapter");
const { buildCatalog, saveCatalog } = require("./services/catalog.service");

async function main() {
  try {
    console.log("=== KNOWFIT SCRAPER: SPORTLINE ===");
    console.log(`Máximo de productos: ${config.vtex.maxProducts}`);
    const adapter = new Tienda1Adapter(config);
    const catalog = await buildCatalog(adapter, config);
    const output = await saveCatalog(catalog, config.outputFile);
    console.log(`Extracción finalizada. Productos: ${catalog.totalProductos}`);
    console.log(`Archivo: ${output}`);
  } catch (error) {
    console.error("Falló la extracción del catálogo.");
    if (error.response) {
      console.error("HTTP:", error.response.status);
      console.error(error.response.data);
    } else console.error(error.message);
    process.exitCode = 1;
  }
}
main();
