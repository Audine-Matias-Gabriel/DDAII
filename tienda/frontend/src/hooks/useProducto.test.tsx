import { act, renderHook, waitFor } from '@testing-library/react'
import { ApiError } from '@/services/api'
import { obtenerProducto } from '@/services/productoService'
import { producto } from '@/test/test-utils'
import { useProducto } from './useProducto'

vi.mock('@/services/productoService', () => ({
  listarProductos: vi.fn(),
  obtenerProducto: vi.fn(),
  crearPedido: vi.fn(),
}))

beforeEach(() => {
  vi.mocked(obtenerProducto).mockReset()
})

describe('useProducto', () => {
  it('devuelve un ApiError 400 si falta el id y no consulta', () => {
    const { result } = renderHook(() => useProducto(undefined))

    expect(result.current.cargando).toBe(false)
    expect(result.current.error).toBeInstanceOf(ApiError)
    expect(result.current.error?.status).toBe(400)
    expect(obtenerProducto).not.toHaveBeenCalled()
  })

  it('carga el producto cuando el id existe', async () => {
    vi.mocked(obtenerProducto).mockResolvedValue(producto())

    const { result } = renderHook(() => useProducto('5'))

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(obtenerProducto).toHaveBeenCalledWith('5')
    expect(result.current.producto?.nombre).toBe('Remera Nike')
    expect(result.current.error).toBeNull()
  })

  it('deja el error tipado como ApiError en un 404', async () => {
    vi.mocked(obtenerProducto).mockRejectedValue(new ApiError('Producto no encontrado', 404))

    const { result } = renderHook(() => useProducto('999'))

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.error?.status).toBe(404)
    expect(result.current.producto).toBeNull()
  })

  it('envuelve un error desconocido en un ApiError con status 0', async () => {
    vi.mocked(obtenerProducto).mockRejectedValue(new TypeError('x'))

    const { result } = renderHook(() => useProducto('5'))

    await waitFor(() => expect(result.current.cargando).toBe(false))
    expect(result.current.error).toBeInstanceOf(ApiError)
    expect(result.current.error?.status).toBe(0)
    expect(result.current.error?.message).toBe('Error inesperado')
  })

  it('recargar vuelve a pedir el mismo id', async () => {
    vi.mocked(obtenerProducto)
      .mockResolvedValueOnce(producto())
      .mockResolvedValueOnce(producto({ nombre: 'Campera' }))

    const { result } = renderHook(() => useProducto('5'))
    await waitFor(() => expect(result.current.cargando).toBe(false))

    act(() => result.current.recargar())
    expect(result.current.cargando).toBe(true)

    await waitFor(() => expect(result.current.producto?.nombre).toBe('Campera'))
    expect(obtenerProducto).toHaveBeenCalledTimes(2)
  })
})
