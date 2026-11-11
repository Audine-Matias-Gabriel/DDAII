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
frontend (React/JS)  <-->  backend (Spring Boot)  <-->  scraper (Spring Boot)
                              |                      |
                              v                      v
                         Postgres              RabbitMQ  <-->  Ximilar (IA similar)
                         RabbitMQ
                              |
                              v
                         Mercado Pago (API externa)
```

- **backend**: catálogo, SOAP de talles, suscripciones/pago Mercado Pago, recomendaciones REST y por cola, similares Ximilar.
- **scraper**: lee catálogos de tiendas reales, publica `ProductoScrapeado` a RabbitMQ, guarda snapshots.
- **frontend mínimo**: catálogo y detalle públicos, "Ver en tienda", filtros, bloque "Te puede gustar", página Planes con botón "Hacerme premium" que redirige a Mercado Pago.

## Servicios (docker-compose)

| Servicio | Puerto | Descripción |
|---|---|---|
| postgres | 5432 | Base de datos del backend |
| rabbitmq | 5672 / 15672 | Broker de mensajes + panel de administración |
| backend | 8080 | API REST, SOAP, Swagger UI |
| scraper | 8081 | Segundo servicio, lectura de catálogos |
| frontend | 80 / 5173 | Web mínima para la demo |

## Cómo levantar el proyecto

```bash
# 1. Copiar variables de entorno
cp .env.example .env
# editar .env con tus valores (claves de Mercado Pago/Ximilar en sandbox)

# 2. Levantar toda la pila
docker compose up --build

# 3. Verificar
# Swagger UI:       http://localhost:8080/swagger-ui/index.html
# WSDL (SOAP):      http://localhost:8080/ws/talles.wsdl
# RabbitMQ panel:   http://localhost:15672  (guest / guest)
# Frontend demo:    http://localhost:5173
```

## Pruebas

```bash
# Backend (Java/Maven)
cd backend
mvn test

# Frontend (Node)
cd ../frontend
npm test
```

El pipeline de GitHub Actions corre `mvn test` y `npm test` en cada Pull Request.

## Trabajo por integrante (resumen del plan de 10 días)

- **P1 — Frontend y evidencias:** frontend mínimo, borrado de carrito, filtros, detalle con "Ver en tienda", login simple (header `X-Usuario-Id`), bloque "Te puede gustar", página Planes/premium, Dockerfile del frontend, evidencias en `docs/evidencias/`. Revisa PR de P4.
- **P2 — Backend core, catálogo y SOAP:** merge prototype→main (Día 1), modelo, borrado de código de compra, entidades/repositorios/DTOs, endpoints catálogo + paginación + seed, Swagger UI (springdoc), SOAP talles (Spring-WS), consumidor de ProductoScrapeado, tests, Dockerfile backend + docker-compose final, README. Revisa PR de P3.
- **P3 — Scraper y Ximilar:** elegir 2 tiendas argentinas con catálogo público, cuenta Ximilar, proyecto `scraper/`, snapshot tienda 1 (≥30 prendas), interfaz `ScraperTienda` (Strategy), publicar `ProductoScrapeado` a RabbitMQ, tienda 2, ejecución automática 1 vez/día, `XimilarFacade` y endpoint similares, tests del convertidor sin internet, Dockerfile scraper. Revisa PR de P5.
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

## Créditos

Guía del equipo y plan de etapas: `@Salva` (5 nov 2026).
