require("dotenv").config();

module.exports = {
  storeAdapter: (process.env.STORE_ADAPTER || "SPORTLINE").toUpperCase(),
  store: {
    id: Number(process.env.STORE_ID || 1),
    nombre: process.env.STORE_NAME || "Sportline",
    url: (process.env.STORE_URL || "https://www.sportline.com.ar").replace(/\/$/, "")
  },
  vtex: {
    searchUrl: process.env.VTEX_SEARCH_URL || "https://www.sportline.com.ar/api/catalog_system/pub/products/search",
    pageSize: Math.min(Math.max(Number(process.env.PAGE_SIZE || 50), 1), 50),
    maxProducts: Math.max(Number(process.env.MAX_PRODUCTS || 150), 1),
    requestDelayMs: Math.max(Number(process.env.REQUEST_DELAY_MS || 700), 0)
  },
  outputFile: process.env.OUTPUT_FILE || "output/sportline.json"
};
