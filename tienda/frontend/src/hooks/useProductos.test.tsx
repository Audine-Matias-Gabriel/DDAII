import { act, renderHook, waitFor } from '@testing-library/react'
import { listarProductos } from '@/services/productoService'
import { producto } from '@/test/test-utils'
import { useProductos } from './useProductos'

vi.mock('@/services/productoService', () => ({
  listarProductos: vi.fn(),
  obtenerProducto: vi.fn(),
  crearPedido: vi.fn(),
}))

beforeEach(() => {
  vi.mocked(listarProductos).mockReset()
})

describe('useProductos', () => {
  it('arranca cargando y termina con los productos', async () => {
    vi.mocked(listarProductos).mockResolvedValue([producto()])

    const { result } = renderHook(() => useProductos())

    expect(result.current.cargando).toBe(true)

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.productos).toHaveLength(1)
    expect(result.current.error).toBeNull()
  })

  it('expone el mensaje cuando el servicio falla', async () => {
    vi.mocked(listarProductos).mockRejectedValue(new Error('boom'))

    const { result } = renderHook(() => useProductos())

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.error).toBe('boom')
    expect(result.current.productos).toEqual([])
  })

  it('usa un mensaje generico si el error no es un Error', async () => {
    vi.mocked(listarProductos).mockRejectedValue('boom')

    const { result } = renderHook(() => useProductos())

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.error).toBe('Error inesperado')
  })

  it('recargar vuelve a cargando y consulta de nuevo', async () => {
    vi.mocked(listarProductos)
      .mockResolvedValueOnce([producto()])
      .mockResolvedValueOnce([producto({ id: '2', nombre: 'Campera' })])

    const { result } = renderHook(() => useProductos())
    await waitFor(() => expect(result.current.cargando).toBe(false))

    act(() => result.current.recargar())
    expect(result.current.cargando).toBe(true)

    await waitFor(() => expect(result.current.productos[0]?.nombre).toBe('Campera'))
    expect(listarProductos).toHaveBeenCalledTimes(2)
  })
})
