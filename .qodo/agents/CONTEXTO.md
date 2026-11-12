# Contexto del proyecto — Agregador de ropa multi-tienda

## Idea

App web tipo Mercado Libre pero solo para ropa, con artículos de múltiples tiendas.
Proyecto universitario de **Desarrollo de Aplicaciones II — UADE**.

La Segunda Parte del TP (P2) apunta a una arquitectura de integración **sincrónica y
asincrónica**: SOAP, REST con Swagger, RabbitMQ con productores y consumidores, dos
servicios separados, una API externa real y un componente de IA simple.

## Alcance de la Segunda Parte

### Dentro del alcance

- Catálogo unificado de prendas de **1 o 2 tiendas reales**, obtenidas por scraping.
- Cada producto guarda la **URL oficial** de la tienda y puede repetirse entre tiendas.
- Stock simplificado: **disponible / sin stock** y **talles disponibles**.
- API REST de catálogo, tiendas, recomendaciones, similares y suscripciones.
- Servicio **SOAP** de equivalencias de talles AR/US/EU con WSDL.
- **RabbitMQ** con exchange, colas por consumidor y colas de error (DLQ).
- Eventos de dominio: `ProductoScrapeado`, `SuscripcionActivada`,
  `RecomendacionSolicitada`.
- **IA de recomendaciones por contenido**, expuesta por REST y por cola.
- **Ximilar** como segunda API externa para prendas similares.
- **MercadoPago sandbox** como API externa obligatoria para el plan premium.
- **Cuentas locales** con email y contraseña (hash BCrypt), sesión con token opaco y
  persistencia en PostgreSQL. No se usa Firebase ni Firestore.
- **Favoritos** por usuario, con pantalla `/favoritos` y menú desplegable en el header.
- **Modo invitado** por defecto: los favoritos viven en memoria y se pierden al recargar.
- Si un invitado con favoritos crea cuenta o inicia sesión, se le pregunta si quiere
  fusionarlos o descartarlos.
- Frontend mínimo de demo: catálogo, detalle, filtros, “Ver tienda”, favoritos, cuenta,
  premium, recomendaciones y similares.
- Docker Compose con PostgreSQL, RabbitMQ, backend, scraper y frontend.
- Swagger/OpenAPI, manejo global de errores, logging y variables de entorno.
- Tests unitarios de backend, frontend y conversores del scraper.

### Fuera del alcance (queda para el Integrador)

- Try-on o maniquí virtual.
- Outfits armados por el usuario.
- Login con Firebase/Firestore. La autenticación local con BCrypt y token sí está en
  alcance.
- Cuentas cloud o sincronización de favoritos entre dispositivos.
- Historial de precios.
- Búsqueda por imagen subida por el usuario.
- Más tiendas que las elegidas para la demo.
- Pruebas de carga.
- Diagramas completos de arquitectura.

## Decisiones confirmadas

- **Sin compra**: se elimina el carrito, `Pedido`, `DetallePedido` y sus tests. Los
  eventos pasan a ser de catálogo, suscripción y recomendaciones.
- **Datos por scraping** de 1 o 2 tiendas reales; cada producto guarda la URL oficial.
- **Stock**: solo `disponible` (boolean) y `tallesDisponibles` (lista de talles).
- **Sin deduplicación**: la misma prenda puede aparecer una vez por tienda.
- **IA**: recomendaciones por contenido con un modelo simple, por REST y por cola.
- **Cuentas locales**: PostgreSQL, contraseña con hash BCrypt y token opaco con
  vencimiento. No se usa Firebase ni Firestore.
- **Sesión**: el frontend guarda el token en `localStorage` y lo envía como
  `Authorization: Bearer <token>`. El invitado no se persiste.
- **Favoritos**: unique `usuario + producto`; registrados en la base, invitados en memoria.
- **Try-on, outfits y login con Firebase** no entran en esta etapa.

