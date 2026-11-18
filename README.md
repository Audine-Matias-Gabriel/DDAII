# Web de Ropa — TP Segunda Parte

Agregador de ropa con integración sincrónica y asincrónica. Entrega en 10 días, 6 fases y 3 hitos.

## ¿Qué entrega esta etapa (7 requisitos obligatorios, prioridad 1)

| Requisito | Cómo lo cumplimos | Dueño | Evidencia |
|---|---|---|---|
| ≥ 1 servicio SOAP con WSDL | Equivalencias de talles (AR/US/EU) con Spring-WS | P2 | WSDL + request/respuesta de prueba |
| ≥ 2 servicios REST con OpenAPI | Catálogo, suscripciones, recomendaciones (+Swagger UI) | P2 (Swagger) + cada dueño | Swagger UI con todos los endpoints |
| Mensajería y colas RabbitMQ | Exchange, colas por consumidor y cola de errores (DLQ) | P5 | Panel de RabbitMQ con mensajes |
| Productores y consumidores | ProductoScrapeado, SuscripcionActivada, RecomendacionSolicitada | P5 coordina; P2, P3, P4 implementan | Logs de publicación y consumo |
| Arquitectura SOA / microservicios ligeros | 2 servicios (backend y scraper) comunicados por la cola | P3 y P2 | docker-compose con los servicios |
| ≥ 1 API externa real | Mercado Pago (pagos, plan premium, sandbox) | P5 | Pago de prueba en sandbox que activa premium |
| Componente de IA por REST o cola | Recomendaciones por contenido (modelo simple) | P4 | Tests con casos esperados + llamado REST y por cola |

## Arquitectura

```
 Sportline / Grid (VTEX)                                   Mercado Pago  Ximilar
        │ HTTP                                                   ▲         ▲
        ▼                                                        │ REST    │ REST
 ┌─────────────┐  ProductoScrapeado  ┌──────────┐   consume   ┌───┴─────────┴───┐  JDBC  ┌──────────┐
 │   scraper   │ ──────────────────► │ RabbitMQ │ ◄─────────► │     backend     │ ─────► │ Postgres │
 │  (Node.js)  │  tienda.exchange    │ + DLQs   │  Suscripcion│  (Spring Boot)  │        └──────────┘
 └─────────────┘                     └──────────┘  Activada,  │ REST · SOAP     │
                                                   Recomenda- └────────▲────────┘
                                                   cionSolicitada      │ REST (JSON)
                                                                ┌──────┴──────┐
                                                                │  frontend   │
                                                                │ (React/Vite)│
                                                                └─────────────┘
```

Los dos servicios (backend y scraper) no se llaman entre sí: solo se comunican por RabbitMQ.

- **backend**: catálogo, SOAP de talles, suscripciones/pago Mercado Pago, recomendaciones REST y por cola, similares Ximilar.
- **scraper** (Node.js): lee los catálogos reales de Sportline y Grid (API pública de VTEX), normaliza categoría, género y talles, publica `ProductoScrapeado` a RabbitMQ una vez por día y guarda snapshots. Ver [tienda/scraper/README.md](tienda/scraper/README.md).
- **frontend mínimo**: catálogo y detalle públicos, "Ver en tienda", filtros, bloque "Te puede gustar", página Planes con botón "Hacerme premium" que redirige a Mercado Pago.

## Estructura

```
tienda/
  backend/                     # API REST, SOAP y RabbitMQ (Spring Boot)
  scraper/                     # servicio de scraping (Node.js)
  frontend/                    # demo web (Node/JS)
  ximilar-product-similarity/  # prototipo Node de Ximilar (no se usa: la integración está en el backend)
docker-compose.yml             # PostgreSQL, RabbitMQ, backend, scraper y frontend
docs/                          # modelo.md, eventos.md y evidencias/ versionados; el resto es local
```

## Servicios (docker-compose)

