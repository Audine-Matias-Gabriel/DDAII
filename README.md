# Tienda de Ropa Multi-tienda

Proyecto universitario para **Desarrollo de Aplicaciones II — UADE**.
App web tipo Mercado Libre solo para ropa, con artículos de múltiples tiendas.

## Estructura

```
tienda/
  backend/    # Java 17 + Spring Boot + Maven + PostgreSQL
  frontend/   # React 19 + TypeScript + Vite
docker-compose.yml   # PostgreSQL 16 en el puerto 5433 + RabbitMQ 4 en 5672/15672
docs/         # contexto e instrucciones locales (ignorado por git)
```

## Stack

- Backend: Java 17, Spring Boot 4.1, Maven, Spring Data JPA, PostgreSQL, Spring AMQP
- Frontend: TypeScript, React 19 + Vite 8, CSS Modules
- Deploy: Docker + docker-compose (PostgreSQL y RabbitMQ)
- P1-P2 (pendiente): SOAP puntual, API externa, componente IA, Swagger

## Levantar

Requisitos: Docker Desktop y un **JDK 17+** con `JAVA_HOME` apuntando a él. Maven **no**
hace falta instalarlo: el wrapper del repo (`.\mvnw`) lo descarga solo en el primer uso.

```powershell
docker compose up -d                    # PostgreSQL en 5433 y RabbitMQ en 5672/15672

cd tienda\backend
.\mvnw spring-boot:run                  # API en localhost:8080

cd ..\frontend
npm install
npm run dev                             # UI en localhost:5173
```

## Métodos de prueba

Los comandos van en **PowerShell desde Windows**: `docker` no está en el PATH de WSL y
`java` de WSL no existe. De WSL solo funcionan `npm run lint` y el typecheck.

### 1. Base de datos (PostgreSQL en Docker)

```powershell
docker compose down -v      # IMPORTANTE: resetea el volumen
docker compose up -d        # PostgreSQL en localhost:5433
docker compose ps           # tienda-postgres Up, 0.0.0.0:5433->5432/tcp
docker logs tienda-postgres # esperar "database system is ready to accept connections"
```

El `down -v` es **obligatorio**: el seed corre solo si la tabla de productos está vacía, así que
con el volumen viejo quedan los 4 productos de una versión anterior y el JSON no se carga.

### 2. RabbitMQ (mensajería)

RabbitMQ se utiliza para enviar eventos cuando se confirma una compra. El backend publica el evento `PedidoCreadoEvent` en la cola `tienda.pedidos.creado`, y un consumidor recibe el mensaje y muestra los datos del pedido en la consola.

La cola `tienda.pedidos.creado.dlq` es la **cola de mensajes muertos**: recibe los mensajes
que el consumidor rechaza, para poder revisarlos sin que se pierdan.

#### 1. Iniciar RabbitMQ

Requisitos: Docker Desktop abierto y funcionando.

`docker-compose.yml` ya define el servicio `rabbitmq` con su interfaz de administración,
así que la forma normal de levantarlo es:

```powershell
docker compose up -d        # RabbitMQ en 5672 (AMQP) y 15672 (administración)
docker compose ps           # tienda-rabbitmq Up, 0.0.0.0:5672->5672/tcp
```

Si el contenedor ya existe y está detenido, iniciarlo con:

```powershell
docker start tienda-rabbitmq
```

> **No usar `docker run` para crear el contenedor a mano**: el servicio de `docker-compose.yml`
> ya lo crea con el mismo nombre (`tienda-rabbitmq`) y con las mismas credenciales, así que
> los dos caminos chocan con el mismo nombre de contenedor.

Para verificar que esté funcionando:

```powershell
docker ps
docker logs tienda-rabbitmq
```

#### 2. Acceder a RabbitMQ

Abrir en el navegador:

- **Interfaz de administración:** http://localhost:15672
- **Usuario:** `tienda`
- **Contraseña:** `tienda_dev_123`
- **Puerto AMQP:** `5672`

El puerto 5672 se utiliza para la comunicación entre el backend y RabbitMQ. El puerto 15672 permite administrar y monitorear las colas desde el navegador.

#### 3. Verificar la configuración del backend

En `backend/src/main/resources/application.properties` deben estar configuradas las credenciales correspondientes al contenedor:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=tienda
spring.rabbitmq.password=tienda_dev_123

# Confirmaciones de publicación: el backend espera el ack del broker
spring.rabbitmq.publisher-confirm-type=correlated
spring.rabbitmq.publisher-returns=true
spring.rabbitmq.template.mandatory=true