## Stack

- Backend: Java 17, Spring Boot 4.1, Maven, Spring Data JPA, PostgreSQL, Spring AMQP,
  Spring-WS, springdoc.
- Seguridad: BCrypt (`spring-security-crypto`) y token opaco con expiración.
- Scraper: Java 17 + Spring Boot, como segundo servicio independiente.
- Frontend: TypeScript, React 19 + Vite 8, CSS Modules.
- Mensajería: RabbitMQ.
- APIs externas: MercadoPago sandbox (pagos) y Ximilar (similares).
- Deploy: Docker + Docker Compose.
- Tests: JUnit 5 + Mockito (backend), Vitest + Testing Library (frontend).

## Requisitos de la consigna y cómo se cubren

| Requisito | Cómo se cumple | Evidencia |
|---|---|---|
| Al menos 1 servicio SOAP con WSDL | Equivalencias de talles AR/US/EU con Spring-WS | WSDL publicado y request/response de prueba |
| Al menos 2 servicios REST con OpenAPI | Catálogo, suscripciones, recomendaciones y similares | Swagger UI con todos los endpoints |
| Mensajería y colas | Exchange, colas por consumidor y DLQ | Panel de RabbitMQ con mensajes |
| Productores y consumidores | `ProductoScrapeado`, `SuscripcionActivada`, `RecomendacionSolicitada` | Logs de publicación y consumo |
| Arquitectura SOA / microservicios | Backend y scraper separados, comunicados por cola | Docker Compose con ambos servicios |
| Al menos 1 API externa real | MercadoPago sandbox para el plan premium | Pago de prueba que activa `premium` |
| Componente de IA por REST y cola | Recomendaciones por contenido | Tests con casos esperados y llamadas REST/cola |
| Segunda API externa | Ximilar para prendas similares | Respuesta real guardada |
| Errores, logging y configuración | `@RestControllerAdvice`, logs y variables de entorno | Error de ejemplo documentado en Swagger |
| Frontend para la demo | Catálogo, detalle, “Ver tienda” y botón premium | Capturas y video corto |

## Modelo de datos

### `Producto`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Clave primaria autogenerada |
| `idExterno` | String | Identificador del producto en la tienda de origen |
| `nombre` | String | Nombre de la prenda |
| `descripcion` | String | Descripción |
| `precio` | BigDecimal | Precio de la tienda |
| `categoria` | Enum | Tipo de prenda |
| `genero` | Enum | Público objetivo |
| `marca` | String | Marca |
| `tienda` | `Tienda` | Tienda de origen |
| `urlOriginal` | String | URL oficial del producto |
| `imagenUrl` | String | Imagen principal |
| `disponible` | boolean | Hay stock o no |
| `tallesDisponibles` | List<String> | Talles disponibles |

La combinación `tienda + idExterno` es **única**: permite hacer upsert cuando llega un
`ProductoScrapeado` sin duplicar el mismo producto de la misma tienda.

### `Tienda`

`id`, `nombre`, `urlBase`, `activa`.

### `Usuario`

`id`, `nombre`, `email`, `passwordHash`, `premium`.

Las cuentas son locales y se persisten en PostgreSQL. La contraseña se guarda como hash
BCrypt y el email es único. No se usa Firebase ni Firestore.

### `SesionUsuario`

`id`, `usuario`, `tokenHash`, `creadaEn`, `expiraEn`.

El frontend recibe un token opaco y lo envía como `Authorization: Bearer <token>`. En la
base solo se guarda el hash SHA-256 del token. El vencimiento se configura con
`AUTH_TOKEN_TTL_HOURS` (por defecto 168 horas).

### `Favorito`

`id`, `usuario`, `producto`, `fecha`.

La combinación `usuario + producto` es única. Los usuarios registrados guardan sus
favoritos en la base; los invitados los mantienen en memoria y los pierden al recargar.

