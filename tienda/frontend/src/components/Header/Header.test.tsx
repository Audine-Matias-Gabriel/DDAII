import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderConProviders } from '@/test/test-utils'
import { Header } from './Header'

describe('Header', () => {
  it('saluda al usuario', () => {
    renderConProviders(<Header usuario="ana" onLogout={vi.fn()} />)

    expect(screen.getByText('Hola, ana')).toBeInTheDocument()
  })

  it('no saluda si no hay usuario', () => {
    renderConProviders(<Header onLogout={vi.fn()} />)

    expect(screen.queryByText(/Hola,/)).not.toBeInTheDocument()
  })

  it('llama onLogout al salir', async () => {
    const onLogout = vi.fn()
    const user = userEvent.setup()
    renderConProviders(<Header usuario="ana" onLogout={onLogout} />)

    await user.click(screen.getByRole('button', { name: 'Salir' }))

    expect(onLogout).toHaveBeenCalledTimes(1)
  })

  it('muestra el enlace Shop', () => {
    renderConProviders(<Header onLogout={vi.fn()} />)

    expect(screen.getByRole('link', { name: 'Shop' })).toBeInTheDocument()
  })
})
