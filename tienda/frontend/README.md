# Frontend — Tienda

TypeScript + React + Vite. Vacío por ahora.

## Estructura prevista

```
src/{components,pages,services,types}
```

- `components`: ProductCard, Cart, ClientForm (reutilizables)
- `services`: cliente REST contra `backend` (`/api/pedidos`, `/api/productos`)
- `pages`: catálogo, detalle, checkout

## Comandos

```bash
npm create vite@latest . -- --template react-ts
npm install
npm run dev
npm run build
```

El backend expone la API; este paquete solo la consume.
