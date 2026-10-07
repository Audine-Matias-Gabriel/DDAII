import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { UseProductos } from '@/hooks/useProductos'
import { useProductos } from '@/hooks/useProductos'
import { producto, renderConProviders } from '@/test/test-utils'
import { ShopPage } from './ShopPage'

vi.mock('@/hooks/useProductos', () => ({ useProductos: vi.fn() }))

function estado(overrides: Partial<UseProductos> = {}): UseProductos {
  return { productos: [], cargando: true, error: null, recargar: vi.fn(), ...overrides }
}

function montar(useProductosValor: UseProductos) {
  vi.mocked(useProductos).mockReturnValue(useProductosValor)
  return renderConProviders(<ShopPage />)
}

describe('<ShopPage>', () => {
  it('mientras carga muestra el estado de cargando', () => {
    montar(estado())

    expect(screen.getByText('Cargando catálogo...')).toBeInTheDocument()
  })

  it('ante un error muestra el EmptyState y Reintentar llama recargar', async () => {
    const user = userEvent.setup()
    const recargar = vi.fn()
    montar(estado({ cargando: false, error: 'No hubo respuesta del servidor.', recargar }))

    expect(screen.getByText('No se pudo cargar el catálogo')).toBeInTheDocument()
    expect(screen.getByText('No hubo respuesta del servidor.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Reintentar' }))
    expect(recargar).toHaveBeenCalledTimes(1)
  })

  it('con productos renderiza una tarjeta por producto', () => {
    montar(
      estado({
        cargando: false,
        productos: [
          producto({ id: '1', nombre: 'Remera Nike' }),
          producto({ id: '2', nombre: 'Buzo Adidas', tiendaId: '2' }),
        ],
      }),
    )

    expect(screen.getAllByRole('article')).toHaveLength(2)
    expect(screen.getByRole('heading', { name: 'Remera Nike' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Buzo Adidas' })).toBeInTheDocument()
  })

  it('el buscador filtra por nombre', async () => {
    const user = userEvent.setup()
    montar(
      estado({
        cargando: false,
        productos: [
          producto({ id: '1', nombre: 'Remera Nike' }),
          producto({ id: '2', nombre: 'Buzo Adidas', tiendaId: '2' }),
        ],
      }),
    )

    await user.type(screen.getByLabelText('Buscar'), 'buzo')

    expect(screen.getAllByRole('article')).toHaveLength(1)
    expect(screen.getByRole('heading', { name: 'Buzo Adidas' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Remera Nike' })).not.toBeInTheDocument()
  })

  it('sin coincidencias muestra el estado vacío', async () => {
    const user = userEvent.setup()
    montar(estado({ cargando: false, productos: [producto()] }))

    await user.type(screen.getByLabelText('Buscar'), 'zzzz')

    expect(screen.getByText('Sin resultados')).toBeInTheDocument()
    expect(
      screen.getByText('No hay productos que coincidan con "zzzz".'),
    ).toBeInTheDocument()
    expect(screen.queryByRole('article')).not.toBeInTheDocument()
  })
})
