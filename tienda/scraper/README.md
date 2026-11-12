# KNOWFIT - Scraper multi-tienda (Strategy)

Módulo Node.js independiente para obtener catálogos de tiendas y convertirlos al
modelo común `Producto` de KNOWFIT. Está preparado para incorporar una estrategia
por tienda sin cambiar el servicio de catálogo ni el backend.

## Estructura

```text
scraper/
├── src/
│   ├── adapters/
│   │   └── ScraperSportline.js
│   ├── config/
│   │   └── scraper.config.js
│   ├── factories/
│   │   └── scraper.factory.js
│   ├── interfaces/
│   │   └── ScraperTienda.js
│   ├── models/
│   │   ├── Producto.js
│   │   ├── Tienda.js
│   │   └── Usuario.js
│   ├── services/
│   │   └── catalog.service.js
│   ├── utils/
│   │   └── helpers.js
│   └── index.js
├── tests/
├── output/
├── .env.example
└── package.json
```

## Patrón Strategy

- `ScraperTienda` define el contrato: `fetchCatalog()` y `normalizeProduct()`.
- `ScraperSportline` implementa ese contrato usando el endpoint público de catálogo VTEX.
- `scraper.factory.js` selecciona la estrategia según `STORE_ADAPTER`.
- `catalog.service.js` trabaja contra la abstracción: no conoce VTEX ni una tienda concreta.
- `getNormalizedCatalog()` está implementado en la clase base y aplica normalización y deduplicación comunes.

En JavaScript no existe una palabra clave `interface` como en TypeScript; por eso `ScraperTienda` se implementa como clase abstracta que obliga a implementar los métodos al utilizarlos.

## Instalación y ejecución

```bash
npm install
cp .env.example .env
npm test
npm start
```

La ejecución actual utiliza `STORE_ADAPTER=SPORTLINE` y genera `output/sportline.json`.

## Agregar otra tienda

1. Crear una clase en `src/adapters/`, por ejemplo `ScraperLasMargaritas.js`.
2. Extender `ScraperTienda` e implementar `fetchCatalog()` y `normalizeProduct()`.
3. Registrar la clase en `src/factories/scraper.factory.js`.
4. Configurar la identidad, URL y parámetros propios de esa tienda.

No asumir que otra tienda utiliza VTEX: primero verificar su mecanismo real de catálogo. La nueva estrategia debe devolver los mismos campos del modelo `Producto`.

## Modelo y persistencia

Cada producto contiene `id, idExterno, nombre, descripcion, precio, categoria, genero,
marca, tienda, urlOriginal, imagenUrl, disponible, tallesDisponibles`. `id` queda en
`null` para que lo asigne PostgreSQL; `idExterno` identifica el producto en su tienda.
La combinación `tienda + idExterno` debe tratarse como identificador externo único.

El scraper genera JSON; no escribe directamente en PostgreSQL. El backend valida y
persiste los productos. `Usuario` es una entidad interna de KNOWFIT y no se obtiene
del catálogo comercial.

## Reglas de Sportline

- Consulta el catálogo público VTEX con `_from` y `_to`, en páginas de hasta 50.
- Precio: menor precio positivo entre SKUs disponibles.
- Talles: variaciones cuyo nombre contiene Talle, Tamaño o Size, de SKUs disponibles.
- Disponible: al menos un SKU con disponibilidad y cantidad mayor que cero.
- Género: inferencia simple por nombre, descripción y categorías.
- Imagen: primera imagen disponible de los SKUs.

La estructura de VTEX puede variar. Validar la respuesta real del endpoint antes de
considerar completa la extracción y respetar los términos y límites de la tienda.
