# Web de Ropa — TP Segunda Parte

Agregador de ropa con integración sincrónica y asincrónica.
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
- **frontend mínimo**: landing de inicio con una portada por tienda (Sportline, Newsport y Grid; cada una abre el catálogo ya filtrado, ej. `#/catalogo?tienda=1`), catálogo y detalle públicos, "Ver en tienda", filtros, bloque "Te puede gustar", página Planes con botón "Hacerme premium" que redirige a Mercado Pago.

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

### Requisitos

- [Git](https://git-scm.com/downloads) y [Docker Desktop](https://www.docker.com/products/docker-desktop/)
  (en Windows, con WSL 2). **Docker Desktop tiene que estar abierto** antes de levantar.
- Libres los puertos **5432, 5672, 15672, 8080 y 5173**. Si tenés instalado Postgres o RabbitMQ en tu
  PC, detené esos servicios: si no, el contenedor no arranca ("port is already allocated").
- No hace falta instalar Java, Maven ni Node: todo se compila dentro de Docker.

### Paso a paso

1. **Clonar el repo** y entrar a la carpeta:

   ```powershell
   git clone https://github.com/Audine-Matias-Gabriel/DDAII.git
   ```

   ```powershell
   cd DDAII
   ```

2. **Crear el `.env`** a partir del ejemplo. En PowerShell (Windows):

   ```powershell
   Copy-Item .env.example .env
   ```

   En Git Bash, Linux o Mac: `cp .env.example .env`.

3. **Completar las claves** en `.env` (el archivo no se sube al repo; las claves se pasan por privado,
   nunca por el repo ni el chat del grupo):

   | Variable | Qué poner | Si la dejás vacía |
   |---|---|---|
   | `MP_ACCESS_TOKEN` | Access token **de prueba de la cuenta vendedora** de Mercado Pago (Tus integraciones → Credenciales de prueba) | "Hacerme premium" responde un error claro (502) |
   | `XIMILAR_API_KEY` (o `XIMILAR_API_TOKEN`) y `XIMILAR_COLLECTION_ID` | Token y colección de Ximilar. **Leé [Ximilar en otra PC](#ximilar-en-otra-pc) antes de usar la colección del equipo** | "Similares" muestra productos de la misma categoría en la otra tienda |

   Todo lo demás ya tiene un valor que funciona con Docker.

4. **Levantar todo:**

   ```powershell
   docker compose up -d --build
   ```

   La primera vez tarda unos minutos (compila el backend y el front). El **scraper** corre al arrancar y
   en 1 o 2 minutos llena el catálogo con las dos tiendas; después corre solo todos los días a las 2:00.
   Para ver cómo va (salís con `Ctrl+C`, no apaga nada):

   ```powershell
   docker compose logs -f scraper
   ```

   Está listo cuando dice `Eventos confirmados` para Sportline y para Grid.

5. **Abrir:**

   | Qué | Dónde |
   |---|---|
   | Tienda | http://localhost:5173 |
   | Swagger UI | http://localhost:8080/swagger-ui/index.html |
   | WSDL (SOAP) | http://localhost:8080/ws/equivalencias-talles.wsdl |
   | Panel de RabbitMQ | http://localhost:15672 (usuario y clave: `guest`) |

### Pagar en el sandbox

Para probar el plan premium: crear una cuenta en la tienda, ir a **Planes → Hacerme premium** y pagar en
Mercado Pago **con la cuenta compradora de prueba** (no con una cuenta real). La cuenta vendedora es la
del `MP_ACCESS_TOKEN`. En local Mercado Pago no vuelve solo a la tienda: volvé a Planes y se confirma solo
(o con "Verificar pago"). Detalle en [docs/pagos.md](docs/pagos.md).

### Ximilar en otra PC

Ximilar guarda cada imagen con el **id que el producto tiene en tu base de datos** (`producto-<id>`).
Cada PC tiene su propia base y numera los productos distinto, así que **una colección de Ximilar sirve
para una sola base de datos**. **No compartas tu token ni tu colección con otra PC:**

- En la otra PC los similares salen equivocados: busca con tus imágenes y devuelve ids que en su base
  son otros productos.
- La otra PC sube sola sus imágenes cada 10 minutos: agrega sus productos a tu colección y, si cambia la
  imagen de un producto, **borra y reemplaza tu registro** con ese id. Tus similares también se rompen.
- Gastan los mismos créditos (1.000 por mes para toda la cuenta), y el token da acceso a toda la cuenta.

En una PC que no es la que cargó la colección del equipo, elegí una:

- **Sin Ximilar** (alcanza para desarrollar y probar): dejá `XIMILAR_API_KEY` vacío o poné
  `XIMILAR_MOCK=true`. "Similares" usa el respaldo por categoría y todo lo demás anda igual.
- **Con tu propia cuenta de Ximilar:** creá una cuenta gratuita en https://app.ximilar.com, una colección
  de Product Similarity, y usá **tu** token y **tu** `XIMILAR_COLLECTION_ID`. Tiene que ser una cuenta
  aparte: el plan gratuito permite **una sola colección por cuenta**, así que no se puede crear otra en la
  cuenta del equipo. El catálogo se sube solo (unos 134 créditos de los 1.000 del mes).

Para la demo con Ximilar real, usá la PC que cargó la colección del equipo.

### Apagar, prender y empezar de cero

| Para | Comando |
|---|---|
| Apagar (conserva los datos) | `docker compose stop` |
| Volver a prender | `docker compose start` |
| Actualizar después de un `git pull` | `docker compose up -d --build` |
| Borrar los contenedores (conserva la base) | `docker compose down` |
| **Empezar de cero** (borra la base: usuarios, suscripciones, productos) | `docker compose down -v` |

> ⚠️ `docker compose down -v` borra la base de datos. Si esa PC tiene Ximilar activo, la base nueva numera
> los productos distinto y la colección queda desincronizada: después de empezar de cero hay que vaciar la
> colección (con el plan gratuito no se puede crear otra en la misma cuenta). Si no hace falta empezar de
> cero, usá `docker compose down` sin `-v`.

### Si algo no anda

| Síntoma | Qué hacer |
|---|---|
| `port is already allocated` | Otro programa usa ese puerto (Postgres, RabbitMQ u otro proyecto). Cerralo y volvé a levantar |
| `Cannot connect to the Docker daemon` / `pipe ... docker_engine` | Abrí Docker Desktop y esperá a que diga que está corriendo |
| El catálogo dice "No se encontraron productos" | El scraper todavía no terminó: mirá `docker compose logs -f scraper` |
| "Hacerme premium" da error 502 | Falta `MP_ACCESS_TOKEN` en `.env`. Después de cambiar el `.env`: `docker compose up -d` |
| Cambié el `.env` y no toma los valores | `docker compose up -d` (recrea los contenedores con las variables nuevas) |

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
| GET | `/api/productos/{id}/historial-precios` | Serie `[{ "fecha": "2026-11-25", "precio": 1500 }]` con un precio por día (snapshots del scraper). El frontend la grafica solo para usuarios premium |
| GET | `/api/tiendas` | Tiendas del catálogo |
| GET | `/api/recomendaciones?productoId=&limite=&estrategia=` | Recomendaciones (IA, sincrónico). `estrategia`: `contenido` (parecidas) o `complementaria` (arma un conjunto). Con sesión premium prioriza sus marcas/categorías de interés. Ver [docs/ia.md](docs/ia.md) |
| POST 🔒 | `/api/recomendaciones/vistas/{productoId}` | Registra una vista del premium para personalizar recomendaciones futuras (403 si no es premium) |
| POST | `/api/recomendaciones/solicitudes` | Pide recomendaciones por cola: responde 202 y publica `RecomendacionSolicitada` (incluye `usuarioId` si hay sesión) |
| GET | `/api/recomendaciones/solicitudes/{solicitudId}` | Resultado que guardó el consumidor de la cola (404 mientras no se procesó) |
| GET | `/api/suscripciones/plan` | Precio del plan premium (el que se cobra en Mercado Pago) |
| POST 🔒 | `/api/suscripciones` | Crea el pago premium y devuelve la URL de Mercado Pago |
| POST 🔒 | `/api/suscripciones/{id}/confirmar?paymentId=` | Confirma el pago al volver de Mercado Pago |
| POST 🔒 | `/api/suscripciones/verificar` | Busca en Mercado Pago pagos aprobados de las suscripciones pendientes |
| GET 🔒 | `/api/suscripciones/{id}/estado` | Estado de una suscripción propia |
| POST | `/api/suscripciones/webhook` | Webhook de Mercado Pago (`type=payment&data.id=`): consulta el pago y aprueba la suscripción. Ver [docs/pagos.md](docs/pagos.md) |
| GET 🔒 / POST 🔒 / DELETE 🔒 | `/api/favoritos`, `/api/favoritos/{productoId}` | Favoritos del usuario |
| POST 🔒 | `/api/similares/indexar?maximo=` | Sube a Ximilar las imágenes pendientes del catálogo |
| POST 🔒 (premium) | `/api/premium/busqueda-visual` | `multipart/form-data` con campo `imagen` (PNG/JPG/TIFF, máx. 5 MB) y `limite` opcional. Devuelve `[{ "producto": {...}, "distancia": 0.42 }]` del más parecido al menos parecido. Ver [búsqueda visual](#búsqueda-visual-premium) |
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
   **Una colección por base de datos:** ver [Ximilar en otra PC](#ximilar-en-otra-pc).
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

## Búsqueda visual premium

`POST /api/premium/busqueda-visual` recibe la foto que sube el usuario y la envía a la misma colección
de product-similarity (`visualKNN` con `query_record._base64`, sin publicar la imagen en ninguna URL).
Solo responde para usuarios con `premium = true`: sin sesión da 401, con sesión no premium da 403.
En el frontend, el menú muestra **Búsqueda visual** solo cuando el usuario es premium y lo lleva a `#/busqueda-visual`,
donde puede elegir una foto (JPEG/PNG/TIFF, máx. 5 MB), ver una vista previa y explorar los productos encontrados.

```powershell
curl.exe -X POST http://localhost:8080/api/premium/busqueda-visual `
  -H "Authorization: Bearer <token>" `
  -F "imagen=@C:\ruta\a\foto.jpg" -F "limite=8"
```

Cada resultado es `{ "producto": { ...ProductoDTO... }, "distancia": 0.42 }` (menor distancia = más parecido),
en el orden que devolvió Ximilar. Los `_id` de Ximilar se resuelven a productos del catálogo; los registros
que no son productos se descartan con el filtro `tienda_id`.

**Fotos que no son prendas:** Ximilar devuelve vecinos aunque la imagen no tenga nada que ver, así que se
descartan los resultados con distancia mayor a `XIMILAR_VISUAL_MAX_DISTANCIA` (default `1.0`; con `0` se
aceptan todos). Si no queda ninguno válido, se reintenta ampliando `k` hasta
`XIMILAR_VISUAL_MAX_INTENTOS` (default 3; cada intento gasta créditos) y después se corta con
**422 `Sin coincidencias`**. En cuanto aparece una coincidencia válida se termina, sin gastar el resto.

| Variable | Default | Para qué sirve |
|---|---|---|
| `XIMILAR_VISUAL_MAX_INTENTOS` | `3` | Tope de búsquedas a Ximilar por foto |
| `XIMILAR_VISUAL_RESULTADOS_POR_INTENTO` | `5` | Vecinos pedidos en el primer intento (crece en cada reintento) |
| `XIMILAR_VISUAL_MAX_DISTANCIA` | `1.0` | Distancia máxima aceptada como "parecida" |
| `XIMILAR_VISUAL_MAX_IMAGEN_BYTES` | `5242880` | Tamaño máximo de la imagen (5 MB) |

**Créditos usados (18/11/2026):** carga inicial de 537 imágenes = **134,25 créditos**, más unas 15 búsquedas de prueba (~4 créditos). En total, unos **138 de los 1.000** del mes. La colección tiene 561 registros: 537 productos y 24 de pruebas anteriores, que la búsqueda ignora porque no tienen `tienda_id`. Evidencia en [docs/evidencias](docs/evidencias/README.md).

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
