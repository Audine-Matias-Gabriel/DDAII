// Corre el scraper una vez: todas las tiendas activas, las de TIENDAS,
// o las de --tienda=SPORTLINE[,GRID].
const config = require("./config/scraper.config");
const { correrTiendas } = require("./services/catalog.service");

async function main() {
  const arg = process.argv.find(a => a.startsWith("--tienda="));
  const tiendas = config.seleccionarTiendas(arg ? arg.split("=")[1] : undefined);

  console.log(`=== WebDeRopa scraper: ${tiendas.map(t => t.nombre).join(", ")} ===`);
  console.log(`Exchange: ${config.rabbitmq.exchange}. Máximo por tienda: ${config.limites.maxProductos}`);

  const fallas = await correrTiendas(tiendas, config);
  if (fallas) process.exitCode = 1;
}

main().catch(error => {
  console.error(error.message);
  process.exitCode = 1;
});
