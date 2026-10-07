import { render, screen } from '@testing-library/react'
import { producto, renderConProviders } from './test-utils'

describe('entorno de tests', () => {
  it('renderiza con Testing Library y matchers de jest-dom', () => {
    render(<p>hola</p>)

    expect(screen.getByText('hola')).toBeInTheDocument()
  })

  it('el helper entrega los providers y un producto de prueba', () => {
    renderConProviders(<span>{producto().nombre}</span>)

    expect(screen.getByText('Remera Nike')).toBeInTheDocument()
  })
})
