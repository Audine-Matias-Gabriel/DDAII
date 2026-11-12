# Plan de 10 días — Web de Ropa (resumen)

6 fases con 3 hitos de control. En cada hito el equipo se junta 30 min y prueba lo que hay en `main`; lo que no llegó se avisa y se replanifica ese mismo día.

## Fases

| Fase | Días | Contenido | Hito |
|---|---|---|---|
| 1. Arranque | Día 1 | GitHub configurado, prototype mergeado, modelo y eventos escritos, cuentas de Mercado Pago y Ximilar creadas. | — |
| 2. Base | Días 2 y 3 | Se borra el código de compra y cada uno arma la base de su parte. | Hito 1: catálogo con Swagger, RabbitMQ con topología nueva, scraper con snapshot y recomendador con tests. |
| 3. Integraciones | Días 4 a 6 | SOAP, scraper publicando en la cola, pago de prueba, recomendaciones por REST y por cola, similares. | Hito 2: cada requisito obligatorio funciona por separado. |
| 4. Unir y probar | Días 7 y 8 | Todo junto en `main` y en `docker compose`. | Hito 3: desde acá no entran funciones nuevas, solo arreglos. |
| 5. Evidencias y documentación | Día 9 | Evidencias en `docs/evidencias/` y documentación. | — |
| 6. Ensayo y entrega | Día 10 | Ensayo de la demo con todo el equipo. Día de margen para imprevistos. | — |

## Requisitos obligatorios (prioridad 1)

1. ≥ 1 servicio SOAP con WSDL (equivalencias de talles AR/US/EU) — P2
2. ≥ 2 servicios REST con OpenAPI (catálogo, suscripciones, recomendaciones) — P2 (Swagger) + cada dueño
3. Mensajería y colas RabbitMQ (exchange, colas, DLQ) — P5
4. Productores y consumidores (ProductoScrapeado, SuscripcionActivada, RecomendacionSolicitada) — P5 coordina; P2, P3, P4 implementan
5. Arquitectura SOA / 2 servicios (backend + scraper) por la cola — P3 y P2
6. ≥ 1 API externa real (Mercado Pago) — P5
7. Componente de IA (recomendaciones por contenido, REST y cola) — P4

## Tareas por integrante

### P1 — Frontend y evidencias (revisa PR de P4)
- Día 1: levantar frontend contra el backend de main; borrar carrito (CarritoContext, CarritoMenu, useCarrito, crearPedido y sus tests).
- Días 2-3: catálogo y detalle públicos (sacar ProtectedRoute), adaptar tipo Producto a los nuevos campos de P2, filtros (categoría, tienda, género, precio mínimo/máximo, texto).
- Días 4-6: login simple (guarda usuarioId en header `X-Usuario-Id`, acordar con P5), bloque "Te puede gustar" (`GET /api/recomendaciones?productoId=`), página Planes con botón "Hacerme premium" (`POST /api/suscripciones`, redirige a Mercado Pago, páginas `/pago/exito` y `/pago/error`), bloque "Similares en otras tiendas" (`GET /api/productos/{id}/similares`).
- Días 7-8: probar flujo completo en main y arreglar lo que falle; Dockerfile del frontend (`docker compose up` sirve la web).
- Día 9: juntar evidencias en `docs/evidencias/` (una captura por fila de la tabla de la consigna + video de 2-3 min del flujo completo).
- Día 10: ensayo de la demo.

### P2 — Backend core, catálogo y SOAP (revisa PR de P3; coordina la integración en main)
- Día 1: mergear `prototype` a `main` y avisar al grupo (main tiene RabbitMQ y compila); escribir el modelo nuevo en `docs/modelo.md` (Producto, Tienda, Usuario).
- Día 2: borrar código de compra (Pedido, DetallePedido, PedidoController, PedidoCompraService, PedidoService, InventarioService, StockMapConverter, PUT /stock y sus tests); corregir Categoría (REMPERA→REMERA, agregar VESTIDO); entidades, repositorios y DTOs del modelo nuevo (ids con @GeneratedValue, tienda + idExterno único).
- Día 3: GET /api/productos con filtros y paginación; GET /api/productos/{id}; GET /api/tiendas; seed nuevo con snapshot de productos reales que pasa P3; Swagger UI con springdoc (compatible con la versión de Spring Boot), @RestControllerAdvice, CORS global y credenciales en variables de entorno. **Hito 1.**
- Día 4: SOAP de equivalencias de talles con Spring-WS (XSD, endpoint, tabla AR/US/EU para remeras, pantalones y calzado). WSDL se ve en el navegador y un request de prueba devuelve la equivalencia.
- Día 5: consumidor de ProductoScrapeado (crea o actualiza producto por tienda + idExterno).
- Día 6: tests unitarios de ProductoService, del upsert y del SOAP. **Hito 2.**
- Días 7-8: Dockerfile del backend y docker-compose con Postgres, RabbitMQ, backend, scraper y frontend; .env.example con todas las variables; probar todo integrado en main (`docker compose up` levanta todo en una máquina limpia). **Hito 3.**
- Día 9: README (cómo levantar, lista de endpoints, cómo probar el SOAP, diagrama simple de arquitectura servicios y cola).
- Día 10: ensayo de la demo.

