# Contrato de eventos (RabbitMQ)

Fuente única de verdad para los mensajes entre módulos. Si cambiás un campo, una routing key
o una cola, actualizá este archivo **en el mismo PR** y avisá al grupo.

## Convenciones generales

| Concepto | Valor |
|---|---|
| Exchange de eventos | `webderopa.eventos` (tipo `topic`, durable) |
| Exchange de errores (DLX) | `webderopa.dlx` (tipo `direct`, durable) |
| Nombre de cola | `<consumidor>.<evento-en-kebab-case>` |
| DLQ | `<cola>.dlq`, enlazada a `webderopa.dlx` con routing key igual a su nombre |
| Routing key | `<entidad>.<accion-en-participio>` (ej. `producto.scrapeado`) |
| Formato del cuerpo | JSON UTF-8, `content_type = application/json` |
| `message_id` | Identificador único del evento, usado para trazabilidad e idempotencia |
| Fechas | ISO-8601 sin zona, hora de Argentina (`2026-11-12T10:00:00`) |
| Precios | Número entero en pesos argentinos (ARS), sin decimales |

**Reglas para consumidores**

- Un mensaje con JSON inválido o campos obligatorios faltantes se **rechaza sin reencolar**
  (`AmqpRejectAndDontRequeueException`) y termina en la DLQ de su cola.
- El procesamiento tiene que ser **idempotente**: el mismo evento puede llegar más de una vez.
- Los campos desconocidos se ignoran, así se pueden agregar campos sin romper a nadie.

## Resumen

| Evento | Publica | Consume | Routing key | Cola | DLQ |
|---|---|---|---|---|---|
| ProductoScrapeado | scraper (P3) | backend (P2) | `producto.scrapeado` | `backend.producto-scrapeado` | `backend.producto-scrapeado.dlq` |
| SuscripcionActivada | backend (P5) | backend (P5) | `suscripcion.activada` | `backend.suscripcion-activada` | `backend.suscripcion-activada.dlq` |
| RecomendacionSolicitada | backend (P4) | backend (P4) | `recomendacion.solicitada` | `backend.recomendacion-solicitada` | `backend.recomendacion-solicitada.dlq` |

---

## ProductoScrapeado

- **Publica:** scraper (P3) · **Consume:** backend (P2)
- **Exchange:** `webderopa.eventos` · **Routing key:** `producto.scrapeado`
- **Cola:** `backend.producto-scrapeado` · **DLQ:** `backend.producto-scrapeado.dlq`
- **Cuándo:** cada vez que el scraper lee un producto de una tienda (nuevo o actualizado).
- **Idempotencia:** la clave es `tienda` + `idExterno`. Si ya existe, se actualiza; no se duplica.

```json
{
  "tienda": "tienda1",
  "idExterno": "123",
  "nombre": "Remera X",
  "precio": 25000,
  "categoria": "REMERA",
  "genero": "HOMBRE",
  "marca": "X",
  "urlOriginal": "https://...",
  "imagenUrl": "https://...",
  "disponible": true,
  "tallesDisponibles": ["S", "M"]
}
```

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `tienda` | string | sí | Identificador de la tienda de origen |
| `idExterno` | string | sí | ID del producto en la tienda de origen |
| `nombre` | string | sí | |
| `precio` | entero | sí | ARS, mayor a 0 |
| `categoria` | string | sí | Valor de `Categoria`: `REMERA`, `PANTALON`, `BUZO`, `CAMPERA`, `ZAPATILLA`, `ZAPATO`, `ACCESORIO` |
| `genero` | string | sí | Valor de `Genero`: `HOMBRE`, `MUJER`, `UNISEX`, `INFANTIL` |
| `marca` | string | no | |
| `urlOriginal` | string (URL) | sí | Link al producto en la tienda |
| `imagenUrl` | string (URL) | no | |
| `disponible` | boolean | sí | |
| `tallesDisponibles` | string[] | sí | Puede ser `[]` si no hay stock |

## SuscripcionActivada

- **Publica:** backend (P5) · **Consume:** backend (P5)
- **Exchange:** `webderopa.eventos` · **Routing key:** `suscripcion.activada`
- **Cola:** `backend.suscripcion-activada` · **DLQ:** `backend.suscripcion-activada.dlq`
- **Cuándo:** cuando se confirma con Mercado Pago que el pago del plan premium fue aprobado.
- **Efecto:** el consumidor marca al usuario como `premium = true`.
- **Idempotencia:** si el usuario ya es premium, no hace nada.

```json
{
  "suscripcionId": 1,
  "usuarioId": 7,
  "pagoId": "123456",
  "fecha": "2026-11-12T10:00:00"
}
```

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `suscripcionId` | entero | sí | ID de la `Suscripcion` en la base |
| `usuarioId` | entero | sí | ID del `Usuario` que pasa a premium |
| `pagoId` | string | sí | ID del pago en Mercado Pago |
| `fecha` | string (fecha-hora) | sí | Momento de la aprobación |

## RecomendacionSolicitada

- **Publica:** backend (P4) · **Consume:** backend (P4)
- **Exchange:** `webderopa.eventos` · **Routing key:** `recomendacion.solicitada`
- **Cola:** `backend.recomendacion-solicitada` · **DLQ:** `backend.recomendacion-solicitada.dlq`
- **Cuándo:** cuando se piden recomendaciones de productos similares a uno dado.
- **Idempotencia:** la clave es `solicitudId`.

```json
{
  "solicitudId": "3f2c9a1e-8b7d-4e2a-9c1f-5d6e7f8a9b0c",
  "productoId": 10,
  "limite": 5
}
```

| Campo | Tipo | Obligatorio | Notas |
|---|---|---|---|
| `solicitudId` | string (UUID) | sí | Generado por quien publica |
| `productoId` | entero | sí | Producto de referencia |
| `limite` | entero | sí | Cantidad máxima de recomendaciones, entre 1 y 20 |

---

## Pendiente de aprobación

- [ ] P2 (consumidor de ProductoScrapeado, entidad Usuario)
- [ ] P3 (publicador de ProductoScrapeado)
- [ ] P4 (RecomendacionSolicitada)

Si en 2 horas desde que se envía al grupo nadie objeta, se toma como aprobado.
