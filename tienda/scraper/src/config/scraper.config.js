require("dotenv").config();

function storeConfig(prefix, defaults) {
  return {
    id: Number(process.env[`${prefix}_STORE_ID`] || defaults.id),
    nombre: process.env[`${prefix}_STORE_NAME`] || defaults.nombre,
    eventName: process.env[`${prefix}_EVENT_NAME`] || process.env[`${prefix}_STORE_NAME`] || defaults.nombre,
    url: (process.env[`${prefix}_STORE_URL`] || defaults.url).replace(/\/$/, ""),
    vtex: {
      searchUrl: process.env[`${prefix}_VTEX_SEARCH_URL`] || defaults.searchUrl,
      pageSize: Math.min(Math.max(Number(process.env.PAGE_SIZE || defaults.pageSize || 50), 1), 50),
      maxProducts: Math.max(Number(process.env.MAX_PRODUCTS || defaults.maxProducts || 150), 1),
      requestDelayMs: Math.max(Number(process.env.REQUEST_DELAY_MS || defaults.requestDelayMs || 700), 0)
    },
    outputFile: process.env[`${prefix}_OUTPUT_FILE`] || defaults.outputFile
  };
}

const stores = {
  SPORTLINE: storeConfig("SPORTLINE", {
    id: 1,
    nombre: "Sportline",
    url: "https://www.sportline.com.ar",
    searchUrl: "https://www.sportline.com.ar/api/catalog_system/pub/products/search",
    outputFile: "output/sportline.json"
  }),
  LASMARGARITAS: storeConfig("LASMARGARITAS", {
    id: 2,
    nombre: "Las Margaritas",
    url: "https://www.lasmargaritas.com.ar",
    searchUrl: "https://www.lasmargaritas.com.ar/api/catalog_system/pub/products/search",
    outputFile: "output/lasmargaritas.json"
  })
};

const adapter = (process.env.STORE_ADAPTER || "SPORTLINE").toUpperCase();
if (!stores[adapter]) {
  throw new Error(
    `STORE_ADAPTER no registrado: ${adapter}. Disponibles: ${Object.keys(stores).join(", ")}`
  );
}

const store = stores[adapter];

module.exports = {
  storeAdapter: adapter,
  store,
  vtex: store.vtex,
  outputFile: store.outputFile,
  stores, // todas las tiendas, para el scheduler
  rabbitmq: {
    url: process.env.RABBITMQ_URL || "",
    exchange: process.env.RABBITMQ_EXCHANGE || "knowfit.events",
    exchangeType: process.env.RABBITMQ_EXCHANGE_TYPE || "topic",
    routingKey: process.env.RABBITMQ_ROUTING_KEY || "producto.scrapeado"
  }
};