### Categorías

`REMERA`, `PANTALON`, `BUZO`, `CAMPERA`, `ZAPATILLA`, `ZAPATO`, `ACCESORIO`, `VESTIDO`.

> Corrección respecto del código anterior: `REMPERA` pasa a `REMERA` y se agrega
> `VESTIDO`.

## Eventos y mensajería

Los eventos se documentan en `docs/eventos.md` con publicador, consumidor, cola y JSON de
ejemplo.

| Evento | Publica | Consume | Efecto |
|---|---|---|---|
| `ProductoScrapeado` | Scraper | Backend | Crea o actualiza el producto por `tienda + idExterno` |
| `SuscripcionActivada` | Backend (pago aprobado) | Backend | Marca al usuario como `premium` |
| `RecomendacionSolicitada` | Backend (REST) | Componente de IA | Calcula y guarda recomendaciones |

Topología objetivo:

- Un exchange principal para el sistema.
- Una cola por consumidor.
- Una cola de mensajes muertos (DLQ) por cada cola.
- Publicador genérico que reutiliza las confirmaciones del broker ya configuradas.

## API REST

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Catálogo con filtros y paginación |
| GET | `/api/productos/{id}` | Detalle de producto |
| GET | `/api/tiendas` | Listado de tiendas |
| GET | `/api/productos/{id}/similares` | Prendas similares de otras tiendas (Ximilar) |
| GET | `/api/recomendaciones?productoId=&limite=` | Recomendaciones por contenido |
| POST | `/api/recomendaciones/solicitudes` | Solicitud asincrónica de recomendaciones (`202`) |
| POST | `/api/auth/registro` | Crea una cuenta local y devuelve token + usuario |
| POST | `/api/auth/login` | Inicia sesión y devuelve token + usuario |
| POST | `/api/auth/logout` | Revoca el token de sesión |
| GET | `/api/auth/me` | Devuelve el usuario del token |
| GET | `/api/favoritos` | Lista los favoritos del usuario autenticado |
| POST | `/api/favoritos/{productoId}` | Agrega un favorito (idempotente) |
| DELETE | `/api/favoritos/{productoId}` | Quita un favorito |
| POST | `/api/suscripciones` | Crea el pago premium y devuelve la URL de MercadoPago |
| POST | `/api/suscripciones/webhook` | Recibe la confirmación de pago |
| GET | `/api/suscripciones/{id}/estado` | Consulta el estado del pago (plan B del webhook) |

Los endpoints de favoritos y suscripciones requieren `Authorization: Bearer <token>`.
El invitado no llama a estos endpoints: maneja sus favoritos en memoria.

Filtros de catálogo: `categoria`, `tienda`, `genero`, `precioMin`, `precioMax`, `texto`,
`pagina`, `tamano`.

## SOAP

Servicio de **equivalencias de talles** con Spring-WS:

- XSD y WSDL publicados.
- Tabla de equivalencias para **AR, US y EU**.
- Tipos de prenda: remeras, pantalones y calzado.
- Request de prueba que devuelve la equivalencia solicitada.

## APIs externas

### MercadoPago

- Cuenta de desarrollador y usuarios de prueba en sandbox.
- `POST /api/suscripciones` crea el pago del plan premium y devuelve la URL.
- El backend confirma el pago por webhook o, como plan B, al volver a `/pago/exito`.
- Si el pago está aprobado, publica `SuscripcionActivada` y el usuario pasa a
  `premium = true`.

### Ximilar

- `XimilarFacade` en el backend (patrón Facade).
- Sube imágenes del catálogo a una colección y busca similares.
- Timeout configurado y perfil mock con respuestas fijas para tests y demo sin red.
- Límites del plan gratuito: 1.000 créditos por mes; subir una imagen cuesta 0,25
  créditos y etiquetar una foto cuesta 15. **No se usa tagging.**

## IA de recomendaciones

