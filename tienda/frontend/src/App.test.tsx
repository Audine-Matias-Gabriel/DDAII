import { screen } from '@testing-library/react'
import type { UseProductos } from '@/hooks/useProductos'
import { useProductos } from '@/hooks/useProductos'
import { App } from '@/App'
import { producto, renderConProviders } from '@/test/test-utils'

vi.mock('@/hooks/useProductos', () => ({ useProductos: vi.fn() }))

const catalogo: UseProductos = {
  productos: [producto({ id: '1', nombre: 'Remera Nike' })],
  cargando: false,
  error: null,
  recargar: vi.fn(),
}

function montar(entrada: string, conSesion: boolean) {
  vi.mocked(useProductos).mockReturnValue(catalogo)
  return renderConProviders(<App />, {
    initialEntries: [entrada],
    auth: conSesion
      ? { usuario: 'ana', entrar: vi.fn(), salir: vi.fn() }
      : { usuario: null, entrar: vi.fn(), salir: vi.fn() },
  })
}

describe('<App>', () => {
  it('sin sesión la ruta privada redirige al login', () => {
    montar('/', false)

    expect(screen.getByText('Ingresá para ver el catálogo.')).toBeInTheDocument()
    expect(screen.queryByText('Catálogo')).not.toBeInTheDocument()
  })

  it('con sesión muestra el catálogo', () => {
    montar('/', true)

    expect(screen.getByRole('heading', { name: 'Catálogo' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Remera Nike' })).toBeInTheDocument()
    expect(screen.getByText('Hola, ana')).toBeInTheDocument()
  })

  it('una ruta desconocida muestra el 404', () => {
    montar('/ruta-que-no-existe', true)

    expect(screen.getByRole('heading', { name: '404' })).toBeInTheDocument()
  })
})
