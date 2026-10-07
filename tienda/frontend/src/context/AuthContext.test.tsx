import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useAuth } from '@/hooks/useAuth'
import { AuthProvider } from './AuthContext'

function Sonda() {
  const { usuario, entrar, salir } = useAuth()

  return (
    <div>
      <span data-testid="usuario">{usuario ?? 'nadie'}</span>
      <button onClick={() => entrar('ana')}>entrar</button>
      <button onClick={salir}>salir</button>
    </div>
  )
}

function renderizar() {
  return render(
    <AuthProvider>
      <Sonda />
    </AuthProvider>,
  )
}

describe('AuthContext', () => {
  it('arranca sin usuario', () => {
    renderizar()

    expect(screen.getByTestId('usuario')).toHaveTextContent('nadie')
  })

  it('entrar y salir cambian el usuario', async () => {
    const user = userEvent.setup()
    renderizar()

    await user.click(screen.getByRole('button', { name: 'entrar' }))
    expect(screen.getByTestId('usuario')).toHaveTextContent('ana')

    await user.click(screen.getByRole('button', { name: 'salir' }))
    expect(screen.getByTestId('usuario')).toHaveTextContent('nadie')
  })
})