Regla de puntaje por par de prendas:

- Misma categoría o categoría complementaria.
- Mismo género.
- Precio parecido.
- Misma marca.
- Disponibilidad.

Componentes:

- Interfaz `EstrategiaRecomendacion` (Strategy).
- `RecomendacionPorContenido`: recomendaciones por similitud de contenido.
- `RecomendacionComplementaria`: arma un conjunto con parte de arriba, parte de abajo y
  calzado.
- `RecomendadorFactory` (Factory) que elige la estrategia.
- Exposición por REST y por cola.
- Documentación en `docs/ia.md`: modelo, validación, limitaciones y evidencias.

## Arquitectura de servicios

```
                         ┌──────────────────┐
                         │    Frontend      │
                         │ React + Vite     │
                         └────────┬─────────┘
                                  │ REST/JSON
                                  ▼
┌──────────────┐         ┌──────────────────┐         ┌──────────────┐
│   Scraper    │  AMQP   │     Backend      │  AMQP   │  Componente  │
│ Spring Boot  ├────────►│  Spring Boot     │◄────────┤   de IA      │
└──────┬───────┘         └───┬──────┬───────┘         └──────────────┘
       │                     │      │
       │ snapshots           │      │ REST
       ▼                     ▼      ▼
  tiendas reales        PostgreSQL  MercadoPago / Ximilar
```

- **Backend**: API REST, SOAP, cuentas locales, sesiones, favoritos, persistencia,
  publicación y consumo de eventos, integración con MercadoPago y Ximilar.
- **Scraper**: lee catálogos reales, normaliza productos y publica `ProductoScrapeado`.
- **Componente de IA**: consume `RecomendacionSolicitada` y calcula recomendaciones.
- **Frontend**: demo del catálogo, cuentas, favoritos y de las integraciones.

## Frontend esperado

### Rutas

| Ruta | Pantalla | Protegida |
|---|---|---|
| `/login` | Ingresar / Crear cuenta | No |
| `/` | Catálogo | No |
| `/productos/:id` | Detalle | No |
| `/favoritos` | Favoritos (invitado o registrado) | No |
| `/planes` | Planes y botón premium | Sí |
| `/pago/exito` | Resultado exitoso del pago | No |
| `/pago/error` | Pago rechazado o cancelado | No |
| `*` | 404 | No |

### Funcionalidad

- Catálogo, detalle y favoritos **públicos**, sin `ProtectedRoute`.
- Filtros por categoría, tienda, género, precio mínimo/máximo y texto.
- Detalle con “Disponible / Sin stock”, talles y botón **“Ver tienda”** que abre la URL
  oficial en otra pestaña.
- Bloque **“Te puede gustar”** con `GET /api/recomendaciones?productoId=`.
- Bloque **“Similares en otras tiendas”** con `GET /api/productos/{id}/similares`.
- Página de planes con botón **“Hacerme premium”** que llama a `POST /api/suscripciones`
  y redirige a MercadoPago.
- **Invitado por defecto**: sin token, los favoritos viven en memoria y se pierden al
  recargar. Se avisa en `/favoritos`.
- **Cuenta local**: `/login` con pestañas Ingresar / Crear cuenta; la sesión se guarda en
  `localStorage` y se envía como `Authorization: Bearer <token>`.
- **Botón de cuenta arriba a la izquierda** con dropdown para ingresar, crear cuenta,
  ver favoritos, ir a planes o cerrar sesión.
- **Menú desplegable de favoritos** en el header, con contador, miniaturas y “Ver todos”.
- **Pantalla `/favoritos`** para invitados y registrados, con quitar favorito y abrir la
  tienda original.
- Si un invitado con favoritos crea cuenta o inicia sesión, se le pregunta si quiere
  fusionarlos o descartarlos.
- Se elimina el carrito: `CarritoContext`, `CarritoMenu`, `useCarrito`, `crearPedido` y
  sus tests.

