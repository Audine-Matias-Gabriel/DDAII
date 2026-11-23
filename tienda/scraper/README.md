# Scraper de WebDeRopa (Node.js)

Servicio aislado que lee catálogos reales de tiendas de ropa, los normaliza y publica
un evento `ProductoScrapeado` por producto en RabbitMQ. El resto del sistema solo
depende de ese evento: el backend lo consume y hace upsert del producto.

## Arquitectura

Tres capas, así agregar una tienda no requiere escribir código:

```
config/tiendas.js  →  extractores/ (1 por PLATAFORMA)  →  normalizacion/ (única)  →  publicador (único)
  (datos)              VTEX, Shopify...                    categoría, género,         ProductoScrapeado
                       devuelven "producto crudo"          talles, precio             → RabbitMQ
```

```
src/
  config/
    tiendas.js            # lista de tiendas (datos)
    scraper.config.js     # RabbitMQ, límites, scheduler; exporta las tiendas
  extractores/
    Extractor.js          # clase base (Strategy): extraer() → producto crudo[]
    ExtractorVtex.js      # API catalog_system de VTEX
  factories/
    extractor.factory.js  # crearExtractor(tienda) según tienda.plataforma
  normalizacion/
    texto.js              # minúsculas, sin tildes, limpiar HTML
    categoria.js          # REMERA | PANTALON | CALZADO | VESTIDO | null
    genero.js             # Mujer | Hombre | Unisex | Niña
    normalizador.js       # producto crudo → Producto, o se descarta
  models/Producto.js      # producto final (lo que se guarda y se publica)
  events/ProductoScrapeadoEvent.js
  messaging/rabbitmq.publisher.js
  services/catalog.service.js   # extraer → normalizar → deduplicar → guardar output → publicar
  index.js                # una corrida (todas las tiendas o --tienda=NEWSPORT)
  scheduler.js            # cron diario (+ --run-now)
output/                   # último catálogo de cada tienda (respaldo para la demo)
tests/                    # node --test, sin internet
```

### Producto crudo (lo que devuelve cada extractor)

```js
{
  idExterno: "123",
  nombre: "Remera Running Dry Fit Mujer",
  descripcion: "...",
  marca: "Nike",
  urlOriginal: "https://...",          // absoluta
  imagenUrl: "https://...",            // absoluta
  rutasCategoria: ["/Running/Mujer/Remeras/"],
  skus: [{ talle: "M", precio: 25999, disponible: true }, ...]
}
```

El extractor no decide categoría, género ni disponibilidad: solo ordena los datos de su plataforma.

## Patrones

**Strategy, por plataforma y no por tienda.** Todas las tiendas VTEX exponen la misma API,
así que un extractor sirve para todas.

```js
// src/extractores/Extractor.js
class Extractor {
  constructor(tienda, config = {}) {
    if (new.target === Extractor) throw new Error("Extractor es abstracta ...");
    this.tienda = tienda;
    this.config = config;
  }
  async extraer() {
    throw new Error(`${this.constructor.name} debe implementar extraer().`);
  }
}

// src/extractores/ExtractorVtex.js
class ExtractorVtex extends Extractor {
  async extraer() { /* pagina _from/_to y convierte cada producto con aCrudo() */ }
}
```

**Factory.** Elige el extractor con el campo `plataforma` de la tienda:

```js
// src/factories/extractor.factory.js
const EXTRACTORES = { VTEX: ExtractorVtex };

function crearExtractor(tienda, config) {
  const Clase = EXTRACTORES[String(tienda.plataforma).toUpperCase()];
  if (!Clase) throw new Error(`Plataforma no soportada ... Disponibles: VTEX`);
  return new Clase(tienda, config);
}
```

## Cómo agregar una tienda

- **Tienda VTEX:** una entrada en `src/config/tiendas.js`. La URL de la API se arma sola
  (`${url}/api/catalog_system/pub/products/search`).

  ```js
  { clave: "NUEVA", id: 3, nombre: "Nueva", plataforma: "VTEX", url: "https://www.nueva.com.ar", activa: true }
  // opcionales: generoPorDefecto: "Mujer", maxProductos: 200
  ```

  Y sumarla en `TIENDAS` del `docker-compose.yml` (o dejar `TIENDAS` vacío para correr todas las activas).
- **Otra plataforma (Shopify, Tiendanube...):** escribir `src/extractores/ExtractorShopify.js`
  que extienda `Extractor` y devuelva productos crudos, y agregar una línea en la factory
  (`SHOPIFY: ExtractorShopify`). La normalización y el publicador no cambian.

## Reglas de normalización

Iguales para todas las tiendas. Las categorías de una tienda son su menú (`MODA`, `RUNNING`,
`HOMBRE`), no el tipo de prenda, así que el tipo se deduce del **nombre** y, si no alcanza, de
**las rutas de categoría** (de la más específica a la menos). Se compara sin tildes ni
mayúsculas, con límite de palabra y aceptando plurales.

**Categoría** (gana la primera regla que matchea; si ninguna, el producto se descarta):

| Categoría | Palabras |
|---|---|
| `CALZADO` | zapatilla, botin, zapato, sandalia, ojota, chinela, bota, pantufla |
| `VESTIDO` | vestido, enterito, mono |
| `PANTALON` | pantalon, jogger, calza, short, bermuda, jean, babucha |
| `REMERA` | remera, camiseta, musculosa, top, chomba, camisa, polo |

