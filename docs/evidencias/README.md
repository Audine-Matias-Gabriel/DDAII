# Evidencias — TP Segunda Parte

Capturas y salidas tomadas el 18/11/2026 con `docker compose up` en local (rama `try-nuevo`
más `p1/filtros-tienda-precio`). Una fila por requisito de la consigna.

| # | Requisito | Evidencia | Archivos |
|---|---|---|---|
| 1 | Servicio SOAP con WSDL | WSDL publicado en `/ws/equivalencias-talles.wsdl` y request/respuesta de prueba: calzado AR 38 → US 6, EU 38 | [01-soap-wsdl.jpg](01-soap-wsdl.jpg), [soap-equivalencias-talles.wsdl](soap-equivalencias-talles.wsdl), [soap-request.xml](soap-request.xml), [soap-response.xml](soap-response.xml) |
| 2 | 2+ servicios REST con OpenAPI | Swagger UI con catálogo, tiendas, suscripciones, recomendaciones, similares y usuarios | [02-swagger-ui.jpg](02-swagger-ui.jpg) |
| 3 | Mensajería y colas | Exchange `tienda.exchange`, una cola por consumidor y una DLQ por cola. Todas las DLQ en 0. **Cola de errores probada:** un mensaje que el consumidor no puede procesar se reintenta 3 veces y termina en `producto.scrapeado.dlq` con su motivo (`x-death`). Contrato en [docs/eventos.md](../eventos.md) | [03-rabbitmq-colas.jpg](03-rabbitmq-colas.jpg), [rabbitmq-colas.json](rabbitmq-colas.json), [rabbitmq-dlq.txt](rabbitmq-dlq.txt) |
| 4 | Productores y consumidores | **ProductoScrapeado**: el scraper publica (273 + 262 confirmados) y el backend consume (la cola se vacía, gráfico de publish/deliver/ack). **RecomendacionSolicitada**: `POST /api/recomendaciones/solicitudes` → 202 y el consumidor guarda el resultado. **SuscripcionActivada**: ver fila 6 | [04-rabbitmq-producto-scrapeado.jpg](04-rabbitmq-producto-scrapeado.jpg), [logs-scraper-productor.txt](logs-scraper-productor.txt), [cola-recomendaciones-consumidas.txt](cola-recomendaciones-consumidas.txt) |
| 5 | Arquitectura SOA / 2 servicios | backend (Spring Boot) y scraper (Node.js) comunicados solo por RabbitMQ; 537 productos reales de 2 tiendas | [docker-compose-ps.txt](docker-compose-ps.txt), [catalogo-por-tienda.txt](catalogo-por-tienda.txt) |
| 6 | API externa real (Mercado Pago) | Suscripciones con pago aprobado en el sandbox (`id_pago` de Mercado Pago) y usuario premium. Webhook probado contra la API real del sandbox (idempotente). Planes muestra el precio que se cobra ($5.000). Flujo completo en [docs/pagos.md](../pagos.md) | [mercadopago-suscripciones.txt](mercadopago-suscripciones.txt), [mercadopago-webhook.txt](mercadopago-webhook.txt), [08-front-planes.jpg](08-front-planes.jpg) |
| 7 | IA por REST y por cola | Recomendaciones por contenido y complementarias: `GET /api/recomendaciones` (sincrónico) y `POST /api/recomendaciones/solicitudes` (202, por cola) con el resultado del consumidor. **Validación con 10 casos reales: 10/10** (la primera iteración dio 8/10 y se corrigió; ver [docs/ia.md](../ia.md)) | [ia-validacion.json](ia-validacion.json), [ia-cola.txt](ia-cola.txt), [ia-recomendaciones.txt](ia-recomendaciones.txt), [07-front-recomendaciones-y-similares.jpg](07-front-recomendaciones-y-similares.jpg) |
| 8 | Errores y configuración externa | Error manejado por `@RestControllerAdvice`: 404 con `{"error": ...}` | [error-ejemplo.txt](error-ejemplo.txt) |
| 9 | Frontend para la demo | Catálogo con filtros (Remera + Grid), detalle con "Ver en tienda oficial", "Te puede gustar", "Prendas similares en otras tiendas" y Planes | [05-front-catalogo-filtrado.jpg](05-front-catalogo-filtrado.jpg), [06-front-detalle-ver-en-tienda.jpg](06-front-detalle-ver-en-tienda.jpg), [07-front-recomendaciones-y-similares.jpg](07-front-recomendaciones-y-similares.jpg), [08-front-planes.jpg](08-front-planes.jpg) |
| 10 | Segunda API externa (Ximilar) | Catálogo subido a la colección de Product Similarity (537 imágenes, ~134 créditos) y similares reales por `visualKNN`: para unas zapatillas negras con suela caramelo de Sportline devuelve zapatillas parecidas de Grid (`origen: "ximilar"`) | [09-front-similares-ximilar.jpg](09-front-similares-ximilar.jpg), [ximilar-respuesta-real.json](ximilar-respuesta-real.json), [ximilar-similares.txt](ximilar-similares.txt), [ximilar-indexado.txt](ximilar-indexado.txt) |

## Falta (no se puede generar automáticamente)

- **Captura del checkout de Mercado Pago (sandbox)** con la cuenta compradora de prueba, y la de
  Planes diciendo "Ya sos premium" después de pagar. Las tiene que sacar quien tiene las cuentas de prueba.
- **Video de 2 a 3 minutos** del flujo completo (guion abajo).

## Guion del video (2 a 3 minutos)

1. **Arquitectura (15 s):** `docker compose ps`: postgres, rabbitmq, backend, scraper y frontend.
2. **Scraper → cola → backend (30 s):** `docker compose restart scraper` y `docker compose logs -f scraper`
   ("Eventos confirmados: N" por tienda); en el panel de RabbitMQ, `producto.scrapeado.queue`
   recibe y se vacía, y la DLQ queda en 0.
3. **Catálogo (30 s):** `http://localhost:5173`, filtrar por categoría, tienda y precio; abrir un detalle
   y "Ver en tienda oficial" (abre la tienda real).
4. **IA (20 s):** en el detalle, "Te puede gustar" (REST). En Swagger,
   `POST /api/recomendaciones/solicitudes` → 202 (por cola).
5. **SOAP (20 s):** abrir el WSDL y mandar `soap-request.xml` (Postman o curl); mostrar la respuesta.
6. **Mercado Pago (40 s):** registrarse, ir a Planes ($5.000), "Hacerme premium", pagar en el sandbox
   con la tarjeta de prueba, volver a Planes: "Ya sos premium" y el botón bloqueado.
7. **Swagger (10 s):** recorrer los endpoints.

### Comandos útiles

```powershell
curl.exe -X POST http://localhost:8080/ws -H "Content-Type: text/xml" --data-binary "@docs/evidencias/soap-request.xml"
```

```powershell
curl.exe -X POST http://localhost:8080/api/recomendaciones/solicitudes -H "Content-Type: application/json" -d '{\"productoId\":1,\"limite\":3}'
```