# Un mensaje rechazado no se re-encola: va a la cola de mensajes muertos
spring.rabbitmq.listener.simple.default-requeue-rejected=false
```

No es necesario instalar RabbitMQ directamente en Windows: Docker ejecuta el servicio y el backend se conecta a él mediante el puerto 5672.

#### 4. Ejecutar y probar la integración

1. Iniciar PostgreSQL siguiendo los pasos de la sección **Métodos de prueba**.
2. Iniciar RabbitMQ con los comandos anteriores.
3. Ejecutar el backend desde `backend`:

   ```powershell
   .\mvnw spring-boot:run
   ```

4. Realizar una compra válida mediante `POST /api/pedidos` o desde el frontend.
5. Verificar la consola del backend: debe aparecer el mensaje de publicación y el bloque `PEDIDO RECIBIDO DESDE RABBITMQ`, con el ID del pedido y los artículos comprados.
6. En la interfaz de administración, ingresar a **Queues and Streams** y comprobar que existan las colas `tienda.pedidos.creado` y `tienda.pedidos.creado.dlq`.

El consumidor procesa los mensajes automáticamente. Por eso, si funciona correctamente, la cantidad de mensajes pendientes de la cola puede volver rápidamente a cero.

**Si RabbitMQ está caído, la compra igual se guarda.** El backend publica el evento *después* del
commit de la transacción y, si el broker no responde, registra el error y sigue: el `POST
/api/pedidos` devuelve `201` con el pedido creado. Lo que se pierde es la notificación del
evento, no el pedido.

#### 5. Detener RabbitMQ

Para detener el contenedor sin eliminarlo:

```powershell
docker stop tienda-rabbitmq
```

Para volver a iniciarlo:

```powershell
docker start tienda-rabbitmq
```

Para eliminarlo definitivamente (y con él el volumen de datos):

```powershell
docker rm -f tienda-rabbitmq
docker volume rm proyecto-rabbitmq-data
```

Si se elimina el contenedor, `docker compose up -d` lo vuelve a crear. Ojo: el volumen guarda
las colas ya declaradas, y si cambian sus argumentos el broker las rechaza (ver la fila de
`PRECONDITION_FAILED` más abajo).

#### Fallos típicos de RabbitMQ

| Síntoma | Causa posible |
|---|---|
| `Connection refused` o error de conexión | RabbitMQ está detenido o el puerto 5672 no está disponible. |
| `ACCESS_REFUSED` | Las credenciales configuradas en `application.properties` no coinciden con las del contenedor. |
| `Port 5672 is already allocated` | Otro proceso o contenedor ya está utilizando el puerto. |
| `PRECONDITION_FAILED` (406) | La cola ya existe en el volumen con otros argumentos. **Eliminar el contenedor y el volumen**, y volver a levantarlo con `docker compose up -d`. |
| No aparece `tienda.pedidos.creado` | Revisar que el backend haya iniciado correctamente y que `RabbitMQConfig` declare la cola. |
| La compra se guarda, pero no llega el mensaje | RabbitMQ está caído o no confirma la publicación. El backend lo deja asentado en el log: `No se pudo publicar el pedido N en RabbitMQ`. La confirmación de PostgreSQL no garantiza por sí sola la entrega del evento. |
| La cola `tienda.pedidos.creado.dlq` tiene mensajes | El consumidor rechazó algún mensaje (por ejemplo, JSON inválido). Es el comportamiento esperado: el mensaje queda en la cola de mensajes muertos en vez de perderse. |

**Importante:** las credenciales indicadas son únicamente para el entorno local de desarrollo. No deben utilizarse tal cual en un entorno de producción.


### 3. Backend

```powershell
cd tienda\backend
.\mvnw spring-boot:run
```

Dos señales de arranque OK:

- `Tomcat started on port 8080`
- `[INFO] Productos cargados desde productos.json: 14`

Si aparece `La base ya contiene productos`, es que faltó el `down -v` del paso 1.

### 4. Checks de la API

En otra ventana de PowerShell, con el backend corriendo:

```powershell
# 1. Catálogo: 14 productos, con stock por variante (talle × género)
curl.exe -s localhost:8080/api/productos

# 2. Detalle del producto 1: la campera, con imagenUrl en null
curl.exe -s localhost:8080/api/productos/1

# 3. Producto inexistente: 404 con { "error": ... }
curl.exe -s -i localhost:8080/api/productos/999

# 4. Compra válida con variante: 201 y total 149800 (74900 x 2)
curl.exe -s -i -X POST localhost:8080/api/pedidos `
  -H "Content-Type: application/json" `
  -d "{\"detalles\":[{\"productoId\":5,\"cantidad\":2,\"talle\":\"M\",\"genero\":\"HOMBRE\"}]}"

