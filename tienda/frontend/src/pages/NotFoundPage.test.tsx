import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import { renderConProviders } from '@/test/test-utils'
import { NotFoundPage } from './NotFoundPage'

function rutas() {
  return (
    <Routes>
      <Route path="/noexiste" element={<NotFoundPage />} />
      <Route path="/" element={<p>catalogo</p>} />
    </Routes>
  )
}

describe('<NotFoundPage>', () => {
  it('muestra el codigo 404', () => {
    renderConProviders(rutas(), { initialEntries: ['/noexiste'] })

    expect(screen.getByRole('heading', { name: '404' })).toBeInTheDocument()
    expect(screen.getByText('No encontramos la página que buscabas.')).toBeInTheDocument()
  })

  it('Volver al inicio navega al catalogo', async () => {
    const user = userEvent.setup()
    renderConProviders(rutas(), { initialEntries: ['/noexiste'] })

    await user.click(screen.getByRole('button', { name: 'Volver al inicio' }))

    expect(await screen.findByText('catalogo')).toBeInTheDocument()
  })
})
