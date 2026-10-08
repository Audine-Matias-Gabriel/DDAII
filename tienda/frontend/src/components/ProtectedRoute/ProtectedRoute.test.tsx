import { screen } from '@testing-library/react'
import { Route, Routes } from 'react-router-dom'
import { renderConProviders } from '@/test/test-utils'
import { ProtectedRoute } from './ProtectedRoute'

function rutas() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <p>privado</p>
          </ProtectedRoute>
        }
      />
      <Route path="/login" element={<p>login</p>} />
    </Routes>
  )
}

describe('ProtectedRoute', () => {
  it('redirige al login si no hay sesion', () => {
    renderConProviders(rutas())

    expect(screen.getByText('login')).toBeInTheDocument()
    expect(screen.queryByText('privado')).not.toBeInTheDocument()
  })

  it('muestra el contenido si hay sesion', () => {
    renderConProviders(rutas(), {
      auth: { usuario: 'ana', entrar: vi.fn(), salir: vi.fn() },
    })

    expect(screen.getByText('privado')).toBeInTheDocument()
  })
})
