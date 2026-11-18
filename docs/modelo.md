# Modelo de datos

Entidades JPA del backend (`tienda/backend/src/main/java/com/ddaii/domain`). Las tablas las crea y
actualiza Hibernate (`spring.jpa.hibernate.ddl-auto=update`). Contrato de los eventos de RabbitMQ:
[eventos.md](eventos.md) y `tienda/backend/.../events/`.

```
Tienda 1 ──< Producto >── talle_disponible (lista de talles)
                │
Usuario 1 ──< Favorito >── 1 Producto
   │
   ├──< Suscripcion        (pago del plan premium en Mercado Pago)
   └──< SesionUsuario      (token de sesión)

Recomendacion ──> productoId + productosRecomendados (ids), resultado de una solicitud por cola
```

## Producto (`producto`)

Una prenda tal como la publica una tienda. **Único por `tienda` + `idExterno`**: la misma prenda
puede estar una vez por tienda (no se deduplica entre tiendas). La crea o actualiza el consumidor de
`ProductoScrapeado` (upsert).

| Campo | Tipo | Notas |
|---|---|---|
| `id` | Long | Generado |
| `idExterno` | String, obligatorio | Id del producto en la tienda (VTEX `productId`) |
| `tienda` | Tienda, obligatorio | Se resuelve por **nombre** de tienda (si no existe, se crea) |
| `nombre` | String, obligatorio | |
| `descripcion` | String (1000) | Sin HTML |
| `precio` | Double, obligatorio | ARS, el mínimo de los talles con stock |
| `categoria` | String | `REMERA` \| `PANTALON` \| `CALZADO` \| `VESTIDO` (lo normaliza el scraper) |
| `genero` | String | `Mujer` \| `Hombre` \| `Unisex` \| `Niña` |
| `marca` | String | `Sin marca` si la tienda no la informa |
| `urlOriginal` | String | Página del producto en la tienda oficial ("Ver en tienda") |
| `imagenUrl` | String | Primera imagen |
| `disponible` | Boolean, obligatorio | Hay al menos un talle con stock. El catálogo solo lista los disponibles |
| `tallesDisponibles` | List&lt;String&gt; | Tabla `talle_disponible`; solo los talles con stock |
| `ximilarImagenUrl` | String (1000) | Imagen subida a la colección de Ximilar (null = no subida) |

## Tienda (`tienda`)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | Long | Generado |
| `nombre` | String, obligatorio | Clave estable del contrato con el scraper (`Sportline`, `Grid`) |
| `urlBase` | String, obligatorio | Dominio de la tienda, tomado de `urlOriginal` (`https://www.grid.com.ar`) |
| `activa` | Boolean | |

## Usuario (`usuario`)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | Long | Generado |
| `nombre` | String, obligatorio | |
| `email` | String, obligatorio, único | |
| `passwordHash` | String, obligatorio | BCrypt. Contraseña de al menos 6 caracteres |
| `premium` | Boolean | Lo pone en `true` el consumidor de `SuscripcionActivada` |

## Suscripcion (`suscripcion`)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | Long | Viaja a Mercado Pago como `external_reference` |
| `usuario` | Usuario, obligatorio | |
| `estado` | String | `PENDIENTE` → `APROBADA` (o `RECHAZADA`) |
| `preferenciaId` | String | Preferencia de pago creada en Mercado Pago |
| `idPago` | String | Id del pago aprobado en Mercado Pago |
| `fecha` | LocalDateTime | |

Al aprobarse se publica `SuscripcionActivada` y el consumidor marca al usuario como premium.

## Favorito (`favorito`)

`usuario` + `producto` (únicos juntos) y `fecha`.

## SesionUsuario (`sesion_usuario`)

`usuario`, `token` (único) y `expiraEn`. El token va en `Authorization: Bearer <token>`.

## Recomendacion (`recomendacion`)

Resultado de una solicitud por cola (`RecomendacionSolicitada`): `solicitudId` (único), `productoId`,
`fecha` y `productosRecomendados` (tabla `recomendacion_producto`).
