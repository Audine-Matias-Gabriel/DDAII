# Backend — Agregador de ropa

Java 17 + Spring Boot 4.1 + Maven + PostgreSQL. Expone la API REST, el servicio SOAP y
participa de la mensajería con RabbitMQ.

Maven se corre con el **wrapper** del repo (`.\mvnw`): no hay que instalar `mvn`. Requiere
un **JDK 17+** con `JAVA_HOME` apuntando a él.

## Estructura objetivo

```
mvnw / mvnw.cmd              # Maven Wrapper
.mvn/wrapper/

src/main/java/com/ddaii/
├── Main.java                # @SpringBootApplication
├── domain/                  # Producto, Tienda, Usuario, Suscripcion + enums
├── repositories/            # ProductoRepository, TiendaRepository, UsuarioRepository,
│                            # SuscripcionRepository
├── services/                # ProductoService, RecomendacionService, SuscripcionService,
│                            # EquivalenciaTallesService
├── controllers/             # ProductoController, TiendaController,
│                            # RecomendacionController, SuscripcionController
├── events/                  # ProductoScrapeado, SuscripcionActivada,
│                            # RecomendacionSolicitada
├── messaging/               # publicador genérico y consumidores
├── integrations/            # XimilarFacade, MercadoPagoClient
├── config/                  # RabbitMQConfig, SwaggerConfig, CorsConfig
└── exceptions/              # GlobalExceptionHandler (@RestControllerAdvice)

src/main/resources/
├── application.properties
├── productos.json           # seed inicial
└── equivalencias-talles.xsd
```

> El código anterior de compra (`Pedido`, `DetallePedido`, `PedidoController`,
> `PedidoCompraService`, `PedidoService`, `InventarioService`, `StockMapConverter` y
> `PUT /stock`) se elimina en esta etapa.

## Modelo

### `Producto`

`id`, `idExterno`, `nombre`, `descripcion`, `precio`, `categoria`, `genero`, `marca`,
`tienda`, `urlOriginal`, `imagenUrl`, `disponible`, `tallesDisponibles`.

La combinación `tienda + idExterno` es única.

### `Tienda`

`id`, `nombre`, `urlBase`, `activa`.

### `Usuario`

`id`, `nombre`, `email`, `premium`.

### `Suscripcion`

`id`, `usuario`, `estado`, `idPago`, `fecha`.

## Endpoints REST

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/productos` | Catálogo con filtros y paginación |
| GET | `/api/productos/{id}` | Detalle o `404 {"error": "..."}` |
| GET | `/api/tiendas` | Listado de tiendas |
| GET | `/api/productos/{id}/similares` | Prendas similares de otras tiendas |
| GET | `/api/recomendaciones?productoId=&limite=` | Recomendaciones por contenido |
| POST | `/api/recomendaciones/solicitudes` | `202` y publicación de evento |
| POST | `/api/suscripciones` | Crea el pago premium |
| POST | `/api/suscripciones/webhook` | Confirmación de MercadoPago |
| GET | `/api/suscripciones/{id}/estado` | Estado del pago |

Filtros de catálogo: `categoria`, `tienda`, `genero`, `precioMin`, `precioMax`, `texto`,
`pagina`, `tamano`.

Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

## SOAP

Servicio de equivalencias de talles:

- WSDL: `http://localhost:8080/ws/equivalencias-talles.wsdl`
- Endpoint: `http://localhost:8080/ws/equivalencias-talles`
- Tabla AR/US/EU para remeras, pantalones y calzado.

## Eventos

| Evento | Publica | Consume | Efecto |
|---|---|---|---|
| `ProductoScrapeado` | Scraper | Backend | Upsert por `tienda + idExterno` |
| `SuscripcionActivada` | Backend | Backend | Marca `usuario.premium = true` |
| `RecomendacionSolicitada` | Backend | Componente de IA | Calcula y guarda recomendaciones |

Topología:

- Exchange `tienda.exchange`.
- `producto.scrapeado.queue` + `producto.scrapeado.dlq`.
- `suscripcion.activada.queue` + `suscripcion.activada.dlq`.
- `recomendacion.solicitada.queue` + `recomendacion.solicitada.dlq`.

## Configuración

Credenciales por variables de entorno. En local se puede usar `.env` o
`application-local.properties` (ignorados por git).

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/tienda
spring.datasource.username=tienda
spring.datasource.password=tienda_dev_123

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=tienda
spring.rabbitmq.password=tienda_dev_123

mercadopago.access-token=
mercadopago.public-key=
ximilar.api-token=
ximilar.collection-id=
```

## Levantar

```powershell
docker compose up -d
cd tienda\backend
.\mvnw spring-boot:run
```

Queda en `http://localhost:8080`.

## Tests

```powershell
.\mvnw test
```

Cobertura esperada:

- `ProductoServiceTest`: catálogo, filtros y detalle.
- `ProductoScrapeadoConsumerTest`: alta y upsert por tienda.
- `ProductoControllerTest`: 200/404 y similares.
- `EquivalenciaTallesServiceTest`: SOAP AR/US/EU.
- `RecomendacionPorContenidoTest`, `RecomendacionComplementariaTest` y
  `RecomendadorFactoryTest`.
- `SuscripcionServiceTest` y `SuscripcionActivadaConsumerTest`.
- `EventoPublisherTest`: publicación y DLQ.

## Pendiente

- Implementar el modelo nuevo y eliminar el código de compra.
- Implementar catálogo con filtros y paginación.
- Implementar SOAP con Spring-WS.
- Implementar Swagger con springdoc.
- Implementar RabbitMQ, eventos y DLQ.
- Implementar recomendaciones, Ximilar y MercadoPago.
- Completar Dockerfile y variables de entorno.
