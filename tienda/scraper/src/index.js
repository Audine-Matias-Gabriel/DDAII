const config = require("./config/scraper.config");
const { createScraper } = require("./factories/scraper.factory");
const { buildCatalog, saveCatalog } = require("./services/catalog.service");

async function main() {
  try {
    const scraper = createScraper(config.storeAdapter, config);
    console.log(`=== KNOWFIT SCRAPER: ${config.store.nombre} ===`);
    console.log(`Estrategia: ${config.storeAdapter}`);
    console.log(`Máximo de productos: ${config.vtex.maxProducts}`);

    const catalog = await buildCatalog(scraper, config);
    const output = await saveCatalog(catalog, config.outputFile);

    console.log(`Extracción finalizada. Productos: ${catalog.totalProductos}`);
    console.log(`Archivo: ${output}`);
  } catch (error) {
    console.error("Falló la extracción del catálogo.");
    if (error.response) {
      console.error("HTTP:", error.response.status);
      console.error(error.response.data);
    } else {
      console.error(error.message);
    }
    process.exitCode = 1;
  }
}

main();