## Docker e infraestructura

`docker-compose.yml` debe levantar:

- PostgreSQL.
- RabbitMQ.
- Backend.
- Scraper.
- Frontend.

Cada servicio con su `Dockerfile`. Las credenciales y URLs van en `.env`; el repositorio
solo versiona `.env.example` con los nombres de las variables.

## Tareas por área

### 1. Datos, catálogo y modelo

- [ ] Definir el modelo nuevo en `docs/modelo.md`.
- [ ] Corregir `Categoria`: `REMPERA` → `REMERA` y agregar `VESTIDO`.
- [ ] Crear entidades, repositorios y DTOs de `Producto`, `Tienda` y `Usuario`.
- [ ] Usar ids autogenerados y restricción única `tienda + idExterno`.
- [ ] Implementar `GET /api/productos` con filtros y paginación.
- [ ] Implementar `GET /api/productos/{id}` y `GET /api/tiendas`.
- [ ] Eliminar el código de compra: `Pedido`, `DetallePedido`, `PedidoController`,
  `PedidoCompraService`, `PedidoService`, `InventarioService`, `StockMapConverter`,
  `PUT /stock` y sus tests.
- [ ] Cargar el seed con el snapshot de productos reales del scraper.
- [ ] Configurar Swagger UI con springdoc compatible con Spring Boot 4.1.
- [ ] Agregar `@RestControllerAdvice`, CORS global y credenciales por variables de entorno.

### 2. Scraper

- [ ] Elegir 2 tiendas argentinas con catálogo accesible.
- [ ] Verificar la URL del catálogo en el navegador y revisar `robots.txt`.
- [ ] Crear el proyecto `scraper/` como servicio Spring Boot.
- [ ] Implementar la interfaz `ScraperTienda` con el patrón Strategy.
- [ ] Implementar la tienda 1 y guardar el snapshot en `scraper/snapshots/tienda1.json`.
- [ ] Implementar la tienda 2 como otra implementación de `ScraperTienda`.
- [ ] Convertir cada prenda al formato acordado con el backend.
- [ ] Publicar cada prenda como `ProductoScrapeado` en RabbitMQ.
- [ ] Consumir `ProductoScrapeado` en el backend y hacer upsert por `tienda + idExterno`.
- [ ] Agregar ejecución automática diaria y un comando manual para la demo.
- [ ] Agregar `Dockerfile` del scraper.
- [ ] Testear los conversores de cada tienda con los JSON guardados, sin internet.

### 3. SOAP

- [ ] Crear el XSD de equivalencias de talles.
- [ ] Implementar el endpoint SOAP con Spring-WS.
- [ ] Cargar la tabla de equivalencias AR/US/EU para remeras, pantalones y calzado.
- [ ] Verificar que el WSDL se vea en el navegador.
- [ ] Probar un request de ejemplo y documentar la respuesta.
- [ ] Agregar tests unitarios del servicio SOAP.

### 4. RabbitMQ y eventos

- [ ] Escribir `docs/eventos.md` con publicador, consumidor, cola y JSON de cada evento.
- [ ] Crear la topología: exchange, colas por consumidor y DLQ por cola.
- [ ] Reemplazar `PedidoCreado` por un publicador genérico.
- [ ] Reutilizar las confirmaciones del broker ya configuradas.
- [ ] Dejar las clases de eventos y la configuración en `main`.
- [ ] Agregar un test que publique y consuma un evento de prueba.
- [ ] Documentar las colas y capturas del panel de RabbitMQ.

### 5. IA de recomendaciones

