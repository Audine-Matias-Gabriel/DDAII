# Agregador de Ropa Multi-tienda

Proyecto universitario para **Desarrollo de Aplicaciones II — UADE**.
App web tipo Mercado Libre solo para ropa, con artículos de múltiples tiendas.

La **Segunda Parte (P2)** apunta a una arquitectura de integración sincrónica y
asincrónica: SOAP, REST con Swagger, RabbitMQ, dos servicios separados, APIs externas y un
componente de IA.

## Alcance P2

- Catálogo de prendas de 1 o 2 tiendas reales, obtenido por scraping.
- Cada producto guarda la URL oficial de la tienda y puede repetirse entre tiendas.
- Stock simplificado: **disponible / sin stock** y **talles disponibles**.
- API REST con filtros, paginación, recomendaciones, similares y suscripciones.
- Servicio **SOAP** de equivalencias de talles AR/US/EU con WSDL.
- **RabbitMQ** con exchange, colas por consumidor y colas de error (DLQ).
- Eventos: `ProductoScrapeado`, `SuscripcionActivada`, `RecomendacionSolicitada`.
- **IA de recomendaciones por contenido**, por REST y por cola.
- **Ximilar** para prendas similares.
- **MercadoPago sandbox** para el plan premium.
- Frontend de demo: catálogo, detalle, filtros, “Ver tienda”, recomendaciones, similares y
  premium.
- Docker Compose con PostgreSQL, RabbitMQ, backend, scraper y frontend.

No hay compra ni carrito: se eliminaron `Pedido`, `DetallePedido`, el checkout y el
descuento de stock. Try-on, outfits y login con Firebase quedan para el Integrador.

## Estructura

```
tienda/
  backend/              # Java 17 + Spring Boot + Maven + PostgreSQL
  scraper/              # segundo servicio Spring Boot: scraping y publicación
  frontend/             # React 19 + TypeScript + Vite
  ximilar-product-similarity/  # integración con Ximilar
docker-compose.yml      # PostgreSQL, RabbitMQ, backend, scraper y frontend
docs/                   # contexto, planes, eventos, IA y evidencias (ignorado por git)
```

## Stack

- Backend: Java 17, Spring Boot 4.1, Maven, Spring Data JPA, PostgreSQL, Spring AMQP,
  Spring-WS, springdoc.
- Scraper: Java 17 + Spring Boot.
- Frontend: TypeScript, React 19 + Vite 8, CSS Modules.
- Mensajería: RabbitMQ.
- APIs externas: MercadoPago sandbox y Ximilar.
- Deploy: Docker + Docker Compose.
- Tests: JUnit 5 + Mockito y Vitest + Testing Library.

## Levantar

Requisitos: Docker Desktop y un **JDK 17+** con `JAVA_HOME` apuntando a él. Maven no hace
falta instalarlo: el wrapper del repo (`.\mvnw`) lo descarga solo en el primer uso.

```powershell
docker compose up -d                    # PostgreSQL, RabbitMQ, backend, scraper y frontend

cd tienda\backend
.\mvnw spring-boot:run                  # API en localhost:8080

cd ..\frontend
npm install
npm run dev                             # UI en localhost:5173
```

Para levantar todo junto:

```powershell
docker compose up --build
```

## Servicios y puertos

| Servicio | Puerto | Descripción |
|---|---|---|
| PostgreSQL | 5433 | Base de datos |
| RabbitMQ AMQP | 5672 | Mensajería |
| RabbitMQ administración | 15672 | Panel web |
| Backend | 8080 | API REST, SOAP y Swagger |
| Scraper | 8081 | Servicio de scraping |
| Frontend | 5173 | Demo React |

Credenciales locales de RabbitMQ: usuario `tienda`, contraseña `tienda_dev_123`.

## Configuración

Las credenciales y URLs van en `.env`. El repositorio solo versiona `.env.example` con los
nombres de las variables.

Variables principales:

```properties
# Base de datos
POSTGRES_DB=tienda
POSTGRES_USER=tienda
POSTGRES_PASSWORD=tienda_dev_123

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=tienda
SPRING_RABBITMQ_PASSWORD=tienda_dev_123

# MercadoPago
MERCADOPAGO_ACCESS_TOKEN=
MERCADOPAGO_PUBLIC_KEY=
MERCADOPAGO_WEBHOOK_URL=

# Ximilar
XIMILAR_API_TOKEN=
XIMILAR_COLLECTION_ID=

# Frontend
VITE_API_URL=http://localhost:8080
```

No commitear `.env` ni tokens.

## API REST

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Catálogo con filtros y paginación |
| GET | `/api/productos/{id}` | Detalle de producto |
| GET | `/api/tiendas` | Listado de tiendas |
| GET | `/api/productos/{id}/similares` | Prendas similares de otras tiendas |
| GET | `/api/recomendaciones?productoId=&limite=` | Recomendaciones por contenido |
| POST | `/api/recomendaciones/solicitudes` | Solicitud asincrónica (`202`) |
| POST | `/api/suscripciones` | Crea el pago premium y devuelve la URL |
| POST | `/api/suscripciones/webhook` | Confirmación de MercadoPago |
| GET | `/api/suscripciones/{id}/estado` | Consulta el estado del pago |

Filtros del catálogo: `categoria`, `tienda`, `genero`, `precioMin`, `precioMax`, `texto`,
`pagina`, `tamano`.

Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

## SOAP

Servicio de equivalencias de talles AR/US/EU:

- WSDL: `http://localhost:8080/ws/equivalencias-talles.wsdl`
- Endpoint: `http://localhost:8080/ws/equivalencias-talles`
- Cubre remeras, pantalones y calzado.

## RabbitMQ

### Topología

- Exchange: `tienda.exchange`.
- `producto.scrapeado.queue` + `producto.scrapeado.dlq`.
- `suscripcion.activada.queue` + `suscripcion.activada.dlq`.
- `recomendacion.solicitada.queue` + `recomendacion.solicitada.dlq`.

### Eventos

| Evento | Publica | Consume |
|---|---|---|
| `ProductoScrapeado` | Scraper | Backend |
| `SuscripcionActivada` | Backend (pago aprobado) | Backend |
| `RecomendacionSolicitada` | Backend (REST) | Componente de IA |

La documentación completa con JSON de ejemplo está en `docs/eventos.md`.

### Verificar

1. Abrir `http://localhost:15672`.
2. Ingresar con `tienda` / `tienda_dev_123`.
3. Revisar **Queues and Streams**: deben existir las colas y sus DLQ.
4. Correr el scraper y ver los mensajes de `ProductoScrapeado`.
5. Solicitar recomendaciones y ver el mensaje entrar y salir de la cola.

Si un mensaje es rechazado, termina en la DLQ correspondiente en lugar de perderse.

## MercadoPago

1. Crear una cuenta de desarrollador y usuarios de prueba en sandbox.
2. Configurar `MERCADOPAGO_ACCESS_TOKEN` y `MERCADOPAGO_PUBLIC_KEY` en `.env`.
3. Exponer el backend con ngrok para que MercadoPago llegue al webhook.
4. Desde el frontend, entrar a `/planes` y pulsar **“Hacerme premium”**.
5. Pagar en la pantalla de prueba.
6. Verificar que el usuario quede con `premium = true`.

Plan B si el webhook no llega: al volver a `/pago/exito`, el backend consulta el estado del
pago con `GET /api/suscripciones/{id}/estado`.

## Ximilar

- `XimilarFacade` sube imágenes del catálogo y busca prendas similares.
- `GET /api/productos/{id}/similares` devuelve productos de otras tiendas.
- Hay perfil mock con respuestas fijas para tests y demo sin red.
- Plan gratuito: 1.000 créditos/mes, 0,25 por subir imagen, 15 por tagging.
  **No se usa tagging.**

Detalle en `tienda/ximilar-product-similarity/env.md`.

## Frontend de demo

| Ruta | Pantalla |
|---|---|
| `/login` | Login simple (mock) |
| `/` | Catálogo público |
| `/productos/:id` | Detalle público |
| `/planes` | Planes y botón premium |
| `/pago/exito` | Pago exitoso |
| `/pago/error` | Pago rechazado o cancelado |
| `*` | 404 |