### P3 — Scraper y Ximilar (revisa PR de P5)
- Día 1: elegir 2 tiendas de ropa argentinas (priorizar las que publican catálogo en JSON, ej. Shopify `/products.json`; VTEX tiene endpoints públicos); verificar abriendo la URL y revisar `robots.txt`; crear cuenta de Ximilar (plan gratuito) y hacer un llamado de prueba con una foto de la tienda 1.
- Día 2: crear el proyecto `scraper/` (Java + Spring Boot para que el resto pueda revisarlo); leer el catálogo de la tienda 1 y convertir cada prenda al formato acordado con P2 (`docs/modelo.md`); guardar el resultado en `scraper/snapshots/tienda1.json` (≥30 prendas con imagen, precio, talles y URL oficial).
- Día 3: pasarle el snapshot a P2 para el seed; ordenar el código con interfaz `ScraperTienda` (patrón Strategy) e implementación de la tienda 1. **Hito 1.**
- Día 4: publicar cada prenda como `ProductoScrapeado` en RabbitMQ (configuración de P5). Los mensajes se ven en el panel de RabbitMQ.
- Día 5: tienda 2 como otra implementación de `ScraperTienda`; ejecución automática 1 vez por día y un comando para correrlo a mano en la demo.
- Día 6: `XimilarFacade` en el backend (patrón Facade): sube imágenes del catálogo a una colección de Ximilar y busca similares (timeout + perfil mock con respuestas fijas); GET `/api/productos/{id}/similares` devuelve prendas de la otra tienda. **Hito 2.**
- Días 7-8: Dockerfile del scraper; tests del convertidor de cada tienda usando los JSON guardados (sin internet).
- Día 9: documentar en el README: tiendas elegidas, cómo correr el scraper, créditos de Ximilar usados, capturas de evidencia.
- Día 10: ensayo de la demo.

> Cuidado con Ximilar: el plan gratuito da 1.000 créditos/mes. Subir una imagen a la colección cuesta 0,25 créditos, pero taggear una foto cuesta 15. En estos 10 días no se usa tagging.

### P4 — IA de recomendaciones y configuración de GitHub (revisa PR de P1)
- Día 1: configurar GitHub (proteger main: 1 aprobación y tests en verde), etiquetas P1 a P5, milestones Hito 1/2/3, tablero en GitHub Projects y plantilla de PR. GitHub Actions que corre `mvn test` y `npm test` en cada PR. Listo cuando un PR de prueba muestra los tests corriendo.
- Día 2: escribir la regla del recomendador en `docs/ia.md` (puntaje por prenda según misma categoría o complementaria, mismo género, precio parecido, misma marca y disponibilidad); interfaz `EstrategiaRecomendacion` (Strategy) y clase `RecomendacionPorContenido`, probada con productos de ejemplo.
- Día 3: `RecomendacionFactory` (Factory) que elige la estrategia; 5 tests unitarios con resultado esperado. **Hito 1.**
- Día 4: GET `/api/recomendaciones?productoId=&limite=` documentado en Swagger. Devuelve prendas parecidas al producto pedido.
- Día 5: versión por cola: POST `/api/recomendaciones/solicitudes` responde 202 y publica `RecomendacionSolicitada`; un consumidor la procesa y guarda el resultado. Se ve el mensaje entrar y salir de la cola.
- Día 6: segunda estrategia `RecomendacionComplementaria` (arma un conjunto: parte de arriba, parte de abajo y calzado); tabla de validación con 10 casos (producto de entrada, resultado esperado, resultado obtenido). **Hito 2.**
- Días 7-8: probar las recomendaciones con P1 en el frontend y arreglar lo que falle.
- Día 9: completar `docs/ia.md` (qué modelo se usa y por qué, cómo se valida, limitaciones, capturas de evidencia del REST y de la cola).
- Día 10: ensayo de la demo. Solo si todo lo anterior está terminado: probar una API de try-on con 3 fotos del catálogo, como preparación para el Integrador.

