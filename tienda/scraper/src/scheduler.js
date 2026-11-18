// Corre el scraper según SCHEDULER_CRON (por defecto todos los días a las 2:00).
// Con --run-now además corre una vez al arrancar.
const cron = require("node-cron");
const config = require("./config/scraper.config");
const { correrTiendas } = require("./services/catalog.service");

const tiendas = config.seleccionarTiendas();
const { cron: expresion, timezone } = config.scheduler;

if (!cron.validate(expresion)) {
  console.error(`[Scheduler] SCHEDULER_CRON inválido: "${expresion}"`);
  process.exit(1);
}

let corriendo = false;
async function correr(motivo) {
  if (corriendo) {
    console.warn(`[Scheduler] Se saltea la corrida (${motivo}): la anterior sigue en curso.`);
    return;
  }
  corriendo = true;
  console.log(`[Scheduler] Inicio (${motivo}): ${tiendas.map(t => t.nombre).join(", ")}`);
  try {
    await correrTiendas(tiendas, config);
  } catch (error) {
    console.error("[Scheduler] Error:", error.message);
  } finally {
    corriendo = false;
    console.log("[Scheduler] Fin");
  }
}

cron.schedule(expresion, () => correr("cron"), { timezone });
console.log(`[Scheduler] Programado "${expresion}" (${timezone}). Tiendas: ${tiendas.map(t => t.clave).join(", ")}`);

if (process.argv.includes("--run-now")) {
  correr("--run-now");
}