El catálogo y el detalle son públicos. El login simple guarda `usuarioId` y lo envía en el
header `X-Usuario-Id`.

## Métodos de prueba

### Backend

```powershell
cd tienda\backend
.\mvnw test
.\mvnw spring-boot:run
```

### Frontend

```powershell
cd tienda\frontend
npm run test
npm run build
npm run lint
```

### Docker

```powershell
docker compose down -v      # resetea la base y las colas
docker compose up -d --build
docker compose ps
```

### Checks manuales

```powershell
# Catálogo con filtros
curl.exe -s "localhost:8080/api/productos?categoria=REMERA&pagina=0&tamano=10"

# Detalle
curl.exe -s localhost:8080/api/productos/1

# Tiendas
curl.exe -s localhost:8080/api/tiendas

# Similares
curl.exe -s localhost:8080/api/productos/1/similares

# Recomendaciones REST
curl.exe -s "localhost:8080/api/recomendaciones?productoId=1&limite=5"

# Solicitud asincrónica de recomendaciones
curl.exe -s -i -X POST localhost:8080/api/recomendaciones/solicitudes `
  -H "Content-Type: application/json" `
  -d "{\"productoId\":1,\"limite\":5}"

# Crear suscripción premium
curl.exe -s -i -X POST localhost:8080/api/suscripciones `
  -H "Content-Type: application/json" `
  -H "X-Usuario-Id: 1" `
  -d "{\"usuarioId\":1,\"plan\":\"PREMIUM\"}"
```

## Estado

El repositorio conserva la base del backend, frontend, RabbitMQ y la integración inicial con
Ximilar. La Segunda Parte se organiza en las tareas de `docs/CONTEXTO.md`,
`docs/PLAN-INTEGRACION.md` y `docs/PLAN-TESTS.md`.

Pendientes principales:

- Eliminar el flujo de compra y el stock por variante.
- Implementar el modelo nuevo de catálogo y el scraper.
- Implementar SOAP, Swagger, colas nuevas y DLQ.
- Implementar recomendaciones por contenido y prendas similares.
- Integrar MercadoPago sandbox.
- Adaptar el frontend a filtros, disponibilidad, recomendaciones, similares y premium.
- Completar Docker Compose, CI y evidencias.

## Fallos típicos

| Síntoma | Causa |
|---|---|
| `password authentication failed for user "tienda"` | Volumen viejo: falta `docker compose down -v` |
| `Port 5433 / 5672 / 8080 / 5173 is already allocated` | Hay otra instancia viva; bajarla antes |
| El frontend dice `No se pudo conectar con el backend` | El backend está caído o no está en el puerto 8080 |
| Error de CORS en el navegador | El front se abrió en un origen distinto de `localhost:5173` |
| `PRECONDITION_FAILED` en RabbitMQ | La cola existe con otros argumentos: borrar contenedor y volumen |
| Ximilar responde error de créditos | Usar el perfil mock o renovar la cuenta |
| MercadoPago no llama al webhook | Exponer el backend con ngrok o usar el plan B |

## Equipo

### Profesor
- Nombre y apellido: Parkinson, Christian Anibal
- Email: cparkinson@uade.edu.ar

### Profesor auxiliar
- Nombre y apellido: Bianca, Nahuel
- Email: nbianca@uade.edu.ar

### Miembros del grupo (5)
1. Nombre y apellido: Aldaz, Luciano Nahuel — Legajo: 1139484 — Email: lualdaz@uade.edu.ar
2. Nombre y apellido: Audine, Matias Gabriel — Legajo: 1172179 — Email: maudine@uade.edu.ar
3. Nombre y apellido: Frisoli Pirc, Salvador — Legajo: 1112365 — Email: SAFRISOLIPIRC@uade.edu.ar
4. Nombre y apellido: Mendez, Nicolas — Legajo: 1172141 — Email: nicomendez@uade.edu.ar
5. Nombre y apellido: Vignolo, Nahir Yael — Legajo: 1137587 — Email: nvignolo@uade.edu.ar

## Entregas
- TP Inicial: componentes + multicapa + local vs remoto
- P1: app empresarial + patrones + servicios
- P2: SOAP/REST + RabbitMQ + API externa + IA
