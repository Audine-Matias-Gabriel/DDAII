import { act, renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'
import { useCarrito } from '@/hooks/useCarrito'
import { claveItem } from '@/lib/variantes'
import { crearPedido } from '@/services/productoService'
import { producto, stockPorGenero } from '@/test/test-utils'
import type { Pedido } from '@/types/Pedido'
import { CarritoProvider } from './CarritoContext'

vi.mock('@/services/productoService', () => ({
  listarProductos: vi.fn(),
  obtenerProducto: vi.fn(),
  crearPedido: vi.fn(),
}))

const pedidoBase: Pedido = {
  id: 15,
  clienteId: null,
  tiendaId: 1,
  fecha: '2024-03-15T12:00:00',
  estado: 'PENDIENTE',
  total: 60000,
  detalles: [],
}

const GENERO = 'UNISEX' as const
const TALLE = 'M'

function clave(id: string) {
  return claveItem(id, GENERO, TALLE)
}

function wrapper({ children }: { children: ReactNode }) {
  return <CarritoProvider>{children}</CarritoProvider>
}

function usarCarrito() {
  return renderHook(() => useCarrito(), { wrapper })
}

beforeEach(() => {
  vi.mocked(crearPedido).mockReset()
})

describe('CarritoContext - items', () => {
  it('agrega un producto nuevo con cantidad 1', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' }), GENERO, TALLE))

    expect(result.current.items).toHaveLength(1)
    expect(result.current.items[0]?.cantidad).toBe(1)
    expect(result.current.items[0]?.genero).toBe(GENERO)
    expect(result.current.items[0]?.talle).toBe(TALLE)
  })

  it('no agrega una variante sin stock', () => {
    const { result } = usarCarrito()

    act(() =>
      result.current.agregar(
        producto({ id: '5', stock: stockPorGenero({ UNISEX: { M: 0 } }) }),
        GENERO,
        TALLE,
      ),
    )

    expect(result.current.items).toHaveLength(0)
  })

  it('incrementa la cantidad al agregar la misma variante', () => {
    const { result } = usarCarrito()
    const p = producto({ id: '5' })

    act(() => {
      result.current.agregar(p, GENERO, TALLE)
      result.current.agregar(p, GENERO, TALLE)
    })

    expect(result.current.items).toHaveLength(1)
    expect(result.current.items[0]?.cantidad).toBe(2)
  })

  it('trata como filas distintas a la misma prenda en otro talle', () => {
    const { result } = usarCarrito()
    const p = producto({ id: '5' })

    act(() => {
      result.current.agregar(p, GENERO, 'S')
      result.current.agregar(p, GENERO, 'M')
    })

    expect(result.current.items).toHaveLength(2)
  })

  it('no supera el stock de la variante al agregar', () => {
    const { result } = usarCarrito()
    const p = producto({ id: '5', stock: stockPorGenero({ UNISEX: { M: 3 } }) })

    act(() => {
      for (let i = 0; i < 5; i++) {
        result.current.agregar(p, GENERO, TALLE)
      }
    })

    expect(result.current.items[0]?.cantidad).toBe(3)
  })

  it('cambiarCantidad setea la cantidad pedida', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' }), GENERO, TALLE))
    act(() => result.current.cambiarCantidad(clave('5'), 4))

    expect(result.current.items[0]?.cantidad).toBe(4)
  })

  it('cambiarCantidad limita al stock disponible', () => {
    const { result } = usarCarrito()

    act(() =>
      result.current.agregar(
        producto({ id: '5', stock: stockPorGenero({ UNISEX: { M: 10 } }) }),
        GENERO,
        TALLE,
      ),
    )
    act(() => result.current.cambiarCantidad(clave('5'), 99))

    expect(result.current.items[0]?.cantidad).toBe(10)
  })

  it('cambiarCantidad a 0 quita la fila', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' }), GENERO, TALLE))
    act(() => result.current.cambiarCantidad(clave('5'), 0))

    expect(result.current.items).toHaveLength(0)
  })

  it('cambiarCantidad a negativo quita la fila', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' }), GENERO, TALLE))
    act(() => result.current.cambiarCantidad(clave('5'), -1))

    expect(result.current.items).toHaveLength(0)
  })

  it('quitar elimina el item', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' }), GENERO, TALLE))
    act(() => result.current.quitar(clave('5')))

    expect(result.current.items).toHaveLength(0)
  })

  it('vaciar deja el carrito vacio', () => {
    const { result } = usarCarrito()

    act(() => {
      result.current.agregar(producto({ id: '1' }), GENERO, TALLE)
      result.current.agregar(producto({ id: '2' }), GENERO, TALLE)
    })
    act(() => result.current.vaciar())

    expect(result.current.items).toHaveLength(0)
  })

  it('calcula cantidadTotal y total', () => {
    const { result } = usarCarrito()

    act(() => {
      result.current.agregar(producto({ id: '1', precio: 100 }), GENERO, TALLE)
      result.current.agregar(producto({ id: '2', precio: 200 }), GENERO, TALLE)
    })
    act(() => result.current.cambiarCantidad(clave('2'), 3))

    expect(result.current.cantidadTotal).toBe(4)
    expect(result.current.total).toBe(700)
  })
})

describe('CarritoContext - confirmar', () => {
  it('no llama al backend si el carrito esta vacio', async () => {
    const { result } = usarCarrito()

    const resultado = await act(() => result.current.confirmar())

    expect(resultado).toEqual({ ok: false, mensaje: 'El carrito está vacío.' })
    expect(crearPedido).not.toHaveBeenCalled()
  })

  it('confirma, manda la variante y vacia el carrito', async () => {
    vi.mocked(crearPedido).mockResolvedValue({ ...pedidoBase, total: 50000 })
    const { result } = usarCarrito()

    act(() => {
      result.current.agregar(producto({ id: '5' }), GENERO, TALLE)
      result.current.cambiarCantidad(clave('5'), 2)
    })

    const resultado = await act(() => result.current.confirmar())

    expect(crearPedido).toHaveBeenCalledWith([
      { productoId: '5', cantidad: 2, genero: GENERO, talle: TALLE },
    ])
    expect(result.current.items).toEqual([])
    expect(resultado).toMatchObject({ ok: true })
    expect(resultado.mensaje).toContain('Pedido #15')
  })

  it('no vacia el carrito si la compra falla', async () => {
    vi.mocked(crearPedido).mockRejectedValue(new Error('Stock insuficiente'))
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' }), GENERO, TALLE))

    const resultado = await act(() => result.current.confirmar())

    expect(resultado).toEqual({ ok: false, mensaje: 'Stock insuficiente' })
    expect(result.current.items).toHaveLength(1)
  })
})