### P5 — RabbitMQ y Mercado Pago (revisa PR de P2)
- Día 1: escribir `docs/eventos.md` (para cada evento: quién lo publica, quién lo consume, en qué cola y el JSON de ejemplo). Eventos: ProductoScrapeado, SuscripcionActivada, RecomendacionSolicitada. Listo cuando lo aprueban P2, P3 y P4. Crear cuenta de desarrollador de Mercado Pago y usuarios de prueba (sandbox).
- Día 2: topología en RabbitMQ (un exchange para el sistema, una cola por consumidor y una cola de errores DLQ por cola); reemplazar `PedidoCreado` por un publicador genérico que reutiliza las confirmaciones del broker. Listo cuando un test publica y consume un evento de prueba. **Hito 1.**
- Día 3: dejar en main las clases de los eventos y la configuración para que P3 y P4 publiquen sin repetir código; identificación simple del usuario (el backend lee el header `X-Usuario-Id`).
- Día 4: entidad `Suscripcion` (usuario, estado, id del pago, fecha); POST `/api/suscripciones`: crea el pago del plan premium en el sandbox de Mercado Pago y devuelve la URL de pago. La URL abre la pantalla de pago de prueba.
- Día 5: recibir la confirmación: endpoint de webhook que consulta el estado del pago en Mercado Pago y, si está aprobado, publica `SuscripcionActivada`. Exponer el backend con ngrok para que Mercado Pago llegue. Plan B: al volver a `/pago/exito`, el backend consulta el estado del pago.
- Día 6: consumidor de `SuscripcionActivada` que marca al usuario como premium (patrón Observer). Pagar en el sandbox deja `premium = true`. **Hito 2.**
- Días 7-8: tests del publicador y de los consumidores; probar el flujo de pago con P1 en el frontend.
- Día 9: documentar las colas y el flujo de pago; capturas del panel de RabbitMQ y del pago de prueba.
- Día 10: ensayo de la demo.

## Dependencias: quién espera a quién

| Día | Entrega | De | Para |
|---|---|---|---|
| 1 | prototype mergeado a main | P2 | Todos |
| 1 | GitHub configurado (main protegida, tablero, Actions) | P4 | Todos |
| 1 | docs/modelo.md (formato de producto) | P2 | P1, P3, P4 |
| 1 | docs/eventos.md (eventos y JSON) | P5 | P2, P3, P4 |
| 3 | Snapshot de productos reales | P3 | P2 |
| 3 | Clases de eventos y configuración de RabbitMQ | P5 | P2, P3, P4 |
| 3 | Endpoints de catálogo con Swagger | P2 | P1, P4 |
| 4 | GET /api/recomendaciones | P4 | P1 |
| 4 | ProductoScrapeado publicándose | P3 | P2 |
| 4 | POST /api/suscripciones | P5 | P1 |
| 6 | GET /api/productos/{id}/similares | P3 | P1 |
| 8 | docker-compose completo | P2 | Todos |

## Riesgos y plan B

| Riesgo | Plan B | Dueño |
|---|---|---|
| La tienda bloquea el scraper o cambia el sitio | Usar el snapshot guardado en `scraper/snapshots/` para la demo | P3 |
| El webhook de Mercado Pago no llega a la máquina local | Consultar el estado del pago al volver a `/pago/exito` | P5 |
| Ximilar no responde o se terminan los créditos | Perfil mock con respuestas fijas; Mercado Pago ya cubre la API externa obligatoria | P3 |
| Alguien se atrasa más de 1 día | Se avisa en la daily y se pasa la tarea a quien esté adelantado; el frontend es lo primero que se recorta | Todos |
| La cátedra pide literalmente PedidoCreado | Consultar por mail el día 1 | P2 |

> Pendiente de decidir el día 1: qué persona toma cada rol (P1 a P5).
