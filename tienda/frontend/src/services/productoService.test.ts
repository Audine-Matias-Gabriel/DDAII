import type { Pedido } from '@/types/Pedido'
import { apiGet, apiPost } from './api'
import { crearPedido, listarProductos, obtenerProducto } from './productoService'

vi.mock('./api', () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
}))

const crudo = {
  id: 5,
  nombre: 'Buzo',
  descripcion: 'Buzo de prueba',
  precio: 30000,
  stock: 4,
  categoria: 'BUZO',
  genero: 'UNISEX',
  estado: 'NUEVO',
  talles: ['M', 'L'],
  tiendaId: 2,
  imagenUrl: null,
  creadoEn: '2024-03-15T12:00:00',
}

beforeEach(() => {
  vi.mocked(apiGet).mockReset()
  vi.mocked(apiPost).mockReset()
})

describe('listarProductos', () => {
  it('normaliza id y tiendaId a string y los nulls a undefined/[]', async () => {
    vi.mocked(apiGet).mockResolvedValue([crudo])

    const [producto] = await listarProductos()

    expect(producto.id).toBe('5')
    expect(producto.tiendaId).toBe('2')
    expect(producto.talles).toEqual(['M', 'L'])
    expect(producto.imagenUrl).toBeUndefined()
  })

  it('conserva el resto de los campos', async () => {
    vi.mocked(apiGet).mockResolvedValue([crudo])

    const [producto] = await listarProductos()

    expect(producto).toMatchObject({
      nombre: 'Buzo',
      precio: 30000,
      stock: 4,
      categoria: 'BUZO',
      estado: 'NUEVO',
      creadoEn: '2024-03-15T12:00:00',
    })
  })

  it('consulta /api/productos', async () => {
    vi.mocked(apiGet).mockResolvedValue([])

    await listarProductos()

    expect(apiGet).toHaveBeenCalledWith('/api/productos')
  })

  it('convierte talles null en arreglo vacio y conserva una imagenUrl presente', async () => {
    vi.mocked(apiGet).mockResolvedValue([
      { ...crudo, talles: null, imagenUrl: 'http://img/1.png' },
    ])

    const [producto] = await listarProductos()

    expect(producto.talles).toEqual([])
    expect(producto.imagenUrl).toBe('http://img/1.png')
  })
})

describe('obtenerProducto', () => {
  it('pide el producto por id y lo normaliza', async () => {
    vi.mocked(apiGet).mockResolvedValue(crudo)

    const producto = await obtenerProducto('5')

    expect(apiGet).toHaveBeenCalledWith('/api/productos/5')
    expect(producto.id).toBe('5')
  })
})

describe('crearPedido', () => {
  const pedido: Pedido = {
    id: 15,
    clienteId: null,
    tiendaId: 2,
    fecha: '2024-03-15T12:00:00',
    estado: 'PENDIENTE',
    total: 60000,
    detalles: [],
  }

  it('manda el productoId como numero y devuelve el pedido', async () => {
    vi.mocked(apiPost).mockResolvedValue(pedido)

    const resultado = await crearPedido([{ productoId: '5', cantidad: 2 }])

    expect(apiPost).toHaveBeenCalledWith('/api/pedidos', {
      detalles: [{ productoId: 5, cantidad: 2 }],
    })
    expect(resultado).toBe(pedido)
  })

  it('no manda el precio (lo resuelve el backend)', async () => {
    vi.mocked(apiPost).mockResolvedValue(pedido)

    await crearPedido([{ productoId: '5', cantidad: 2 }])

    const body = vi.mocked(apiPost).mock.calls[0][1] as {
      detalles: Record<string, unknown>[]
    }
    expect(body.detalles[0]).not.toHaveProperty('precio')
  })
})
