import { fireEvent, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import type { CarritoContextValue, ResultadoCompra } from '@/context/CarritoContext'
import { claveItem } from '@/lib/variantes'
import { producto, renderConProviders, stockPorGenero } from '@/test/test-utils'
import { CarritoMenu } from './CarritoMenu'

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

const CLAVE_BUZO = claveItem('5', 'UNISEX', 'M')

function itemBuzo(cantidad = 1, stock = 10) {
  return {
    producto: producto({
      id: '5',
      nombre: 'Buzo',
      precio: 30000,
      stock: stockPorGenero({ UNISEX: { M: stock } }),
    }),
    genero: 'UNISEX' as const,
    talle: 'M',
    cantidad,
  }
}

function abrir(user: ReturnType<typeof userEvent.setup>) {
  return user.click(screen.getByRole('button', { name: /Carrito/ }))
}

const titulo = 'Productos agregados'

describe('CarritoMenu', () => {
  it('abre y cierra el desplegable', async () => {
    const user = userEvent.setup()
    renderConProviders(<CarritoMenu />, { carrito: carritoCon() })

    expect(screen.queryByText(titulo)).not.toBeInTheDocument()

    await abrir(user)
    expect(screen.getByText(titulo)).toBeInTheDocument()

    await abrir(user)
    expect(screen.queryByText(titulo)).not.toBeInTheDocument()
  })

  it('muestra el mensaje de carrito vacio', async () => {
    const user = userEvent.setup()
    renderConProviders(<CarritoMenu />, { carrito: carritoCon() })

    await abrir(user)

    expect(screen.getByText('Todavía no agregaste nada.')).toBeInTheDocument()
  })

  it('muestra el contador con la cantidad total', () => {
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({ items: [itemBuzo(2)], cantidadTotal: 2, total: 60000 }),
    })

    expect(screen.getByRole('button', { name: /Carrito/ })).toHaveTextContent('2')
  })

  it('muestra los items con precio unitario y subtotal', async () => {
    const user = userEvent.setup()
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({ items: [itemBuzo(2)], cantidadTotal: 2, total: 60000 }),
    })

    await abrir(user)

    expect(screen.getByText('Buzo')).toBeInTheDocument()
    expect(screen.getByText('Unisex · Talle M')).toBeInTheDocument()
    expect(screen.getByText(/30\.000 c\/u/)).toBeInTheDocument()
    expect(screen.getAllByText(/60\.000/)).toHaveLength(2)
  })

  it('el nombre del item navega al detalle y cierra el desplegable', async () => {
    const user = userEvent.setup()
    renderConProviders(
      <Routes>
        <Route path="/" element={<CarritoMenu />} />
        <Route
          path="/productos/:id"
          element={
            <div>
              <span>Detalle del producto</span>
              <span>id:5</span>
            </div>
          }
        />
      </Routes>,
      { carrito: carritoCon({ items: [itemBuzo(1)], cantidadTotal: 1, total: 30000 }) },
    )

    await abrir(user)
    await user.click(screen.getByRole('link', { name: 'Buzo' }))

    expect(screen.getByText('Detalle del producto')).toBeInTheDocument()
    expect(screen.queryByText('Productos agregados')).not.toBeInTheDocument()
  })

  it('el boton + incrementa la cantidad', async () => {
    const user = userEvent.setup()
    const cambiarCantidad = vi.fn()
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({
        items: [itemBuzo(1)],
        cantidadTotal: 1,
        total: 30000,
        cambiarCantidad,
      }),
    })

    await abrir(user)
    await user.click(screen.getByRole('button', { name: 'Agregar una unidad de Buzo' }))

    expect(cambiarCantidad).toHaveBeenCalledWith(CLAVE_BUZO, 2)
  })

  it('deshabilita + al llegar al stock', async () => {
    const user = userEvent.setup()
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({ items: [itemBuzo(3, 3)], cantidadTotal: 3, total: 90000 }),
    })

    await abrir(user)

    expect(screen.getByRole('button', { name: 'Agregar una unidad de Buzo' })).toBeDisabled()
  })

  it('el boton - decrementa la cantidad', async () => {
    const user = userEvent.setup()
    const cambiarCantidad = vi.fn()
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({
        items: [itemBuzo(1)],
        cantidadTotal: 1,
        total: 30000,
        cambiarCantidad,
      }),
    })

    await abrir(user)
    await user.click(screen.getByRole('button', { name: 'Quitar una unidad de Buzo' }))

    expect(cambiarCantidad).toHaveBeenCalledWith(CLAVE_BUZO, 0)
  })

  it('Quitar saca el item', async () => {
    const user = userEvent.setup()
    const quitar = vi.fn()
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({ items: [itemBuzo(1)], cantidadTotal: 1, total: 30000, quitar }),
    })

    await abrir(user)
    await user.click(screen.getByRole('button', { name: 'Quitar' }))

    expect(quitar).toHaveBeenCalledWith(CLAVE_BUZO)
  })

  it('Vaciar vacia el carrito', async () => {
    const user = userEvent.setup()
    const vaciar = vi.fn()
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({ items: [itemBuzo(1)], cantidadTotal: 1, total: 30000, vaciar }),
    })

    await abrir(user)
    await user.click(screen.getByRole('button', { name: 'Vaciar' }))

    expect(vaciar).toHaveBeenCalledTimes(1)
  })

  it('confirmar OK muestra el mensaje de exito', async () => {
    const user = userEvent.setup()
    const confirmar = vi
      .fn()
      .mockResolvedValue({ ok: true, mensaje: 'Pedido #15 confirmado.' } as ResultadoCompra)
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({ items: [itemBuzo(1)], cantidadTotal: 1, total: 30000, confirmar }),
    })

    await abrir(user)
    await user.click(screen.getByRole('button', { name: 'Confirmar compra' }))

    expect(confirmar).toHaveBeenCalledTimes(1)
    expect(await screen.findByText('Pedido #15 confirmado.')).toBeInTheDocument()
  })

  it('confirmar con error muestra el mensaje de error', async () => {
    const user = userEvent.setup()
    const confirmar = vi
      .fn()
      .mockResolvedValue({ ok: false, mensaje: 'Stock insuficiente' } as ResultadoCompra)
    renderConProviders(<CarritoMenu />, {
      carrito: carritoCon({ items: [itemBuzo(1)], cantidadTotal: 1, total: 30000, confirmar }),
    })

    await abrir(user)
    await user.click(screen.getByRole('button', { name: 'Confirmar compra' }))

    expect(await screen.findByText('Stock insuficiente')).toBeInTheDocument()
  })

  it('cierra con la tecla Escape', async () => {
    const user = userEvent.setup()
    renderConProviders(<CarritoMenu />, { carrito: carritoCon() })

    await abrir(user)
    expect(screen.getByText(titulo)).toBeInTheDocument()

    fireEvent.keyDown(document, { key: 'Escape' })

    expect(screen.queryByText(titulo)).not.toBeInTheDocument()
  })

  it('cierra al hacer click fuera', async () => {
    const user = userEvent.setup()
    renderConProviders(<CarritoMenu />, { carrito: carritoCon() })

    await abrir(user)
    expect(screen.getByText(titulo)).toBeInTheDocument()

    fireEvent.mouseDown(document.body)

    expect(screen.queryByText(titulo)).not.toBeInTheDocument()
  })
})
