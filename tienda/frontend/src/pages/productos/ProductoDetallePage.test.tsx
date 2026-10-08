import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import type { CarritoContextValue, ResultadoCompra } from '@/context/CarritoContext'
import type { UseProducto } from '@/hooks/useProducto'
import { useProducto } from '@/hooks/useProducto'
import { ApiError } from '@/services/api'
import { producto, renderConProviders } from '@/test/test-utils'
import { ProductoDetallePage } from './ProductoDetallePage'

vi.mock('@/hooks/useProducto', () => ({ useProducto: vi.fn() }))

function carritoCon(overrides: Partial<CarritoContextValue> = {}): CarritoContextValue {
  return {
    items: [],
    cantidadTotal: 0,
    total: 0,
    agregar: vi.fn(),
    cambiarCantidad: vi.fn(),
    quitar: vi.fn(),
    vaciar: vi.fn(),
    confirmar: vi.fn().mockResolvedValue({ ok: true, mensaje: 'ok' } as ResultadoCompra),
    ...overrides,
  }
}

function rutas() {
  return (
    <Routes>
      <Route path="/productos/:id" element={<ProductoDetallePage />} />
      <Route path="/" element={<p>catalogo</p>} />
    </Routes>
  )
}

function montar(estado: UseProducto, carrito: CarritoContextValue = carritoCon()) {
  vi.mocked(useProducto).mockReturnValue(estado)
  return renderConProviders(rutas(), {
    initialEntries: ['/productos/5'],
    carrito,
  })
}

describe('<ProductoDetallePage>', () => {
  it('mientras carga muestra el estado de cargando', () => {
    montar({ producto: null, cargando: true, error: null, recargar: vi.fn() })

    expect(screen.getByText('Cargando producto...')).toBeInTheDocument()
  })

  it('ante un 404 muestra el EmptyState de no encontrado y permite volver', async () => {
    const user = userEvent.setup()
    montar({
      producto: null,
      cargando: false,
      error: new ApiError('No existe', 404),
      recargar: vi.fn(),
    })

    expect(screen.getByText('Producto no encontrado')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Volver al catálogo' }))
    expect(await screen.findByText('catalogo')).toBeInTheDocument()
  })

  it('ante otro error ofrece Reintentar que llama recargar', async () => {
    const user = userEvent.setup()
    const recargar = vi.fn()
    montar({
      producto: null,
      cargando: false,
      error: new ApiError('No hubo respuesta del servidor.', 500),
      recargar,
    })

    expect(screen.getByText('No se pudo cargar el producto')).toBeInTheDocument()
    expect(screen.getByText('No hubo respuesta del servidor.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Reintentar' }))
    expect(recargar).toHaveBeenCalledTimes(1)
  })

  it('muestra el detalle completo del producto', () => {
    montar({
      producto: producto({ nombre: 'Campera Norte', precio: 74900, talles: ['M', 'L'] }),
      cargando: false,
      error: null,
      recargar: vi.fn(),
    })

    expect(screen.getByRole('heading', { name: 'Campera Norte' })).toBeInTheDocument()
    expect(screen.getByText(/74\.900/)).toBeInTheDocument()
    expect(screen.getByText('REMPERA')).toBeInTheDocument()
    expect(screen.getByText('10 en stock')).toBeInTheDocument()
    expect(screen.getByText('Talles: M · L')).toBeInTheDocument()
  })

  it('Agregar al carrito llama agregar con el producto', async () => {
    const user = userEvent.setup()
    const p = producto({ nombre: 'Campera Norte' })
    const carrito = carritoCon()
    montar({ producto: p, cargando: false, error: null, recargar: vi.fn() }, carrito)

    await user.click(screen.getByRole('button', { name: 'Agregar al carrito' }))

    expect(carrito.agregar).toHaveBeenCalledWith(p)
  })
})
