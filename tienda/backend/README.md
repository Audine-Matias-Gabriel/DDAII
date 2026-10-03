# Backend — Tienda

Java 17 + Spring Boot 4.1 + Maven + PostgreSQL. Expone la API REST que consume el frontend.

## Estructura

```
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

```bash
docker compose down -v    # resetea el volumen: NECESARIO si cambiás productos.json
docker compose up -d
```

Luego:

```bash
mvn spring-boot:run
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

```bash
mvn test              # no hay tests todavía: src/test/java está vacío
mvn spring-boot:run
```

## Pendiente

- Tests JUnit (compra válida, stock insuficiente, producto inexistente).
- Swagger/OpenAPI: falta la dependencia `springdoc`.
- `util/` (validación, logging, config) del enunciado.
- P1-P2: Factory, Strategy, Facade, `ClienteService`, RabbitMQ, SOAP, API externa,
  IA (`AiService`).