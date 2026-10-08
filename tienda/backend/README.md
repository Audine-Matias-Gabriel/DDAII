# Backend — Tienda

Java 17 + Spring Boot 4.1 + Maven + PostgreSQL. Expone la API REST que consume el frontend.

Maven se corre con el **wrapper** del repo (`.\mvnw`): no hay que instalar `mvn`. Requiere
un **JDK 17+** con `JAVA_HOME` apuntando a él (el wrapper baja Maven 3.9.16 solo).

## Estructura

```
mvnw / mvnw.cmd              # Maven Wrapper (only-script, sin .jar)
.mvn/wrapper/
└── maven-wrapper.properties # distributionUrl → Maven 3.9.16

src/main/java/com/ddaii/
├── Main.java             # @SpringBootApplication
├── domain/               # Producto, Pedido, DetallePedido, Cliente
│                         # + Categoria, Genero, EstadoProducto, EstadoPedido, TipoCliente
├── repositories/         # ProductoRepository, PedidoRepository, ClienteRepository
├── services/             # ProductoService, PedidoService, PedidoCompraService, InventarioService
├── controllers/          # ProductoController, PedidoController
├── events/               # PedidoCreadoEvent, StockActualizadoEvent
├── messaging/            # CompraLogger (Observer)
└── config/               # ProductoDataLoader (seed)

src/main/resources/
├── application.properties
└── productos.json        # seed de 14 productos
```

> El enunciado pedía `com/tienda/{domain,repository,service,web,util}`. El código real usa
> `com/ddaii/{domain,repositories,services,controllers,events,messaging,config}`.

## Endpoints

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/productos` | Lista el catálogo |
| GET | `/api/productos/{id}` | Detalle, o `404 {"error": "..."}` |
| GET | `/api/productos/tienda/{tiendaId}` | Productos de una tienda |
| PUT | `/api/productos/{id}/stock?cantidad=N` | Descuenta stock |
| POST | `/api/pedidos` | Confirma la compra, `201` con el `Pedido` |
| GET | `/api/pedidos/{clienteId}/pedidos` | Pedidos del cliente |

CORS habilitado para `localhost:5173` y `localhost:4173` en ambos controllers.

`POST /api/pedidos` recibe solo `productoId` y `cantidad` por detalle:

```json
{ "detalles": [ { "productoId": 1, "cantidad": 2 } ] }
```

El **precio lo resuelve el backend** desde la base, así el cliente no puede manipularlo.
Errores: `400` por payload inválido o producto inexistente, `409` por stock insuficiente.

## Levantar

PostgreSQL primero (el `docker-compose.yml` de la raíz lo levanta en el puerto **5433**):

```powershell
docker compose down -v    # resetea el volumen: NECESARIO si cambiás productos.json
docker compose up -d
```

Luego (PowerShell, desde este directorio):

```powershell
.\mvnw spring-boot:run
```

Queda en `http://localhost:8080`.

> `ProductoDataLoader` solo siembra cuando `productoRepository.count() == 0`. Con
> `ddl-auto=update` las columnas nuevas se crean solas, pero el seed no vuelve a correr: si
> tocás `productos.json`, hacé `docker compose down -v` primero.

## Flujo de compra

```
POST /api/pedidos
      ↓
PedidoCompraService.confirmarCompra()   valida, resuelve precios, calcula el total
      ↓
PedidoRepository.save()                 el pedido se persiste antes del evento para tener id
      ↓
publica PedidoCreadoEvent
      ↓
InventarioService.alCrearPedido()       Observer: verifica y descuenta stock
      ↓
publica StockActualizadoEvent
      ↓
CompraLogger                            Observer: loguea ====COMPRA REALIZADA====
```

## Comandos

```powershell
.\mvnw test             # 55 tests (servicios, controllers, dominio) — JUnit 5 + Mockito
.\mvnw spring-boot:run
```

## Tests

`src/test/java/com/ddaii/` — unitarios con Mockito, sin DB:

| Clase | Qué cubre |
|---|---|
| `services/InventarioServiceTest` | Los 3 casos del enunciado: compra válida, stock insuficiente, producto inexistente |
| `services/PedidoCompraServiceTest` | `confirmarCompra`: precios desde la base, validaciones, evento |
| `services/ProductoServiceTest` · `PedidoServiceTest` | Delegación a repositorio y excepciones |
| `controllers/*ControllerTest` | MockMvc standalone: códigos 200/201/400/404/409 |
| `domain/ProductoTest` | Descuento de stock y `estaDisponible` |

## Pendiente

- Swagger/OpenAPI: falta la dependencia `springdoc`.
- `util/` (validación, logging, config) del enunciado.
- P1-P2: Factory, Strategy, Facade, `ClienteService`, RabbitMQ, SOAP, API externa,
  IA (`AiService`).