- [ ] Escribir la regla del recomendador en `docs/ia.md`.
- [ ] Implementar `EstrategiaRecomendacion` y `RecomendacionPorContenido`.
- [ ] Implementar `RecomendadorFactory`.
- [ ] Agregar 5 tests unitarios con resultado esperado.
- [ ] Implementar `GET /api/recomendaciones?productoId=&limite=`.
- [ ] Implementar `POST /api/recomendaciones/solicitudes` con respuesta `202` y evento.
- [ ] Implementar el consumidor que procesa y guarda el resultado.
- [ ] Implementar `RecomendacionComplementaria`.
- [ ] Completar la tabla de validación con 10 casos.
- [ ] Documentar modelo, validación, limitaciones y evidencias en `docs/ia.md`.

### 6. Similares con Ximilar

- [ ] Crear la cuenta de Ximilar y hacer una llamada de prueba.
- [ ] Implementar `XimilarFacade` en el backend.
- [ ] Subir imágenes del catálogo a una colección.
- [ ] Buscar similares con timeout configurado.
- [ ] Agregar perfil mock con respuestas fijas.
- [ ] Implementar `GET /api/productos/{id}/similares`.
- [ ] Verificar que devuelva prendas de otra tienda.
- [ ] Documentar créditos usados y capturas de evidencia.

### 7. MercadoPago y suscripciones

- [ ] Crear la cuenta de desarrollador y los usuarios de prueba de MercadoPago.
- [ ] Crear la entidad `Suscripcion` (`usuario`, `estado`, `idPago`, `fecha`).
- [ ] Implementar `POST /api/suscripciones` con la URL de pago.
- [ ] Implementar el webhook que consulta el estado del pago.
- [ ] Publicar `SuscripcionActivada` cuando el pago esté aprobado.
- [ ] Implementar el consumidor que marca `premium = true` (Observer).
- [ ] Exponer el backend con ngrok para que MercadoPago llegue al webhook.
- [ ] Implementar el plan B: consultar el estado al volver a `/pago/exito`.
- [ ] Testear el publicador y los consumidores.
- [ ] Documentar el flujo de pago y capturas del sandbox.

### 8. Cuentas, sesión y favoritos

- [ ] Agregar `passwordHash` a `Usuario` y `findByEmailIgnoreCase`.
- [ ] Implementar `AuthService`, `TokenService` y `AuthController`.
- [ ] Exponer `/api/auth/registro`, `/api/auth/login`, `/api/auth/logout` y
  `/api/auth/me`.
- [ ] Hashear contraseñas con BCrypt y validar nombre, email y contraseña.
- [ ] Emitir tokens opacos, guardar solo SHA-256 y aplicar vencimiento configurable.
- [ ] Agregar Flyway y migración V2: `password_hash`, borrado de usuarios sin contraseña,
  `sesion_usuario` y `favorito`.
- [ ] Crear `Favorito` con unique `usuario + producto`, repositorio, servicio y controller.
- [ ] Exponer `GET/POST/DELETE /api/favoritos` resolviendo el usuario desde el token.
- [ ] Resolver también el usuario de `POST /api/suscripciones` desde el token.
- [ ] Tests de registro, login, token válido/expirado/revocado, favoritos y 401/409.

### 9. Frontend

- [ ] Levantar el frontend contra el backend de `main` y ver el catálogo.
- [ ] Eliminar el carrito y `crearPedido`.
- [ ] Sacar `ProtectedRoute` de catálogo y detalle.
- [ ] Adaptar el tipo `Producto` a `disponible`, `tallesDisponibles`, `urlOriginal`,
  `tienda` y `marca`.
- [ ] Implementar filtros de categoría, tienda, género, precio y texto.
- [ ] Mostrar “Disponible / Sin stock”, talles y “Ver tienda”.
- [ ] Implementar `AuthContext` con registro, login, logout y restauración desde
  `localStorage`.
- [ ] Implementar `/login` con pestañas Ingresar / Crear cuenta.
- [ ] Arrancar en modo invitado si no hay token.
- [ ] Agregar botón de cuenta arriba a la izquierda con dropdown.
- [ ] Agregar `FavoritosContext` con fuente en memoria para invitado y API para registrado.
- [ ] Agregar corazón en tarjeta y detalle, pantalla `/favoritos` y menú desplegable.
- [ ] Preguntar al invitado si fusiona o descarta sus favoritos al crear cuenta o iniciar
  sesión.
