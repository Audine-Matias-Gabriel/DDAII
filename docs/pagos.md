# Pago del plan premium con Mercado Pago

API externa obligatoria de la consigna (P5). Se usa **Checkout Pro** en el **sandbox** de Mercado Pago
con el SDK oficial `com.mercadopago:sdk-java`. Código: `integrations/MercadoPagoGateway.java`,
`services/SuscripcionService.java` y `controllers/SuscripcionController.java`.

## Flujo

```
 Usuario            Frontend              Backend                     Mercado Pago            RabbitMQ
   │ Hacerme premium  │                      │                             │                      │
   │─────────────────►│ POST /api/suscripciones (Bearer)                   │                      │
   │                  │─────────────────────►│ Suscripcion PENDIENTE       │                      │
   │                  │                      │ crea preferencia ──────────►│ external_reference =  │
   │                  │◄──── urlPago ────────│◄──── init_point ────────────│   id de suscripción   │
   │◄── redirige ─────│                      │                             │                      │
   │ paga con la cuenta compradora de prueba ─────────────────────────────►│                      │
   │                  │                      │                             │                      │
   │   Se confirma por cualquiera de estos tres caminos (todos consultan el pago a la API):     │
   │   1) vuelve a /pago/exito ─► POST /api/suscripciones/{id}/confirmar?paymentId=            │
   │   2) Planes / "Verificar pago" ─► POST /api/suscripciones/verificar (busca por referencia)│
   │   3) Mercado Pago ─► POST /api/suscripciones/webhook (type=payment, data.id)              │
   │                  │                      │── GET /v1/payments/{id} ───►│                      │
   │                  │                      │◄── status = approved ───────│                      │
   │                  │                      │ Suscripcion APROBADA ───── SuscripcionActivada ───►│
   │                  │                      │◄──────────── consumidor (Observer) ────────────────│
   │                  │                      │ usuario.premium = true      │                      │
```

1. **Crear el pago** (`POST /api/suscripciones`, con sesión): si el usuario ya es premium responde 400.
   Si no, guarda una `Suscripcion` `PENDIENTE` y crea la preferencia con el precio de
   `MP_PREMIUM_PRECIO` (Planes muestra ese mismo precio con `GET /api/suscripciones/plan`). El id de la
   suscripción viaja como `external_reference`. Devuelve `urlPago` y el front redirige.
2. **Pagar** en el checkout de Mercado Pago con la **cuenta compradora de prueba**.
3. **Confirmar.** Hay tres caminos, y los tres son idempotentes:
   - **Vuelta a `/pago/exito`:** el front manda el `payment_id` de la URL a `/confirmar`. Mercado Pago
     solo vuelve solo (`auto_return`) si `FRONTEND_URL` es https pública; en localhost el comprador vuelve
     con el botón del checkout o abriendo la tienda de nuevo.
   - **Verificar:** Planes llama a `/verificar` al entrar y al volver a la pestaña. El backend busca en
     Mercado Pago un pago aprobado con `external_reference` = cada suscripción pendiente. Es lo que hace
     que funcione en localhost sin que Mercado Pago pueda llegar al backend.
   - **Webhook:** Mercado Pago llama a `POST /api/suscripciones/webhook`. Necesita un backend público:
     poner `MP_NOTIFICATION_BASE_URL` (por ejemplo, la URL https de `ngrok http 8080`) y la preferencia
     manda `notification_url`. Acepta el formato actual (`type=payment`, `data.id`, por query o cuerpo) y
     el IPN viejo (`topic=payment`, `id`). Las notificaciones que no son de pagos se ignoran con 200.
4. **Activar:** al aprobarse, se publica `SuscripcionActivada` y su consumidor marca `premium = true`
   (ver [eventos.md](eventos.md)). El front refresca el usuario y Planes muestra "Ya sos premium".

## Seguridad

- **Nunca se confía en lo que llega del navegador ni de la notificación**: el `payment_id` de la URL
  de retorno o del webhook solo se usa para consultar el pago a la API de Mercado Pago, y se verifica que
  su `external_reference` sea la suscripción (y, en `/confirmar`, que sea del usuario de la sesión).
- **Firma del webhook:** si está `MP_WEBHOOK_SECRET` (Tus integraciones → Webhooks → clave secreta), se
  valida el header `x-signature` (HMAC-SHA256 de `id:<data.id>;request-id:<x-request-id>;ts:<ts>;`) y una
  firma inválida responde 401.
- **Idempotencia:** una suscripción aprobada no vuelve a publicar el evento (Mercado Pago reintenta las
  notificaciones y el usuario puede pasar por más de un camino). Un rechazo no cierra la suscripción:
  el comprador puede reintentar con otra tarjeta en el mismo checkout.
- `GET /api/suscripciones/{id}/estado` y `/confirmar` solo funcionan con suscripciones del usuario de la sesión.
- El access token va solo en `.env` (nunca en `.env.example` ni en el repo).

## Configuración

| Variable | Para qué |
|---|---|
| `MP_ACCESS_TOKEN` | Access token de **prueba de la cuenta vendedora** (Tus integraciones → Credenciales de prueba). Sin él, crear el pago responde 502 con un mensaje claro |
| `MP_PREMIUM_PRECIO` | Precio del plan en ARS (por defecto 5000) |
| `FRONTEND_URL` | Base de las back URLs (`/pago/exito`, `/pago/error`) |
| `MP_NOTIFICATION_BASE_URL` | Opcional: URL https pública del backend para el webhook |
| `MP_WEBHOOK_SECRET` | Opcional: clave secreta para validar la firma del webhook |

**Cuentas de prueba:** la **vendedora** es la dueña del access token (va en `.env`). La **compradora**
se usa solo para iniciar sesión en el checkout de Mercado Pago al pagar. En la tienda cada uno usa su
propia cuenta de la tienda; no se mezclan con las de Mercado Pago.

## Estados de la suscripción

| Estado | Cuándo |
|---|---|
| `PENDIENTE` | Se creó la preferencia y todavía no hay un pago aprobado |
| `APROBADA` | Mercado Pago confirmó un pago `approved` con su `external_reference` |
| `RECHAZADA` | Al volver por `/confirmar` con un pago `rejected` o `cancelled` |

## Cómo se prueba

- Tests (`mvn test`): `SuscripcionServiceTest` (crear, confirmar, verificar, webhook aprobado, repetido,
  rechazado y ajeno, estado de una suscripción ajena), `SuscripcionWebhookTest` (formatos de
  notificación, firma inválida), `MercadoPagoFirmaTest` y `MercadoPagoGatewayTest`.
- Evidencias en [docs/evidencias](evidencias/README.md):
  [mercadopago-webhook.txt](evidencias/mercadopago-webhook.txt) (webhook contra la API real del sandbox),
  [mercadopago-suscripciones.txt](evidencias/mercadopago-suscripciones.txt) (suscripciones aprobadas y usuario premium) y
  [08-front-planes.jpg](evidencias/08-front-planes.jpg).
- **A mano:** registrarse en la tienda, Planes → "Hacerme premium", pagar en el sandbox con la cuenta
  compradora y volver a Planes: tiene que decir "Ya sos premium" con el botón bloqueado.
