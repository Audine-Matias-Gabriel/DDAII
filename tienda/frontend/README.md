# Frontend — Tienda

React 19 + TypeScript + Vite 8 + react-router 7. Sin librería de estado ni de estilos.

## Estructura

```
src/
├── App.tsx              # tabla de rutas
├── main.tsx             # StrictMode > BrowserRouter > AuthProvider > CarritoProvider
├── components/          # 10 componentes reutilizables
│                        # cada uno en su carpeta: X.tsx + X.module.css + index.ts
├── pages/               # NotFoundPage, auth/LoginPage, productos/{ShopPage,ProductoDetallePage}
├── context/             # AuthContext (sesión mock), CarritoContext (carrito + checkout)
├── hooks/               # useAuth, useCarrito, useProductos, useProducto
├── services/            # api.ts (fetch + ApiError), productoService.ts
├── data/                # tiendas.json + index.ts
├── types/               # Producto.ts, Pedido.ts, Tienda.ts
├── lib/                 # formatters.ts (moneda, fecha, iniciales)
└── styles/              # global.css, variables.css (tokens)
```

## Rutas

| Ruta | Pantalla | Protegida |
|---|---|---|
| `/login` | `LoginPage` | no |
| `/` | `ShopPage` (catálogo) | sí |
| `/productos/:id` | `ProductoDetallePage` | sí |
| `*` | `NotFoundPage` | no |

## Levantar

```bash
npm install
npm run dev      # http://localhost:5173
```

El frontend llama a `http://localhost:8080` por defecto. Para apuntar a otro lado:

```bash
VITE_API_URL=http://localhost:8080 npm run dev
```

Los controllers del backend tienen CORS para `localhost:5173` y `localhost:4173`, así que
`npm run preview` también funciona.

## Otros comandos

```bash
npm run build     # tsc -b && vite build
npm run lint      # oxlint
npm run preview   # sirve el build de dist/
```

## Consumo de la API

Todo pasa por `src/services/`:

- `api.ts` — wrapper de `fetch` con `apiGet`/`apiPost` y un `ApiError { status, mensaje }`
  que lee el `{ "error": ... }` que devuelve el backend.
- `productoService.ts` — `listarProductos()`, `obtenerProducto(id)`, `crearPedido(detalles)`.
  La respuesta cruda del backend se normaliza a los tipos de la UI: `String(id)`,
  `String(tiendaId)`, `imagenUrl ?? undefined` (el backend manda `null`, no `undefined`).

`useProductos` y `useProducto` exponen `{ datos, cargando, error, recargar }`. El detalle
recibe el error tipado como `ApiError` para poder distinguir el 404 de otros fallos.

## Carrito

`CarritoContext` guarda ítems `{ producto, cantidad }` y expone `agregar`, `cambiarCantidad`,
`quitar`, `vaciar`, `cantidadTotal`, `total` y `confirmar()`. `confirmar()` hace el
`POST /api/pedidos` mandando solo `productoId` y `cantidad`, y devuelve `{ ok, mensaje }`;
el carrito **solo se vacía si la compra se confirmó**.

## Convenciones

- CSS Modules + tokens de `styles/variables.css`. Sin librería de estilos.
- Context + hooks en vez de librería de estado.
- Imports relativos dentro de una carpeta, `@/` para cruzar carpetas.
- Cada componente en su propia carpeta con barrel `index.ts`.

## A propósito no incluye

- Login real: acepta cualquier correo y deriva el nombre del prefijo.
- Alta de producto: no hay `POST /api/productos`, así que se eliminó la pantalla.
- "Mis pedidos".
- Refetch automático del stock sin navegar: el catálogo se vuelve a pedir en cada mount.
- Tests: no hay runner configurado.