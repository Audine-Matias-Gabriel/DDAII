import { render } from '@testing-library/react'
import type { ReactElement, ReactNode } from 'react'
import { MemoryRouter } from 'react-router-dom'
import type { AuthContextValue } from '@/context/AuthContext'
import { AuthContext, AuthProvider } from '@/context/AuthContext'
import type { CarritoContextValue } from '@/context/CarritoContext'
import { CarritoContext, CarritoProvider } from '@/context/CarritoContext'
import type { Genero, Producto } from '@/types/Producto'

export type OpcionesRender = {
  initialEntries?: string[]
  auth?: AuthContextValue | undefined
  carrito?: CarritoContextValue | undefined
}

/** Renderiza `ui` dentro de los providers reales (o unos inyectados) y un router en memoria. */
export function renderConProviders(
  ui: ReactElement,
  { initialEntries = ['/'], auth, carrito }: OpcionesRender = {},
) {
  function Wrapper({ children }: { children: ReactNode }) {
    const conAuth = auth ? (
      <AuthContext value={auth}>{children}</AuthContext>
    ) : (
      <AuthProvider>{children}</AuthProvider>
    )

    const conCarrito = carrito ? (
      <CarritoContext value={carrito}>{conAuth}</CarritoContext>
    ) : (
      <CarritoProvider>{conAuth}</CarritoProvider>
    )

    return <MemoryRouter initialEntries={initialEntries}>{conCarrito}</MemoryRouter>
  }

  return render(ui, { wrapper: Wrapper })
}

/** Producto de prueba con valores por defecto, sobreescribibles. */
export function producto(overrides: Partial<Producto> = {}): Producto {
  return {
    id: '1',
    nombre: 'Remera Nike',
    descripcion: 'Una remera de prueba',
    precio: 25000,
    stock: stockPorGenero({ UNISEX: { S: 5, M: 5 } }),
    categoria: 'REMPERA',
    genero: 'UNISEX',
    estado: 'NUEVO',
    imagenUrl: undefined,
    tiendaId: '1',
    creadoEn: '2024-03-15T00:00:00',
    ...overrides,
  }
}

/** Arma un stock por variante permitiendo declarar solo algunos géneros. */
export function stockPorGenero(
  mapa: Partial<Record<Genero, Record<string, number>>>,
): Producto['stock'] {
  return mapa as Producto['stock']
}
