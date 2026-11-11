# Frontend — Agregador de ropa

React 19 + TypeScript + Vite 8 + react-router 7. Sin librería de estado ni de estilos.

## Estructura objetivo

```
src/
├── App.tsx              # tabla de rutas
├── main.tsx             # StrictMode > BrowserRouter > AuthProvider
├── components/          # componentes reutilizables
│                        # ProductoCard, FiltrosCatalogo, BotonVerTienda,
│                        # BloqueRecomendaciones, BloqueSimilares, Header, etc.
├── pages/               # NotFoundPage, auth/LoginPage, productos/{ShopPage,ProductoDetallePage},
│                        # planes/PlanesPage, pago/{PagoExitoPage,PagoErrorPage}
├── context/             # AuthContext (sesión simple)
├── hooks/               # useAuth, useProductos, useProducto, useRecomendaciones, useSimilares
├── services/            # api.ts, productoService.ts, recomendacionService.ts,
│                        # suscripcionService.ts
├── types/               # Producto.ts, Tienda.ts, Recomendacion.ts, Suscripcion.ts
├── lib/                 # formatters.ts
└── styles/              # global.css, variables.css
```

## Rutas

| Ruta | Pantalla | Protegida |
|---|---|---|
| `/login` | `LoginPage` | No |
| `/` | `ShopPage` (catálogo) | No |
| `/productos/:id` | `ProductoDetallePage` | No |
| `/planes` | `PlanesPage` | Sí |
| `/pago/exito` | `PagoExitoPage` | Sí |
| `/pago/error` | `PagoErrorPage` | Sí |
| `*` | `NotFoundPage` | No |

El catálogo y el detalle son **públicos**. El login simple guarda `usuarioId` y lo envía en
el header `X-Usuario-Id`.

## Levantar

```bash
npm install
npm run dev      # http://localhost:5173
```

El frontend llama a `http://localhost:8080` por defecto. Para apuntar a otro lado:

```bash
VITE_API_URL=http://localhost:8080 npm run dev
```

## Otros comandos

```bash
npm run build       # tsc -b && vite build
npm run lint        # oxlint
npm run preview     # sirve el build de dist/
npm run test        # vitest run
npm run test:watch  # vitest en watch
```

## Consumo de la API

Todo pasa por `src/services/`:

- `api.ts` — wrapper de `fetch` con `apiGet`/`apiPost` y un `ApiError { status, mensaje }`.
- `productoService.ts` — catálogo con filtros, detalle y similares.
- `recomendacionService.ts` — recomendaciones REST y solicitud asincrónica.
- `suscripcionService.ts` — creación de suscripción y consulta de estado.

El tipo `Producto` usa `disponible: boolean` y `tallesDisponibles: string[]`. No hay
`stock` numérico ni variantes por cantidad.

## Funcionalidad

### Catálogo

- Filtros por categoría, tienda, género, precio mínimo/máximo y texto.
- Paginación.
- Tarjeta con imagen, nombre, precio, tienda y estado “Disponible / Sin stock”.

### Detalle

- Nombre, descripción, precio, marca, tienda y categoría.
- Estado de disponibilidad y talles disponibles.
- Botón **“Ver tienda”** que abre `urlOriginal` en otra pestaña.
- Bloque **“Te puede gustar”** con recomendaciones.
- Bloque **“Similares en otras tiendas”**.

### Premium

- Página `/planes` con botón **“Hacerme premium”**.
- Llama a `POST /api/suscripciones` y redirige a la URL de MercadoPago.
- Páginas `/pago/exito` y `/pago/error` para el resultado.

## Tests

Vitest + Testing Library (jsdom). Los tests van **colocalizados** junto al código que
prueban (`X.test.tsx` dentro de `src/`); el helper compartido está en `src/test/`.

Cubren servicios, hooks, componentes y páginas:

- Filtros del catálogo.
- Disponibilidad y talles.
- “Ver tienda”.
- Recomendaciones y similares.
- Planes y páginas de pago.
- Login y header `X-Usuario-Id`.
- Rutas públicas y 404.

La red se simula con `vi.mock` de módulos y `vi.stubGlobal('fetch', ...)`.

## A propósito no incluye

- Carrito ni checkout: se eliminaron `CarritoContext`, `CarritoMenu`, `useCarrito`,
  `crearPedido` y sus tests.
- Login real con Firebase.
- Try-on u outfits.
- Historial de precios.
- Búsqueda por imagen subida.

## Convenciones

- CSS Modules + tokens de `styles/variables.css`. Sin librería de estilos.
- Context + hooks en vez de librería de estado.
- Imports relativos dentro de una carpeta, `@/` para cruzar carpetas.
- Cada componente en su propia carpeta con barrel `index.ts`.
