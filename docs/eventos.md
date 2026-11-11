# Eventos y colas — Web de Ropa

Acordado el Día 1 por P5 y aprobado por P2, P3 y P4.

## Topología de RabbitMQ

- Un **exchange** para el sistema (`webropa.events`, tipo topic).
- Una **cola por consumidor**.
- Una **cola de errores (DLQ)** por cola.

## Eventos

### 1. ProductoScrapeado

- **Publica:** P3 (scraper), cada vez que lee una prenda del catálogo.
- **Consume:** P2 (backend), crea o actualiza el producto por `tienda + idExterno`.
- **Cola:** `cola.producto.scrapeado`
- **JSON de ejemplo:**
```json
{
  "tipo": "ProductoScrapeado",
  "tienda": "tienda1",
  "idExterno": "PROD-12345",
  "nombre": "Remera algodón estampada",
  "descripcion": "Remera de algodón con estampa",
  "precio": 15990.00,
  "categoria": "REMERA",
  "genero": "Mujer",
  "marca": "Tienda1",
  "urlOriginal": "https://tienda1.com.ar/producto/12345",
  "imagenUrl": "https://tienda1.com.ar/imagenes/12345.jpg",
  "disponible": true,
  "tallesDisponibles": ["S", "M", "L", "XL"]
}
```

### 2. SuscripcionActivada

- **Publica:** P5 (backend), cuando el webhook de Mercado Pago confirma un pago aprobado.
- **Consume:** P5 (backend), marca al usuario como premium (patrón Observer).
- **Cola:** `cola.suscripcion.activada`
- **JSON de ejemplo:**
```json
{
  "tipo": "SuscripcionActivada",
  "usuarioId": 42,
  "estado": "ACTIVA",
  "idPago": "pay-1234567890",
  "fecha": "2026-11-05T10:00:00"
}
```

### 3. RecomendacionSolicitada

- **Publica:** P4 (backend), al llamar `POST /api/recomendaciones/solicitudes` (responde 202).
- **Consume:** P4 (backend), procesa la solicitud y guarda el resultado.
- **Cola:** `cola.recomendacion.solicitada`
- **JSON de ejemplo:**
```json
{
  "tipo": "RecomendacionSolicitada",
  "productoId": 7,
  "limite": 5,
  "fecha": "2026-11-05T10:05:00"
}
```
