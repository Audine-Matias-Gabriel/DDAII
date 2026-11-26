# Contrato de eventos (RabbitMQ)

Fuente única de verdad para los mensajes entre servicios. Si cambiás un campo, una routing key
o una cola, actualizá este archivo **y `EventosContratoTest` en el mismo PR**, y avisá al grupo.

La topología la declara el backend en `tienda/backend/.../messaging/AmqpConfig.java`. El scraper
declara la cola de `ProductoScrapeado` con los mismos argumentos, así los mensajes no se pierden si
el backend todavía no arrancó (`tienda/scraper/src/messaging/rabbitmq.publisher.js`).

## Convenciones generales

| Concepto | Valor |
|---|---|
| Exchange | `tienda.exchange` (tipo `topic`, durable). Variable `RABBIT_EXCHANGE` / `RABBITMQ_EXCHANGE` |
| Cola | `<entidad>.<accion>.queue`, durable |
| DLQ | `<entidad>.<accion>.dlq`, enlazada al **mismo exchange** con routing key `<entidad>.<accion>.dlq` |
| Mensajes muertos | Cada cola tiene `x-dead-letter-exchange = tienda.exchange` y `x-dead-letter-routing-key = <entidad>.<accion>.dlq` |
| Routing key | `<entidad>.<accion-en-participio>` (ej. `producto.scrapeado`) |
| Cuerpo | JSON UTF-8, `content_type = application/json`. **No** hace falta el header `__TypeId__`: el backend infiere el tipo del parámetro del listener (`TypePrecedence.INFERRED`) |
| Persistencia | Mensajes persistentes (`deliveryMode = 2`) y confirmación del broker (`publisher-confirm-type: correlated`; el scraper usa un canal con confirmaciones) |
| Fechas | ISO-8601 sin zona (`2026-11-18T22:38:52.884`) |
| Precios | Número en pesos argentinos (ARS) |
| `tipo` | Cada mensaje incluye el nombre del evento (`"tipo": "ProductoScrapeado"`), para leerlo en el panel |

**Reglas para consumidores** (`spring.rabbitmq.listener.simple` en `application.yml`)

- Si el consumidor falla, se reintenta **3 veces** (`retry.max-attempts: 3`) y después el mensaje se
  rechaza **sin reencolar** (`default-requeue-rejected: false`): termina en la DLQ de su cola. Así un
  mensaje roto no bloquea la cola ni se reintenta para siempre.
- Un JSON que no se puede leer va directo a la DLQ.
- El procesamiento es **idempotente**: el mismo evento puede llegar más de una vez.
- Los campos desconocidos se ignoran, así se pueden agregar campos sin romper a nadie.

## Resumen

| Evento | Publica | Consume | Routing key | Cola | DLQ |
|---|---|---|---|---|---|
| ProductoScrapeado | scraper Node (P3) | backend (P2) | `producto.scrapeado` | `producto.scrapeado.queue` | `producto.scrapeado.dlq` |
| SuscripcionActivada | backend (P5) | backend (P5) | `suscripcion.activada` | `suscripcion.activada.queue` | `suscripcion.activada.dlq` |
| RecomendacionSolicitada | backend (P4) | backend (P4) | `recomendacion.solicitada` | `recomendacion.solicitada.queue` | `recomendacion.solicitada.dlq` |

```
scraper ──ProductoScrapeado──►┐
                              │  tienda.exchange (topic)
backend ──SuscripcionActivada─┤──► producto.scrapeado.queue ──► backend: upsert del producto
backend ──RecomendacionSolic.─┘──► suscripcion.activada.queue ──► backend: marca premium
                                 ──► recomendacion.solicitada.queue ──► backend: calcula y guarda
                     (si falla 3 veces) ──► <cola>.dlq
```

---

## ProductoScrapeado

- **Publica:** scraper Node (P3) · **Consume:** backend (P2), `RabbitListeners.consumirProductoScrapeado`
- **Clase:** `com.ddaii.events.ProductoScrapeado` · en el scraper: `src/events/ProductoScrapeadoEvent.js`
- **Cuándo:** al arrancar el scraper y después todos los días a las 2:00, uno por producto válido.
- **Efecto:** upsert del producto. La tienda se busca por **nombre** (si no existe, se crea con el dominio de `urlOriginal`).
- **Idempotencia:** la clave es `tienda` + `idExterno`. Si ya existe, se actualiza; no se duplica.