- [ ] Agregar “Te puede gustar” con `GET /api/recomendaciones`.
- [ ] Agregar “Similares en otras tiendas” con `GET /api/productos/{id}/similares`.
- [ ] Agregar página de planes y botón “Hacerme premium” usando el token.
- [ ] Agregar páginas `/pago/exito` y `/pago/error`.
- [ ] Agregar `Dockerfile` del frontend.
- [ ] Probar el flujo completo integrado.

### 10. Infraestructura y entrega

- [ ] Completar `docker-compose.yml` con PostgreSQL, RabbitMQ, backend, scraper y frontend.
- [ ] Crear `.env.example` con todas las variables.
- [ ] Verificar `docker compose up` en una máquina limpia.
- [ ] Configurar GitHub: proteger `main`, etiquetas por área, milestones técnicos, tablero
  en GitHub Projects y plantilla de Pull Request.
- [ ] Configurar GitHub Actions con `mvn test` y `npm test`.
- [ ] Actualizar READMEs con cómo levantar, endpoints, SOAP y arquitectura.
- [ ] Juntar evidencias en `docs/evidencias/`: una captura por requisito y un video corto
  del flujo completo.
- [ ] Ensayar la demo integrada.

## Dependencias técnicas entre tareas

1. El modelo de datos y los contratos REST deben estar definidos antes de adaptar el
   frontend y el scraper.
2. La topología de RabbitMQ y las clases de eventos deben existir antes de que el scraper
   publique y antes de que la IA consuma solicitudes.
3. El endpoint de catálogo y el seed deben existir antes de la demo del frontend.
4. La integración con Ximilar necesita el catálogo cargado.
5. La integración con MercadoPago necesita la entidad `Suscripcion` y el token de sesión.
6. Las cuentas, el token y la migración V2 deben existir antes de los favoritos de
   registrados.
7. El frontend React debe estar migrado antes de la UI de cuenta y favoritos.
8. Docker Compose completo se cierra cuando backend, scraper y frontend funcionan por
   separado.

## Riesgos técnicos y plan B

| Riesgo | Plan B |
|---|---|
| La tienda bloquea el scraper o cambia el sitio | Usar el snapshot guardado en `scraper/snapshots/` para la demo |
| El webhook de MercadoPago no llega a la máquina local | Consultar el estado del pago al volver a `/pago/exito` |
| Ximilar no responde o se terminan los créditos | Perfil mock con respuestas fijas |
| La cátedra pide literalmente `PedidoCreado` | Confirmar el alcance con el docente antes de implementar |
| El token vence durante la demo | TTL configurable y cierre de sesión limpio |
| El invitado pierde favoritos al recargar | Es el comportamiento pedido; se avisa en `/favoritos` |
| La base existente no tiene Flyway | `baseline-on-migrate` y migración V2 idempotente |
| El frontend se atrasa | Recortar primero las pantallas secundarias y conservar catálogo, detalle y demo |

## Evidencias de entrega

- Swagger UI con todos los endpoints.
- WSDL publicado y request/response SOAP.
- Panel de RabbitMQ con exchange, colas, DLQ y mensajes.
- Logs de publicación y consumo de los tres eventos.
- Pago de prueba en sandbox que activa `premium`.
- Respuesta real de Ximilar guardada.
- Registro, login, restauración de sesión y logout funcionando.
- Capturas de `/favoritos` como invitado y como registrado.
- Captura del modal de fusión de favoritos.
- Tests con casos esperados de recomendaciones, auth y favoritos.
- Capturas del frontend y video corto del flujo completo.
- `docker compose up` funcionando en una máquina limpia.
