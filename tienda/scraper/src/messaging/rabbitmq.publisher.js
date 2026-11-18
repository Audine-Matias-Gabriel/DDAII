const { toProductoScrapeadoEvent } = require("../events/ProductoScrapeadoEvent");

const esperar = ms => new Promise(resolve => setTimeout(resolve, ms));

/** Conecta a RabbitMQ reintentando (el broker puede tardar en arrancar). */
async function conectar(rabbit, amqp) {
  let ultimoError;
  for (let intento = 1; intento <= rabbit.reintentos; intento++) {
    try {
      return await amqp.connect(rabbit.url);
    } catch (error) {
      ultimoError = error;
      console.warn(
        `[RabbitMQ] Conexión fallida (${intento}/${rabbit.reintentos}): ${error.message}`
      );
      if (intento < rabbit.reintentos) await esperar(rabbit.esperaReintentoMs);
    }
  }
  throw new Error(`No se pudo conectar a RabbitMQ: ${ultimoError?.message}`);
}

/**
 * Declara exchange, cola y DLQ con los MISMOS argumentos que AmqpConfig.java
 * del backend (si difieren, RabbitMQ responde PRECONDITION_FAILED). Así los
 * mensajes quedan en la cola aunque el backend todavía no haya arrancado.
 */
async function declararTopologia(canal, rabbit) {
  await canal.assertExchange(rabbit.exchange, "topic", { durable: true });
  await canal.assertQueue(rabbit.queue, {
    durable: true,
    arguments: {
      "x-dead-letter-exchange": rabbit.exchange,
      "x-dead-letter-routing-key": rabbit.dlqRoutingKey
    }
  });
  await canal.bindQueue(rabbit.queue, rabbit.exchange, rabbit.routingKey);
  await canal.assertQueue(rabbit.dlq, { durable: true });
  await canal.bindQueue(rabbit.dlq, rabbit.exchange, rabbit.dlqRoutingKey);
}

/**
 * Publica cada producto como ProductoScrapeado (persistente, con confirmación).
 * Devuelve la cantidad de eventos confirmados por el broker.
 */
async function publicarProductos(productos, tienda, config, { amqp = require("amqplib") } = {}) {
  const rabbit = config.rabbitmq;
  const conexion = await conectar(rabbit, amqp);
  let canal;
  try {
    canal = await conexion.createConfirmChannel();
    await declararTopologia(canal, rabbit);

    for (const producto of productos) {
      const evento = toProductoScrapeadoEvent(producto, tienda);
      canal.publish(rabbit.exchange, rabbit.routingKey, Buffer.from(JSON.stringify(evento), "utf8"), {
        contentType: "application/json",
        contentEncoding: "utf-8",
        deliveryMode: 2,
        timestamp: Math.floor(Date.now() / 1000),
        type: "ProductoScrapeado"
      });
    }
    await canal.waitForConfirms();
    return productos.length;
  } finally {
    if (canal) await canal.close().catch(() => {});
    await conexion.close().catch(() => {});
  }
}

module.exports = { publicarProductos, declararTopologia };
