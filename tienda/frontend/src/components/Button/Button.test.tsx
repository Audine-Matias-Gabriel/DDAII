import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Button } from './Button'

describe('Button', () => {
  it('renderiza el contenido', () => {
    render(<Button>Comprar</Button>)

    expect(screen.getByRole('button', { name: 'Comprar' })).toBeInTheDocument()
  })

  it('mientras carga muestra Cargando y queda deshabilitado', () => {
    render(<Button cargando>Comprar</Button>)

    const boton = screen.getByRole('button', { name: 'Cargando...' })
    expect(boton).toBeDisabled()
    expect(boton).toHaveAttribute('aria-busy', 'true')
  })

  it('dispara onClick', async () => {
    const onClick = vi.fn()
    const user = userEvent.setup()
    render(<Button onClick={onClick}>Comprar</Button>)

    await user.click(screen.getByRole('button', { name: 'Comprar' }))

    expect(onClick).toHaveBeenCalledTimes(1)
  })
})
