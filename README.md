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

```bash
docker compose up -d        # PostgreSQL en localhost:5433
cd tienda/backend && mvn spring-boot:run   # API en localhost:8080

cd tienda/frontend && npm install && npm run dev   # UI en localhost:5173
```

## Estado

Frontend y backend **integrados**: el catálogo y el detalle se piden a la API, y el carrito
hace `POST /api/pedidos` para confirmar la compra, que descuenta stock vía evento de dominio.

Pendiente: verificación end-to-end, tests JUnit y Swagger. Detalle en `docs/`.

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
