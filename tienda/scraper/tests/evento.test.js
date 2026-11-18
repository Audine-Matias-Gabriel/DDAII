const test = require("node:test");
const assert = require("node:assert/strict");
const { toProductoScrapeadoEvent } = require("../src/events/ProductoScrapeadoEvent");
const { declararTopologia } = require("../src/messaging/rabbitmq.publisher");
const config = require("../src/config/scraper.config");

// Campos de tienda/backend/src/main/java/com/ddaii/events/ProductoScrapeado.java
const CAMPOS_CONTRATO = [
  "tipo", "tiendaId", "tienda", "idExterno", "nombre", "descripcion", "precio",
  "categoria", "genero", "marca", "urlOriginal", "imagenUrl", "disponible", "tallesDisponibles"
];

const producto = {
  idExterno: 2024612,
  nombre: "Remera",
  descripcion: "Remera liviana",
  precio: "25999.5",
  categoria: "REMERA",
  genero: "Hombre",
  marca: "Nike",
  urlOriginal: "https://www.sportline.com.ar/remera/p",
  imagenUrl: "https://cdn.test/remera.jpg",
  disponible: true,
  tallesDisponibles: ["M", "L", "M"]
};

test("el evento tiene todos los campos del contrato y nada más", () => {
  const evento = toProductoScrapeadoEvent(producto, { id: 1, nombre: "Sportline" });
  assert.deepEqual(Object.keys(evento).sort(), [...CAMPOS_CONTRATO].sort());
  assert.equal(evento.tipo, "ProductoScrapeado");
  assert.equal(evento.tiendaId, 1);
  assert.equal(evento.tienda, "Sportline");
  assert.equal(evento.idExterno, "2024612");
  assert.equal(evento.descripcion, "Remera liviana");
  assert.deepEqual(evento.tallesDisponibles, ["M", "L"]);
});

test("precio es number", () => {
  const evento = toProductoScrapeadoEvent(producto, { id: 1, nombre: "Sportline" });
  assert.equal(typeof evento.precio, "number");
  assert.equal(evento.precio, 25999.5);
  assert.equal(JSON.parse(JSON.stringify(evento)).precio, 25999.5);
});

test("rechaza productos sin idExterno o con precio inválido", () => {
  assert.throws(() => toProductoScrapeadoEvent({ nombre: "Sin ID" }, { id: 1, nombre: "X" }), /idExterno/);
  assert.throws(() => toProductoScrapeadoEvent({ ...producto, precio: "abc" }, { id: 1, nombre: "X" }), /Precio/);
});

test("publica en tienda.exchange y declara la cola con los argumentos del backend", async () => {
  assert.equal(config.rabbitmq.exchange, process.env.RABBITMQ_EXCHANGE || process.env.RABBIT_EXCHANGE || "tienda.exchange");
  const llamadas = [];
  const canal = new Proxy({}, { get: (_, metodo) => async (...args) => { llamadas.push([metodo, ...args]); } });

  await declararTopologia(canal, { ...config.rabbitmq, exchange: "tienda.exchange" });

  assert.deepEqual(llamadas[0], ["assertExchange", "tienda.exchange", "topic", { durable: true }]);
  assert.deepEqual(llamadas[1], [
    "assertQueue",
    "producto.scrapeado.queue",
    {
      durable: true,
      arguments: {
        "x-dead-letter-exchange": "tienda.exchange",
        "x-dead-letter-routing-key": "producto.scrapeado.dlq"
      }
    }
  ]);
  assert.deepEqual(llamadas[2], ["bindQueue", "producto.scrapeado.queue", "tienda.exchange", "producto.scrapeado"]);
});
