require("dotenv").config();
const path = require("path");
const tiendas = require("./tiendas");

function numero(valor, porDefecto, { min = 0, max = Infinity } = {}) {
  const n = Number(valor);
  if (valor === undefined || valor === "" || Number.isNaN(n)) return porDefecto;
  return Math.min(Math.max(n, min), max);
}

function urlRabbit(env) {
  if (env.RABBITMQ_URL) return env.RABBITMQ_URL;
  if (env.RABBIT_URI_HOST) {
    const usuario = encodeURIComponent(env.RABBIT_URI_USERNAME || "guest");
    const clave = encodeURIComponent(env.RABBIT_URI_PASSWORD || "guest");
    return `amqp://${usuario}:${clave}@${env.RABBIT_URI_HOST}:${env.RABBIT_URI_PORT || 5672}`;
  }
  return "amqp://guest:guest@localhost:5672";
}

/**
 * Tiendas que corren: las de `lista` (o TIENDAS) si se pasan, si no las activas.
 * Tira error si se pide una clave que no existe.
 */
function seleccionarTiendas(lista = process.env.TIENDAS) {
  const claves = String(lista || "")
    .split(",")
    .map(c => c.trim().toUpperCase())
    .filter(Boolean);

  if (!claves.length) return tiendas.filter(t => t.activa);

  return claves.map(clave => {
    const tienda = tiendas.find(t => t.clave === clave);
    if (!tienda) {
      throw new Error(
        `Tienda no configurada: ${clave}. Disponibles: ${tiendas.map(t => t.clave).join(", ")}`
      );
    }
    return tienda;
  });
}

module.exports = {
  tiendas,
  seleccionarTiendas,
  rabbitmq: {
    url: urlRabbit(process.env),
    exchange: process.env.RABBITMQ_EXCHANGE || process.env.RABBIT_EXCHANGE || "tienda.exchange",
    routingKey: "producto.scrapeado",
    queue: "producto.scrapeado.queue",
    dlq: "producto.scrapeado.dlq",
    dlqRoutingKey: "producto.scrapeado.dlq",
    reintentos: 10,
    esperaReintentoMs: 3000
  },
  limites: {
    maxProductos: numero(process.env.MAX_PRODUCTS, 100, { min: 1 }),
    tamanoPagina: numero(process.env.PAGE_SIZE, 50, { min: 1, max: 50 }), // 50 es el máximo de VTEX
    demoraMs: numero(process.env.REQUEST_DELAY_MS, 700, { min: 0 }),
    timeoutMs: 30000
  },
  scheduler: {
    cron: process.env.SCHEDULER_CRON || "0 2 * * *",
    timezone: process.env.SCHEDULER_TIMEZONE || "America/Argentina/Buenos_Aires"
  },
  dirSalida: path.resolve(__dirname, "..", "..", "output")
};
