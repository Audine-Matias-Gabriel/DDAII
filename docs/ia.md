# Componente de IA — Recomendaciones por contenido

Acordado el Día 2 por P4. Modelo simple, sin entrenamiento. Expone por REST y por cola.

## Modelo (regla del recomendador)

Cada prenda recibe un puntaje frente a un producto de entrada según:

1. Misma categoría o categoría complementaria (ej. remera ↔ pantalón, parte de arriba ↔ parte de abajo).
2. Misma género.
3. Precio parecido (diferencia dentro de un umbral %, ej. ±30%).
4. Misma marca.
5. Disponibilidad (solo prendas `disponible = true`).

## Clases (backend)

- `EstrategiaRecomendacion` — interfaz (patrón Strategy).
- `RecomendacionPorContenido` — implementa la estrategia por contenido (probada con productos de ejemplo, Día 2).
- `RecomendacionComplementaria` — segunda estrategia: arma un conjunto (parte de arriba, parte de abajo y calzado) (Día 6).
- `RecomendacionFactory` — Factory que elige la estrategia (Día 3).
- `RecomendacionController` — expone `GET /api/recomendaciones?productoId=&limite=` (Día 4) y `POST /api/recomendaciones/solicitudes` (Día 5, versión por cola).

## Endpoints

- `GET /api/recomendaciones?productoId={id}&limite={n}` → devuelve prendas parecidas al producto pedido. Documentado en Swagger.
- `POST /api/recomendaciones/solicitudes` → responde 202 y publica `RecomendacionSolicitada`; un consumidor la procesa y guarda el resultado.

## Validación

Tabla de validación con 10 casos: producto de entrada, resultado esperado y resultado obtenido (Día 6).

## Limitaciones

- Modelo simple basado en reglas de contenido (no hay entrenamiento de ML).
- No usa tagging de Ximilar (cuesta 15 créditos por foto; el plan gratuito da 1.000/mes).
- En estos 10 días solo se usa la colección y la búsqueda de similares de Ximilar (0,25 créditos por imagen subida).

## Evidencias

Capturas de evidencia del REST y de la cola en `docs/evidencias/` (Día 9).
