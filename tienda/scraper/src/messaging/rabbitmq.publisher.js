const amqp = require("amqplib");
const { toProductoScrapeadoEvent } = require("../events/ProductoScrapeadoEvent");

/** Publica cada Producto como un mensaje persistente y espera su confirmación. */
async function publishProducts(products, config) {
  if (!config.rabbitmq.url) {
    throw new Error("Falta RABBITMQ_URL en el archivo .env.");
  }

  const connection = await amqp.connect(config.rabbitmq.url);
  let channel;
  let published = 0;

  try {
    channel = await connection.createConfirmChannel();
    await channel.assertExchange(
      config.rabbitmq.exchange,
      config.rabbitmq.exchangeType,
      { durable: true }
    );

    for (const product of products) {
      const event = toProductoScrapeadoEvent(product, config.store.eventName);
      const body = Buffer.from(JSON.stringify(event), "utf8");

      await new Promise((resolve, reject) => {
        channel.publish(
          config.rabbitmq.exchange,
          config.rabbitmq.routingKey,
          body,
          {
            contentType: "application/json",
            contentEncoding: "utf-8",
            deliveryMode: 2,
            timestamp: Math.floor(Date.now() / 1000),
            type: "ProductoScrapeadoEvent"
          },
          error => error ? reject(error) : resolve()
        );
      });
      published += 1;
      console.log(`[RabbitMQ] Publicado ${published}/${products.length}: ${event.idExterno}`);
    }

    await channel.waitForConfirms();
    return published;
  } finally {
    if (channel) await channel.close().catch(() => {});
    await connection.close().catch(() => {});
  }
}

module.exports = { publishProducts };
