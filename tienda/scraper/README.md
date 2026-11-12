# Scraper Sportline

Módulo Node.js independiente que consulta el catálogo público VTEX de
https://www.sportline.com.ar y genera `output/catalogo.json`.

## Instalación y ejecución

```bash
npm install
npm start
```

Opcionalmente copiar `.env.example` a `.env` y ajustar la configuración.

## Variables

- `PAGE_SIZE`: tamaño de página (el módulo lo limita a 50).
- `MAX_PRODUCTS`: máximo total de productos.
- `REQUEST_DELAY_MS`: pausa entre páginas.
- `OUTPUT_FILE`: ruta del JSON generado.

## Modelo

Cada producto contiene `id, idExterno, nombre, descripcion, precio, categoria,
genero, marca, tienda, urlOriginal, imagenUrl, disponible, tallesDisponibles`.

`id` queda null para que lo asigne PostgreSQL. `idExterno` conserva `productId`
de VTEX. `tienda` contiene el ID de Tienda. El JSON contiene además el objeto
Tienda y la lista de productos.

Usuario es una entidad propia de la app y no se extrae desde Sportline.

## Reglas de normalización

- Precio: menor precio positivo encontrado entre los SKUs disponibles.
- Talles: valores de variaciones cuyo nombre contiene Talle, Tamaño o Size,
  asociados a SKUs disponibles.
- Disponible: verdadero si al menos un SKU tiene disponibilidad y cantidad mayor
  que cero.
- Género: inferencia simple por nombre, descripción y categorías; si no hay
  evidencia suficiente se devuelve `No especificado`.
- Imagen: primera imagen disponible de los SKUs.

La estructura del payload puede variar según la configuración VTEX de la tienda.
Si algún campo llega vacío, ajustar `normalizeProduct()` con una respuesta real
del endpoint. Este módulo solo lee datos públicos; no inicia sesión ni realiza
compras. Respetar condiciones de uso y límites de la tienda.

Para Ximilar, se puede mapear `idExterno` a `_id` e `imagenUrl` a `_url`, sin
delegar a Ximilar los datos comerciales que deben permanecer en PostgreSQL.