| Servicio | Puerto | Descripción |
|---|---|---|
| postgres | 5432 | Base de datos del backend |
| rabbitmq | 5672 / 15672 | Broker de mensajes + panel de administración |
| backend | 8080 | API REST, SOAP, Swagger UI |
| scraper | — | Segundo servicio (Node.js): lee catálogos y publica a RabbitMQ (sin puerto) |
| frontend | 5173 | Web mínima para la demo |

## Cómo levantar el proyecto

```bash
# 1. Copiar variables de entorno
cp .env.example .env
# editar .env con tus valores (claves de Mercado Pago/Ximilar en sandbox)

# 2. Levantar toda la pila
docker compose up --build

# 3. Verificar
# Swagger UI:       http://localhost:8080/swagger-ui/index.html
# WSDL (SOAP):      http://localhost:8080/ws/equivalencias-talles.wsdl
# RabbitMQ panel:   http://localhost:15672  (guest / guest)
# Frontend demo:    http://localhost:5173
```

Sin `.env` también levanta: Mercado Pago y Ximilar quedan sin credenciales (el pago falla con un
502 claro y "similares" usa el respaldo). El catálogo se llena solo cuando corre el scraper (al arrancar
y después todos los días a las 2:00). Para empezar de cero: `docker compose down -v`.

## Endpoints

Documentación interactiva y ejemplos en Swagger UI. 🔒 = requiere `Authorization: Bearer <token>` (de
`/api/auth/login` o `/api/auth/registro`; en Swagger, botón **Authorize**).

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/auth/registro` | Crea la cuenta y devuelve `{ token, usuario }` |
| POST | `/api/auth/login` | Inicia sesión y devuelve `{ token, usuario }` |
| GET 🔒 | `/api/auth/me` | Usuario de la sesión (incluye `premium`) |
| POST 🔒 | `/api/auth/logout` | Cierra la sesión |
| GET | `/api/productos` | Catálogo paginado. Filtros: `categoria`, `tienda` (id), `genero`, `precioMin`, `precioMax`, `texto`, `pagina`, `tamano` (1 a 100) |
| GET | `/api/productos/{id}` | Detalle del producto |
| GET | `/api/productos/disponibles` | Todos los productos con stock |
| GET | `/api/productos/{id}/similares?limite=` | Prendas parecidas de otra tienda (Ximilar; `origen` indica si vino de Ximilar o del respaldo) |
| GET | `/api/tiendas` | Tiendas del catálogo |
| GET | `/api/recomendaciones?productoId=&limite=` | Recomendaciones por contenido (IA, sincrónico) |
| POST | `/api/recomendaciones/solicitudes` | Pide recomendaciones por cola: responde 202 y publica `RecomendacionSolicitada` |
| GET | `/api/suscripciones/plan` | Precio del plan premium (el que se cobra en Mercado Pago) |
| POST 🔒 | `/api/suscripciones` | Crea el pago premium y devuelve la URL de Mercado Pago |
| POST 🔒 | `/api/suscripciones/{id}/confirmar?paymentId=` | Confirma el pago al volver de Mercado Pago |
| POST 🔒 | `/api/suscripciones/verificar` | Busca en Mercado Pago pagos aprobados de las suscripciones pendientes |
| GET 🔒 | `/api/suscripciones/{id}/estado` | Estado de una suscripción |
| POST 🔒 | `/api/suscripciones/webhook?idPago=` | Confirmación simulada de un pago ya registrado |
| GET 🔒 / POST 🔒 / DELETE 🔒 | `/api/favoritos`, `/api/favoritos/{productoId}` | Favoritos del usuario |
| POST 🔒 | `/api/similares/indexar?maximo=` | Sube a Ximilar las imágenes pendientes del catálogo |
| POST | `/ws` (SOAP) | Equivalencias de talles (ver abajo) |

**Errores:** todos responden `{ "estado": 404, "error": "No encontrado", "mensaje": "Producto no encontrado: 9", "ruta": "/api/productos/9" }`.
Los errores del cliente son 4xx con un mensaje para mostrar; los 500 no muestran detalles internos (quedan en el log).

## Cómo probar el SOAP

WSDL: `http://localhost:8080/ws/equivalencias-talles.wsdl`. Endpoint: `POST http://localhost:8080/ws`.
Prendas: `REMERA` (S, M, L), `PANTALON` (40, 42 AR) y `CALZADO` (38, 39 AR); sistemas `AR`, `US`, `EU`.

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:eq="http://ddaii.com/equivalencias">
  <soapenv:Body>
    <eq:EquivalenciaRequest>
      <eq:pais>AR</eq:pais>
      <eq:tipoPrenda>CALZADO</eq:tipoPrenda>
      <eq:talle>38</eq:talle>
    </eq:EquivalenciaRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

