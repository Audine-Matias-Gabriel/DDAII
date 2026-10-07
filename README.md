# Tienda de Ropa Multi-tienda

Proyecto universitario para **Desarrollo de Aplicaciones II — UADE**.
App web tipo Mercado Libre solo para ropa, con artículos de múltiples tiendas.

## Estructura

```
tienda/
  backend/    # Java 17 + Spring Boot + Maven + PostgreSQL
  frontend/   # React 19 + TypeScript + Vite
docker-compose.yml   # PostgreSQL 16 en el puerto 5433
docs/         # contexto e instrucciones locales (ignorado por git)
```

## Stack

- Backend: Java 17, Spring Boot 4.1, Maven, Spring Data JPA, PostgreSQL
- Frontend: TypeScript, React 19 + Vite 8, CSS Modules
- Deploy: Docker + docker-compose (hoy solo PostgreSQL)
- P1-P2 (pendiente): RabbitMQ, SOAP puntual, API externa, componente IA, Swagger

## Levantar

Requisitos: Docker Desktop y un **JDK 17+** con `JAVA_HOME` apuntando a él. Maven **no**
hace falta instalarlo: el wrapper del repo (`.\mvnw`) lo descarga solo en el primer uso.

```powershell
docker compose up -d                    # PostgreSQL en localhost:5433

cd tienda\backend
.\mvnw spring-boot:run                  # API en localhost:8080

cd ..\frontend
npm install
npm run dev                             # UI en localhost:5173
```

## Métodos de prueba

Los comandos van en **PowerShell desde Windows**: `docker` no está en el PATH de WSL y
`java` de WSL no existe. De WSL solo funcionan `npm run lint` y el typecheck.

### 1. Base de datos (lo único en Docker)

```powershell
docker compose down -v      # IMPORTANTE: resetea el volumen
docker compose up -d        # PostgreSQL en localhost:5433
docker compose ps           # tienda-postgres Up, 0.0.0.0:5433->5432/tcp
docker logs tienda-postgres # esperar "database system is ready to accept connections"
```

El `down -v` es **obligatorio**: el seed corre solo si la tabla de productos está vacía, así que
con el volumen viejo quedan los 4 productos de una versión anterior y el JSON no se carga.

### 2. Backend

```powershell
cd tienda\backend
.\mvnw spring-boot:run
```

Dos señales de arranque OK:

- `Tomcat started on port 8080`
- `[INFO] Productos cargados desde productos.json: 14`

Si aparece `La base ya contiene productos`, es que faltó el `down -v` del paso 1.

### 3. Checks de la API

En otra ventana de PowerShell, con el backend corriendo:

```powershell
# 1. Catálogo: 14 productos, con talles, estado, género y creadoEn
curl.exe -s localhost:8080/api/productos

# 2. Detalle del producto 1: la campera, con imagenUrl en null
curl.exe -s localhost:8080/api/productos/1

# 3. Producto inexistente: 404 con { "error": ... }
curl.exe -s -i localhost:8080/api/productos/999

# 4. Compra válida: 201 y total 149800 (74900 x 2), sin mandar precio
curl.exe -s -i -X POST localhost:8080/api/pedidos `
  -H "Content-Type: application/json" `
  -d "{\"detalles\":[{\"productoId\":5,\"cantidad\":2}]}"

# 5. El stock del producto 5 pasó de 9 a 7
curl.exe -s localhost:8080/api/productos/5

# 6. Compra sin stock: 409 con { "error": "Stock insuficiente..." }
curl.exe -s -i -X POST localhost:8080/api/pedidos `
  -H "Content-Type: application/json" `
  -d "{\"detalles\":[{\"productoId\":5,\"cantidad\":999}]}"
```

Además, la consola del backend debe imprimir el bloque `COMPRA REALIZADA` con el stock
anterior y el actual.

`GET /api/pedidos/1/pedidos` devuelve 404: no hay clientes sembrados en la base. Es lo
esperado, no es un bug.

### 4. Flujo completo en el navegador

```powershell
cd tienda\frontend
npm install
npm run dev    # http://localhost:5173
```

1. Entrar en `http://localhost:5173` y hacer login con cualquier correo (es mock).
2. El catálogo tiene que mostrar los 14 productos que bajan de la API.
3. Entrar al detalle del producto 1 y agregarlo al carrito.
4. En el desplegable del header, subir la cantidad con `+`.
5. Confirmar la compra: aparece `Pedido #N confirmado por $X` y el carrito se vacía.
6. Volver al catálogo: el producto figura con el stock ya descontado.
7. Con el backend apagado, el catálogo tiene que mostrar el error de conexión y el botón
   "Reintentar".

### 5. Chequeos rápidos

```powershell
cd tienda\frontend
npm run lint      # oxlint: 0 errores, 2 warnings conocidos de only-export-components
npm run test      # vitest: 111 tests en 24 archivos, todos verdes
npm run build     # tsc + vite build
cd ..\backend
.\mvnw test       # 55 tests JUnit, BUILD SUCCESS
```

### 6. Cerrar

```powershell
docker compose down       # conserva los datos
docker compose down -v    # reinicia la base para el próximo ensayo
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

Frontend y backend **integrados**: el catálogo y el detalle se piden a la API, y el carrito
hace `POST /api/pedidos` para confirmar la compra, que descuenta stock vía evento de dominio.

## API

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Catálogo |
| GET | `/api/productos/{id}` | Detalle |
| POST | `/api/pedidos` | Confirmar compra (manda solo `productoId` y `cantidad`) |
| PUT | `/api/productos/{id}/stock?cantidad=N` | Descontar stock |
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