# 5. El stock de la celda HOMBRE/M del producto 5 pasó de 2 a 0
curl.exe -s localhost:8080/api/productos/5

# 6. Compra sin variante: 400 con { "error": ... }
curl.exe -s -i -X POST localhost:8080/api/pedidos `
  -H "Content-Type: application/json" `
  -d "{\"detalles\":[{\"productoId\":5,\"cantidad\":2}]}"

# 7. Compra con cantidad mayor a la celda HOMBRE/M (quedó en 0): 409
curl.exe -s -i -X POST localhost:8080/api/pedidos `
  -H "Content-Type: application/json" `
  -d "{\"detalles\":[{\"productoId\":5,\"cantidad\":999,\"talle\":\"M\",\"genero\":\"HOMBRE\"}]}"
```

> Ojo: correr los checks 4 y 5 altera el stock persistido. Para repetirlos, `docker compose
> down -v && docker compose up -d` y reiniciar el backend.

Además, la consola del backend debe imprimir el bloque `COMPRA REALIZADA` con el stock
anterior y el actual (incluye talle y género).

`GET /api/pedidos/1/pedidos` devuelve 404: no hay clientes sembrados en la base. Es lo
esperado, no es un bug.

### 5. Flujo completo en el navegador

```powershell
cd tienda\frontend
npm install
npm run dev    # http://localhost:5173
```

1. Entrar en `http://localhost:5173` y hacer login con cualquier correo (es mock).
2. El catálogo tiene que mostrar los 14 productos que bajan de la API.
3. Entrar al detalle del producto 1, elegir género y talle, y agregarlo al carrito.
4. En el desplegable del header, subir la cantidad con `+`.
5. Confirmar la compra: aparece `Pedido #N confirmado por $X` y el carrito se vacía.
6. Volver al catálogo: el producto figura con el stock de esa variante ya descontado.
7. Con el backend apagado, el catálogo tiene que mostrar el error de conexión y el botón
   "Reintentar".

### 6. Chequeos rápidos

```powershell
cd tienda\frontend
npm run lint      # oxlint: 0 errores, 2 warnings conocidos de only-export-components
npm run test      # vitest: 128 tests en 26 archivos, todos verdes
npm run build     # tsc + vite build
cd ..\backend
.\mvnw test       # 55 tests JUnit base + casos de variante del §18 + tests de RabbitMQ
```

### 7. Cerrar

```powershell
docker compose down       # detiene PostgreSQL y RabbitMQ, conserva los datos
docker compose down -v    # reinicia la base y las colas para el próximo ensayo
```

### Fallos típicos

| Síntoma | Causa |
|---|---|
| `password authentication failed for user "tienda"` | Volumen viejo: falta `docker compose down -v` |
| `La base ya contiene productos` en el log del backend | El seed no corrió; falta `down -v` |
| `Port 5433 / 8080 / 5173 is already allocated` | Hay otra instancia viva; bajarla antes |
| El frontend dice `No se pudo conectar con el backend` | El backend está caído o no está en el puerto 8080 |
| Error de CORS en la consola del navegador | El front se abrió en un origen distinto de `localhost:5173` |
| `npm run dev` en WSL va lentísimo | El watching sobre NTFS: correrlo en PowerShell |

## Estado

Frontend y backend **integrados**: el catálogo y el detalle se piden a la API, el carrito
pide **género y talle** (stock por variante) y hace `POST /api/pedidos` para confirmar la
compra, que descuenta la celda `stock[genero][talle]` vía evento de dominio.
Cada compra confirmada publica un `PedidoCreadoEvent` en RabbitMQ, que consume el propio
backend (cola `tienda.pedidos.creado`, con DLQ `tienda.pedidos.creado.dlq`).
Pendiente la verificación end-to-end completa (§ Métodos de prueba).

## API

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Catálogo (stock por variante `genero → talle → cantidad`) |
| GET | `/api/productos/{id}` | Detalle |
| POST | `/api/pedidos` | Confirmar compra (manda `productoId`, `cantidad`, `talle`, `genero`) |
| PUT | `/api/productos/{id}/stock?genero=X&talle=Y&cantidad=N` | Descontar una celda de stock |
| GET | `/api/pedidos/{clienteId}/pedidos` | Pedidos del cliente |

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
4. Nombre y apellido: Mendez , Nicolas — Legajo: 1172141 — Email: nicomendez@uade.edu.ar
5. Nombre y apellido: Vignolo, Nahir Yael — Legajo: 1137587 — Email: nvignolo@uade.edu.ar

## Entregas
- TP Inicial: componentes + multicapa + local vs remoto
- P1: app empresarial + patrones + servicios
- P2: SOAP/REST + RabbitMQ + API externa + IA
