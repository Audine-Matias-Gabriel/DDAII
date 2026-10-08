import { render, screen } from '@testing-library/react'
import { EmptyState } from './EmptyState'

describe('EmptyState', () => {
  it('muestra el titulo', () => {
    render(<EmptyState titulo="Sin resultados" />)

    expect(screen.getByText('Sin resultados')).toBeInTheDocument()
  })

  it('muestra la descripcion cuando viene', () => {
    render(<EmptyState titulo="Sin resultados" descripcion="Proba otra busqueda" />)

    expect(screen.getByText('Proba otra busqueda')).toBeInTheDocument()
  })

  it('muestra la accion solo si viene', () => {
    const { rerender } = render(<EmptyState titulo="Sin resultados" />)

    expect(screen.queryByRole('button', { name: 'Reintentar' })).not.toBeInTheDocument()

    rerender(<EmptyState titulo="Sin resultados" accion={<button>Reintentar</button>} />)

    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
  })
})
