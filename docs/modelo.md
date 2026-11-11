# Modelo de datos — Web de Ropa

Acordado el Día 1 por P2 y aprobado por P3 y P5. Formato de producto que usan backend, scraper y frontend.

## Producto

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | Identificador interno (@GeneratedValue) |
| idExterno | String | ID de la prenda en la tienda original |
| tienda | String | Tienda de origen (ej. "tienda1", "tienda2") |
| nombre | String | Nombre de la prenda |
| descripción | String | Descripción |
| precio | Double | Precio |
| categoría | String | Categoría (REMERA, PANTALON, CALZADO, VESTIDO, ...) |
| género | String | Género |
| marca | String | Marca |
| urlOriginal | String | URL oficial de la tienda |
| imagenUrl | String | URL de la imagen |
| disponible | boolean | disponible / sin stock |
| tallesDisponibles | List<String> | Talles disponibles |

**Regla de unicidad:** `tienda + idExterno` es único. La misma prenda puede aparecer una vez por tienda (sin deduplicar entre tiendas).

## Tienda

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | Identificador interno |
| nombre | String | Nombre de la tienda |
| urlOriginal | String | URL oficial de la tienda |

## Usuario

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | Identificador interno |
| nombre | String | Nombre |
| email | String | Email |
| premium | boolean | Tiene plan premium activo |

## Suscripcion

| Campo | Tipo | Descripción |
|---|---|---|
| usuario | Usuario | Usuario que suscribe |
| estado | String | Estado de la suscripción/pago |
| idPago | String | ID del pago en Mercado Pago |
| fecha | LocalDateTime | Fecha |

## Categorías válidas

REMERA, PANTALON, CALZADO, VESTIDO. (Corregido el Día 2: REMPERA → REMERA, se agrega VESTIDO.)

## Eventos (publicados a RabbitMQ)

Ver `docs/eventos.md`: `ProductoScrapeado`, `SuscripcionActivada`, `RecomendacionSolicitada`.
