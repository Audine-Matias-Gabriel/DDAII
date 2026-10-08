import { act, renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'
import { useCarrito } from '@/hooks/useCarrito'
import { crearPedido } from '@/services/productoService'
import { producto } from '@/test/test-utils'
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

    act(() => result.current.agregar(producto({ id: '5' })))

    expect(result.current.items).toHaveLength(1)
    expect(result.current.items[0]?.cantidad).toBe(1)
  })

  it('no agrega un producto sin stock', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5', stock: 0 })))

    expect(result.current.items).toHaveLength(0)
  })

  it('incrementa la cantidad al agregar el mismo producto', () => {
    const { result } = usarCarrito()
    const p = producto({ id: '5' })

    act(() => {
      result.current.agregar(p)
      result.current.agregar(p)
    })

    expect(result.current.items).toHaveLength(1)
    expect(result.current.items[0]?.cantidad).toBe(2)
  })

  it('no supera el stock disponible al agregar', () => {
    const { result } = usarCarrito()
    const p = producto({ id: '5', stock: 3 })

    act(() => {
      for (let i = 0; i < 5; i++) {
        result.current.agregar(p)
      }
    })

    expect(result.current.items[0]?.cantidad).toBe(3)
  })

  it('cambiarCantidad setea la cantidad pedida', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5', stock: 10 })))
    act(() => result.current.cambiarCantidad('5', 4))

    expect(result.current.items[0]?.cantidad).toBe(4)
  })

  it('cambiarCantidad limita al stock disponible', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5', stock: 10 })))
    act(() => result.current.cambiarCantidad('5', 99))

    expect(result.current.items[0]?.cantidad).toBe(10)
  })

  it('cambiarCantidad a 0 quita la fila', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' })))
    act(() => result.current.cambiarCantidad('5', 0))

    expect(result.current.items).toHaveLength(0)
  })

  it('cambiarCantidad a negativo quita la fila', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' })))
    act(() => result.current.cambiarCantidad('5', -1))

    expect(result.current.items).toHaveLength(0)
  })

  it('quitar elimina el item', () => {
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' })))
    act(() => result.current.quitar('5'))

    expect(result.current.items).toHaveLength(0)
  })

  it('vaciar deja el carrito vacio', () => {
    const { result } = usarCarrito()

    act(() => {
      result.current.agregar(producto({ id: '1' }))
      result.current.agregar(producto({ id: '2' }))
    })
    act(() => result.current.vaciar())

    expect(result.current.items).toHaveLength(0)
  })

  it('calcula cantidadTotal y total', () => {
    const { result } = usarCarrito()

    act(() => {
      result.current.agregar(producto({ id: '1', precio: 100, stock: 5 }))
      result.current.agregar(producto({ id: '2', precio: 200, stock: 5 }))
    })
    act(() => result.current.cambiarCantidad('2', 3))

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

  it('confirma, manda productoId/cantidad y vacia el carrito', async () => {
    vi.mocked(crearPedido).mockResolvedValue({ ...pedidoBase, total: 50000 })
    const { result } = usarCarrito()

    act(() => {
      result.current.agregar(producto({ id: '5' }))
      result.current.cambiarCantidad('5', 2)
    })

    const resultado = await act(() => result.current.confirmar())

    expect(crearPedido).toHaveBeenCalledWith([{ productoId: '5', cantidad: 2 }])
    expect(result.current.items).toEqual([])
    expect(resultado).toMatchObject({ ok: true })
    expect(resultado.mensaje).toContain('Pedido #15')
  })

  it('no vacia el carrito si la compra falla', async () => {
    vi.mocked(crearPedido).mockRejectedValue(new Error('Stock insuficiente'))
    const { result } = usarCarrito()

    act(() => result.current.agregar(producto({ id: '5' })))

    const resultado = await act(() => result.current.confirmar())

    expect(resultado).toEqual({ ok: false, mensaje: 'Stock insuficiente' })
    expect(result.current.items).toHaveLength(1)
  })
})
