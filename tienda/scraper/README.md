# KNOWFIT - Scraper multi-tienda (Strategy)

Módulo Node.js independiente para obtener catálogos de tiendas y convertirlos al
modelo común `Producto` de KNOWFIT. Está preparado para incorporar una estrategia
por tienda sin cambiar el servicio de catálogo ni el backend.

## Estructura

```text
scraper/
├── src/
│   ├── adapters/
│   │   └── ScraperSportline.js
│   ├── config/
│   │   └── scraper.config.js
│   ├── factories/
│   │   └── scraper.factory.js
│   ├── interfaces/
│   │   └── ScraperTienda.js
│   ├── models/
│   │   ├── Producto.js
│   │   ├── Tienda.js
│   │   └── Usuario.js
│   ├── services/
│   │   └── catalog.service.js
│   ├── utils/
│   │   └── helpers.js
│   └── index.js
├── tests/
├── output/
├── .env.example
└── package.json
```

## Patrón Strategy

- `ScraperTienda` define el contrato: `fetchCatalog()` y `normalizeProduct()`.
- `ScraperSportline` implementa ese contrato usando el endpoint público de catálogo VTEX.
- `scraper.factory.js` selecciona la estrategia según `STORE_ADAPTER`.
- `catalog.service.js` trabaja contra la abstracción: no conoce VTEX ni una tienda concreta.
- `getNormalizedCatalog()` está implementado en la clase base y aplica normalización y deduplicación comunes.

En JavaScript no existe una palabra clave `interface` como en TypeScript; por eso `ScraperTienda` se implementa como clase abstracta que obliga a implementar los métodos al utilizarlos.

## Instalación y ejecución

```bash
npm install
cp .env.example .env
npm test
npm start
```

La ejecución actual utiliza `STORE_ADAPTER=SPORTLINE` y genera `output/sportline.json`.

## Agregar otra tienda

1. Crear una clase en `src/adapters/`, por ejemplo `ScraperLasMargaritas.js`.
2. Extender `ScraperTienda` e implementar `fetchCatalog()` y `normalizeProduct()`.
3. Registrar la clase en `src/factories/scraper.factory.js`.
4. Configurar la identidad, URL y parámetros propios de esa tienda.

No asumir que otra tienda utiliza VTEX: primero verificar su mecanismo real de catálogo. La nueva estrategia debe devolver los mismos campos del modelo `Producto`.

## Modelo y persistencia

Cada producto contiene `id, idExterno, nombre, descripcion, precio, categoria, genero,
marca, tienda, urlOriginal, imagenUrl, disponible, tallesDisponibles`. `id` queda en
`null` para que lo asigne PostgreSQL; `idExterno` identifica el producto en su tienda.
La combinación `tienda + idExterno` debe tratarse como identificador externo único.

El scraper genera JSON; no escribe directamente en PostgreSQL. El backend valida y
persiste los productos. `Usuario` es una entidad interna de KNOWFIT y no se obtiene
del catálogo comercial.

## Reglas de Sportline

- Consulta el catálogo público VTEX con `_from` y `_to`, en páginas de hasta 50.
- Precio: menor precio positivo entre SKUs disponibles.
- Talles: variaciones cuyo nombre contiene Talle, Tamaño o Size, de SKUs disponibles.
- Disponible: al menos un SKU con disponibilidad y cantidad mayor que cero.
- Género: inferencia simple por nombre, descripción y categorías.
- Imagen: primera imagen disponible de los SKUs.

La estructura de VTEX puede variar. Validar la respuesta real del endpoint antes de
considerar completa la extracción y respetar los términos y límites de la tienda.

## Publicación de eventos en RabbitMQ (P3 → P2)

Al finalizar la extracción y normalización, el proceso publica **un mensaje por producto** usando el contrato `ProductoScrapeadoEvent` compartido con el backend. El cuerpo JSON contiene exactamente estos campos:

`tienda`, `idExterno`, `nombre`, `precio`, `categoria`, `genero`, `marca`, `urlOriginal`, `imagenUrl`, `disponible`, `tallesDisponibles`.

No se envían `id` ni `descripcion`, porque no pertenecen al record Java indicado. `tienda` se envía como nombre legible (por defecto `STORE_NAME`); `idExterno` se convierte a texto y `precio` se serializa como número JSON, compatible con `BigDecimal` al deserializar.

### Configuración

Copiar `.env.example` a `.env` y configurar:

```env
RABBITMQ_URL=amqp://usuario:password@localhost:5672
RABBITMQ_EXCHANGE=knowfit.events
RABBITMQ_EXCHANGE_TYPE=topic
RABBITMQ_ROUTING_KEY=producto.scrapeado
STORE_EVENT_NAME=Sportline
```

El publisher crea el exchange durable si no existe, usa mensajes persistentes (`deliveryMode: 2`) y un canal de confirmación. La ejecución espera la confirmación de cada mensaje y falla con código distinto de cero si no puede publicar. La conexión se cierra al terminar.

**Contrato de infraestructura:** el backend P2 debe consumir del mismo exchange y tener una cola durable vinculada con la routing key `producto.scrapeado` (o la que se configure). Los nombres de exchange, tipo y routing key deben coincidir con `docs/eventos.md` y la configuración del consumidor del backend. Los valores anteriores son defaults configurables, no sustituyen el contrato real del despliegue.

### Ejecución

```bash
npm install
cp .env.example .env
# Iniciar RabbitMQ y ajustar RABBITMQ_URL
npm test
npm start
```

El JSON local se sigue guardando en `output/` como respaldo y, después, se publican los eventos en RabbitMQ.