**Género:** niña o nena → `Niña`; mujer, femenino, dama, women → `Mujer`; hombre, masculino,
caballero, men → `Hombre`; unisex → `Unisex`; niño, kids, infantil o junior → `Unisex`.
Si no hay nada: `generoPorDefecto` de la tienda, o `Unisex`.

**Resto:**
- `disponible`: hay al menos un SKU con stock (`IsAvailable` y `AvailableQuantity > 0`).
- `tallesDisponibles`: talles únicos de los SKUs con stock. En VTEX salen de
  `item.variations` (`["Talle"]`) y `item["Talle"]` (`["M"]`).
- `precio`: el mínimo mayor a 0 de los SKUs con stock (si no hay, de todos). Sin precio se descarta.
- `marca`: `Sin marca` si no viene. `descripcion`: sin HTML, hasta 1000 caracteres.
- Se deduplica por `idExterno`. El log de cada tienda dice cuántos se extrajeron, cuántos
  se descartaron y por qué.

## Tiendas elegidas

| Clave | Tienda | Plataforma | Qué vende |
|---|---|---|---|
| `SPORTLINE` | [Sportline](https://www.sportline.com.ar) | VTEX | Multimarca deportiva: calzado, remeras, shorts, pantalones |
| `GRID` | [Grid](https://www.grid.com.ar) | VTEX | Multimarca urbana: zapatillas, remeras, camisetas NBA, shorts |
| `NEWSPORT` | [Newsport](https://www.newsport.com.ar) | VTEX | Multimarca deportiva y urbana: calzado, ropa y accesorios |

**Cómo se verificó que son VTEX:** `https://<dominio>/api/catalog_system/pub/products/search?_from=0&_to=2`
devuelve un array JSON con `productId`, `items` (con `variations: ["Talle"]`) y `categories`.

**robots.txt (revisado el 22/11/2026):** ninguna de las tres bloquea `/api/`.
Sportline bloquea checkout y URLs con filtros (`_q=`, `map=`, `page=`, `order=`).
Grid bloquea cuenta, login, checkout, búsqueda (`/busca/`), filtros (`map=`, `page=`) y pide
`Crawl-delay: 0.5`. Newsport bloquea imágenes, cuenta, login, checkout, búsqueda
(`/busca/`), `quick-view` y `espiar`. El scraper espera 700 ms entre páginas
(`REQUEST_DELAY_MS`) y se identifica con el User-Agent `WebDeRopa-catalog-scraper/1.0`.

Las Margaritas (de la versión anterior) se sacó: es una tienda de belleza.

## Contrato con el backend

- Exchange **topic** durable `tienda.exchange`, routing key `producto.scrapeado`, cola
  `producto.scrapeado.queue` con DLQ `producto.scrapeado.dlq`.
- El publicador declara la cola con los mismos argumentos que `AmqpConfig.java`
  (`x-dead-letter-exchange`, `x-dead-letter-routing-key`), así los mensajes no se pierden si
  el backend todavía no arrancó. Si los argumentos no coinciden RabbitMQ responde `PRECONDITION_FAILED`.
- Mensajes JSON persistentes (`deliveryMode: 2`) con confirmación del broker. La conexión se
  reintenta 10 veces cada 3 segundos.
- Campos: `tipo`, `tiendaId`, `tienda`, `idExterno`, `nombre`, `descripcion`, `precio` (number),
  `categoria`, `genero`, `marca`, `urlOriginal`, `imagenUrl`, `disponible`, `tallesDisponibles`.
  El backend hace upsert por nombre de tienda + `idExterno`.

## Cómo correrlo

```bash
npm ci
npm test                                  # sin internet
cp .env.example .env                      # fuera de Docker
docker compose up -d rabbitmq             # desde la raíz del repo
npm run scrape -- --tienda=SPORTLINE      # una tienda, una vez (también NEWSPORT o GRID)
npm run scrape                            # todas las de TIENDAS (o las activas)
npm run scheduler:now                     # corre ya y después según SCHEDULER_CRON
```

En Docker (`docker compose up --build`) el contenedor corre `node src/scheduler.js --run-now`:
publica al arrancar y después todos los días a las 2:00 (`SCHEDULER_CRON=0 2 * * *`,
hora de Buenos Aires).

| Variable | Por defecto | Para qué |
|---|---|---|
| `TIENDAS` | todas las activas | Claves de `tiendas.js` separadas por coma |
| `RABBITMQ_URL` | `amqp://guest:guest@localhost:5672` | Si no está, se arma con `RABBIT_URI_HOST/PORT/USERNAME/PASSWORD` |
| `RABBITMQ_EXCHANGE` | `tienda.exchange` | También acepta `RABBIT_EXCHANGE` |
| `MAX_PRODUCTS` | 100 (300 en compose) | Productos por tienda |
| `PAGE_SIZE` | 50 | Tamaño de página (50 es el máximo de VTEX) |
| `REQUEST_DELAY_MS` | 700 | Espera entre páginas |
| `SCHEDULER_CRON` | `0 2 * * *` | Frecuencia |
| `SCHEDULER_TIMEZONE` | `America/Argentina/Buenos_Aires` | Zona horaria del cron |

**Respaldo por snapshot:** cada corrida exitosa guarda `output/<clave>.json`. Hay un archivo por
tienda (`sportline.json`, `grid.json` y `newsport.json`). Si la extracción
falla (sin internet, tienda caída) o no trae productos, se publica ese archivo y el log dice
`[<tienda>] usando snapshot`. La imagen de Docker incluye `output/`, así la demo funciona
aunque no haya internet.
