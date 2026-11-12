const cron = require("node-cron");
const config = require("./config/scraper.config");
const { createScraper } = require("./factories/scraper.factory");
const { buildCatalog, saveCatalog } = require("./services/catalog.service");
const { publishProducts } = require("./messaging/rabbitmq.publisher");

const STORES = (process.env.STORES || "SPORTLINE,LASMARGARITAS")
  .split(",").map(s => s.trim().toUpperCase()).filter(Boolean);
const CRON_EXPRESSION = process.env.SCHEDULER_CRON || "0 2 * * *";
const TIMEZONE = process.env.SCHEDULER_TIMEZONE || "America/Argentina/Buenos_Aires";

async function runStore(adapterName) {
  const store = config.stores[adapterName];
  if (!store) {
    console.error(`[Scheduler] Store no configurado: ${adapterName}`);
    return;
  }

  console.log(`[Scheduler] Iniciando scraping de ${store.nombre}`);
  try {
    const storeConfig = {
      ...config,
      storeAdapter: adapterName,
      store,
      vtex: store.vtex,
      outputFile: store.outputFile
    };
    const scraper = createScraper(adapterName, storeConfig);
    const catalog = await buildCatalog(scraper, storeConfig);
    const output = await saveCatalog(catalog, store.outputFile);
    console.log(`[Scheduler] ${store.nombre} completado. Productos: ${catalog.totalProductos}. Archivo: ${output}`);

    const published = await publishProducts(catalog.productos, storeConfig);
    console.log(`[Scheduler] ${store.nombre} publicado. Eventos confirmados: ${published}`);
  } catch (error) {
    console.error(`[Scheduler] Falló ${store.nombre}:`, error.message);
  }
}

async function runAll() {
  for (const adapterName of STORES) {
    await runStore(adapterName);
  }
}

cron.schedule(CRON_EXPRESSION, runAll, { timezone: TIMEZONE });
console.log(`[Scheduler] Programado: "${CRON_EXPRESSION}" (${TIMEZONE}). Stores: ${STORES.join(", ")}`);

if (process.argv.includes("--run-now")) {
  console.log("[Scheduler] Ejecución inmediata solicitada por --run-now");
  runAll().catch(err => console.error("[Scheduler] Error:", err));
}