```json
{
  "tipo": "ProductoScrapeado",
  "tiendaId": 2,
  "tienda": "Grid",
  "idExterno": "52974",
  "nombre": "REMERA NBA CHICAGO BULLS HOMBRE",
  "descripcion": "Remera de algodón...",
  "precio": 24999,
  "categoria": "REMERA",
  "genero": "Hombre",
  "marca": "NBA",
  "urlOriginal": "https://www.grid.com.ar/remera-nba-chicago-bulls/p",
  "imagenUrl": "https://essential.vteximg.com.br/arquivos/ids/.../remera.jpg",
  "disponible": true,
  "tallesDisponibles": ["M", "L"]
}
```

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `tipo` | string | no | Siempre `ProductoScrapeado` |
| `tiendaId` | entero | no | Id de la tienda en `tiendas.js` del scraper. Solo se usa si no viene `tienda` |
| `tienda` | string | sí | Nombre de la tienda (`Sportline`, `Grid`, `Newsport`): es la clave |
| `idExterno` | string | sí | Id del producto en la tienda (VTEX `productId`) |
| `nombre` | string | sí | Hasta 255 caracteres |
| `descripcion` | string | no | Sin HTML, hasta 1000 caracteres |
| `precio` | número | sí | ARS, mayor a 0: el mínimo de los talles con stock |
| `categoria` | string | sí | `REMERA` \| `PANTALON` \| `CALZADO` \| `VESTIDO` (los productos de otro tipo no se publican) |
| `genero` | string | sí | `Mujer` \| `Hombre` \| `Unisex` \| `Niña` |
| `marca` | string | no | `Sin marca` si la tienda no la informa |
| `urlOriginal` | string (URL) | sí | Página del producto en la tienda oficial |
| `imagenUrl` | string (URL) | no | |
| `disponible` | boolean | sí | Hay al menos un talle con stock |
| `tallesDisponibles` | string[] | sí | Solo los talles con stock; `[]` si no hay |

## SuscripcionActivada

- **Publica:** backend (P5), `SuscripcionService.aprobar` · **Consume:** backend (P5), `RabbitListeners.consumirSuscripcionActivada` (patrón Observer)
- **Clase:** `com.ddaii.events.SuscripcionActivada`
- **Cuándo:** cuando Mercado Pago confirma que el pago del plan premium está aprobado, por cualquiera de los tres caminos (ver [pagos.md](pagos.md)).
- **Efecto:** el consumidor marca al usuario como `premium = true`.
- **Idempotencia:** si el usuario ya es premium, no hace nada; una suscripción ya aprobada no vuelve a publicar el evento.

```json
{
  "tipo": "SuscripcionActivada",
  "usuarioId": 4,
  "suscripcionId": 5,
  "idPago": "190338481640",
  "estado": "ACTIVA",
  "fecha": "2026-11-18T19:42:10.123"
}
```

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `tipo` | string | no | Siempre `SuscripcionActivada` |
| `usuarioId` | entero | sí | `Usuario` que pasa a premium |
| `suscripcionId` | entero | sí | `Suscripcion` aprobada |
| `idPago` | string | sí | Id del pago en Mercado Pago |
| `estado` | string | no | Siempre `ACTIVA` |
| `fecha` | string (fecha-hora) | no | Momento de la publicación |

## RecomendacionSolicitada

- **Publica:** backend (P4), `POST /api/recomendaciones/solicitudes` · **Consume:** backend (P4), `RabbitListeners.consumirRecomendacionSolicitada`
- **Clase:** `com.ddaii.events.RecomendacionSolicitada`
- **Cuándo:** cuando se piden recomendaciones por cola (el endpoint responde 202 con el `solicitudId`).
- **Efecto:** el consumidor calcula las recomendaciones (estrategia por contenido, ver [ia.md](ia.md)) y las guarda; se consultan en `GET /api/recomendaciones/solicitudes/{solicitudId}`. Si `usuarioId` pertenece a un premium, el resultado prioriza sus marcas/categorías de interés.
- **Idempotencia:** la clave es `solicitudId`; una solicitud ya procesada no se recalcula.

```json
{
  "tipo": "RecomendacionSolicitada",
  "solicitudId": "3f2c9a1e-8b7d-4e2a-9c1f-5d6e7f8a9b0c",
  "productoId": 10,
  "limite": 5,
  "usuarioId": 7,
  "fecha": "2026-11-18T22:38:52.884"
}
```

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `tipo` | string | no | Siempre `RecomendacionSolicitada` |
| `solicitudId` | string (UUID) | sí | Lo genera el backend al recibir el pedido |
| `productoId` | entero | sí | Producto de referencia |
| `limite` | entero | sí | Cantidad máxima de recomendaciones, entre 1 y 20 |
| `usuarioId` | entero | no | Si viene, el consumidor prioriza las marcas/categorías que el premium ya vio o marcó como favorito |
| `fecha` | string (fecha-hora) | no | Momento del pedido |

---

## Cómo se prueba

- `EventosContratoTest`: cada evento se publica y se vuelve a leer con el **mismo conversor** que usa la
  aplicación, y `ProductoScrapeado` se lee desde el JSON exacto que genera el scraper.
- `AmqpConfigTest`: exchange, colas, DLQ, argumentos de dead-letter y bindings.
- `EventPublisherTest` y `RabbitListenersTest`: routing keys y que cada consumidor llame a su servicio.
- En el scraper: `tests/evento.test.js` (campos del contrato) y la declaración de la cola con los mismos argumentos.
- Evidencias: [docs/evidencias](evidencias/README.md) (panel de RabbitMQ, colas vaciándose, DLQ).