Respuesta: `US → 6`, `EU → 38`. Con el archivo de ejemplo del repo:

```powershell
curl.exe -X POST http://localhost:8080/ws -H "Content-Type: text/xml" --data-binary "@docs/evidencias/soap-request.xml"
```

En Postman: `POST http://localhost:8080/ws`, header `Content-Type: text/xml` y ese XML como body raw.

## Pruebas

```bash
# Backend (Java/Maven)
cd tienda/backend
mvn test

# Frontend (Node)
cd ../frontend
npm test

# Scraper (Node, sin internet)
cd ../scraper
npm ci && npm test
```

El pipeline de GitHub Actions corre `mvn test` y los `npm test` del frontend y del scraper en cada Pull Request.

## Prendas similares con Ximilar

`GET /api/productos/{id}/similares` devuelve prendas de **otra tienda** que se parecen visualmente, usando
[Ximilar Product Similarity](https://docs.ximilar.com/visual-search/overview) (patrón Facade: `XimilarFacade`).

1. **Configurar:** en `.env` poner `XIMILAR_API_KEY` y `XIMILAR_COLLECTION_ID` de la cuenta de Ximilar
   (nunca en `.env.example`). Sin ellas, o con `XIMILAR_MOCK=true`, no se llama a la API.
2. **Cargar el catálogo en la colección:** cada producto se sube como un registro
   (`_id = producto-<id>`, `_url` = imagen, `product_id`, `tienda_id`), de a 10 por llamada.
   - Automático: con `XIMILAR_INDEXADO_AUTOMATICO=true` (por defecto en compose) se suben solas las imágenes
     nuevas o cambiadas cada 10 minutos, hasta 100 por corrida.
   - A mano (con sesión iniciada): `POST /api/similares/indexar?maximo=100`. Responde cuántas subió, cuántas
     quedan y los créditos estimados. Repetir hasta `pendientesDespues = 0`.
3. **Buscar:** `visualKNN` por el `_id` del producto, filtrando `tienda_id != tienda del producto`. Cada
   similar trae `origen: "ximilar"`. Si Ximilar no está configurado o falla, el endpoint usa el respaldo
   (misma categoría en otra tienda) y marca `origen: "catalogo"`.

**Créditos (plan gratuito: 1.000 por mes, colección de hasta 1.000 registros):**

| Operación | Créditos | Uso en este proyecto |
|---|---|---|
| Subir una imagen (sin procesar) | 0,25 | ~537 productos ≈ **134 créditos**, una sola vez. Después solo los nuevos |
| Buscar similares por `_id` | 0,25 | Una vez por detalle de producto abierto |
| Buscar por URL externa | 1 | Solo si el producto todavía no está en la colección |
| Tagging de fashion | 15 | **No se usa** |

Topes para no pasarse: `XIMILAR_MAXIMO_POR_CORRIDA` (100) y `XIMILAR_MAXIMO_COLECCION` (1.000).

**Créditos usados (18/11/2026):** carga inicial de 537 imágenes = **134,25 créditos**, más unas 15 búsquedas de prueba (~4 créditos). En total, unos **138 de los 1.000** del mes. La colección tiene 561 registros: 537 productos y 24 de pruebas anteriores, que la búsqueda ignora porque no tienen `tienda_id`. Evidencia en [docs/evidencias](docs/evidencias/README.md).

## Trabajo por integrante (resumen del plan de 10 días)

- **P1 — Frontend y evidencias:** frontend mínimo, borrado de carrito, filtros, detalle con "Ver en tienda", login simple (header `X-Usuario-Id`), bloque "Te puede gustar", página Planes/premium, Dockerfile del frontend, evidencias en `docs/evidencias/`. Revisa PR de P4.
- **P2 — Backend core, catálogo y SOAP:** merge prototype→main (Día 1), modelo, borrado de código de compra, entidades/repositorios/DTOs, endpoints catálogo + paginación + seed, Swagger UI (springdoc), SOAP talles (Spring-WS), consumidor de ProductoScrapeado, tests, Dockerfile backend + docker-compose final, README. Revisa PR de P3.
- **P3 — Scraper y Ximilar:** elegir 2 tiendas argentinas con catálogo público, cuenta Ximilar, proyecto `tienda/scraper/`, snapshot tienda 1 (≥30 prendas), interfaz `ScraperTienda` (Strategy), publicar `ProductoScrapeado` a RabbitMQ, tienda 2, ejecución automática 1 vez/día, `XimilarFacade` y endpoint similares, tests del convertidor sin internet, Dockerfile scraper. Revisa PR de P5.
- **P4 — IA de recomendaciones y configuración de GitHub:** configurar GitHub el Día 1 (main protegida, etiquetas P1-P5, hitos 1-3, tablero Projects, plantilla de PR, Actions `mvn test` + `npm test`), regla del recomendador (`docs/ia.md`), `EstrategiaRecomendacion` + `RecomendacionPorContenido` (Strategy), `RecomendacionFactory` (Factory), 5 tests unitarios, GET `/api/recomendaciones`, versión por cola (`RecomendacionSolicitada`), segunda estrategia `RecomendacionComplementaria`, tabla de validación con 10 casos. Revisa PR de P1.
- **P5 — RabbitMQ y Mercado Pago:** `docs/eventos.md`, cuenta desarrollador Mercado Pago (sandbox), topología RabbitMQ (exchange, colas por consumidor, DLQ), entidad Suscripcion, POST `/api/suscripciones`, webhook de pago, consumidor de `SuscripcionActivada` (marca premium), tests de publicador/consumidores, documentar colas y flujo de pago. Revisa PR de P2.

## Flujo de trabajo (GitHub Flow)

Todo se explica en detalle en `GITHUB_FLOW.md`. Resumen:

1. Nadie pushea directamente sobre `main`. Todo entra por Pull Request con 1 aprobación y tests en verde.
2. Una tarea = un issue = una rama = un PR. Rama: `p<rol>/<tarea-corta>` (ej. `p2/soap-talles`). Vive 1-2 días máximo.
3. Revisores fijos: P1↔P4, P2 revisa a P3, P3 revisa a P5, P5 revisa a P2.
4. PR con `Closes #<issue>`, checklist (compila, tests pasan, probado en local, Swagger actualizado si cambió la API).
5. Merge con **Squash and merge**, se borra la rama.

## Queda para el Integrador (no se toca en estos 10 días)

Try-on y maniquí, outfits, login con Firebase real, historial de precios, búsqueda por imagen subida, más tiendas, pruebas de carga y diagramas completos.

## Equipo

- **Profesor:** Parkinson, Christian Anibal — cparkinson@uade.edu.ar
- **Profesor auxiliar:** Bianca, Nahuel — nbianca@uade.edu.ar

### Integrantes

1. Aldaz, Luciano Nahuel — Legajo 1139484 — lualdaz@uade.edu.ar
2. Audine, Matias Gabriel — Legajo 1172179 — maudine@uade.edu.ar
3. Frisoli Pirc, Salvador — Legajo 1112365 — SAFRISOLIPIRC@uade.edu.ar
4. Mendez, Nicolas — Legajo 1172141 — nicomendez@uade.edu.ar
5. Vignolo, Nahir Yael — Legajo 1137587 — nvignolo@uade.edu.ar

## Créditos

Guía del equipo y plan de etapas: `@Salva` (5 nov 2026).
