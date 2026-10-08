import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import { renderConProviders } from '@/test/test-utils'
import { LoginPage } from './LoginPage'

function rutas() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<p>catalogo</p>} />
    </Routes>
  )
}

describe('<LoginPage>', () => {
  it('submit con email → entra con el prefijo del correo y navega al catalogo', async () => {
    const user = userEvent.setup()
    const entrar = vi.fn()
    renderConProviders(rutas(), {
      initialEntries: ['/login'],
      auth: { usuario: null, entrar, salir: vi.fn() },
    })

    await user.type(screen.getByLabelText('Email'), 'demo@tienda.com')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(entrar).toHaveBeenCalledWith('demo')
    expect(await screen.findByText('catalogo')).toBeInTheDocument()
  })

  it('submit sin email → entra como invitado', async () => {
    const user = userEvent.setup()
    const entrar = vi.fn()
    renderConProviders(rutas(), {
      initialEntries: ['/login'],
      auth: { usuario: null, entrar, salir: vi.fn() },
    })

    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(entrar).toHaveBeenCalledWith('invitado')
    expect(await screen.findByText('catalogo')).toBeInTheDocument()
  })

  it('la contraseña no se envia a ningun lado', async () => {
    const user = userEvent.setup()
    const entrar = vi.fn()
    renderConProviders(rutas(), {
      initialEntries: ['/login'],
      auth: { usuario: null, entrar, salir: vi.fn() },
    })

    await user.type(screen.getByLabelText('Email'), 'demo@tienda.com')
    await user.type(screen.getByLabelText('Contraseña'), 'secreto123')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(entrar).toHaveBeenCalledTimes(1)
    expect(entrar.mock.calls[0]).toHaveLength(1)
  })

  it('la contraseña vacía no bloquea el submit', async () => {
    const user = userEvent.setup()
    const entrar = vi.fn()
    renderConProviders(rutas(), {
      initialEntries: ['/login'],
      auth: { usuario: null, entrar, salir: vi.fn() },
    })

    await user.type(screen.getByLabelText('Email'), 'ana@mail.com')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(entrar).toHaveBeenCalledWith('ana')
    expect(await screen.findByText('catalogo')).toBeInTheDocument()
  })
})
