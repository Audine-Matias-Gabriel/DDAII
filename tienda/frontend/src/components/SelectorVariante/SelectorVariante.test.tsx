import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { producto, renderConProviders, stockPorGenero } from '@/test/test-utils'
import { SelectorVariante } from './SelectorVariante'

const base = {
  talles: ['S', 'M'],
  genero: 'UNISEX' as const,
  talle: '',
  onGenero: vi.fn(),
  onTalle: vi.fn(),
}

describe('SelectorVariante', () => {
  it('con un solo genero lo muestra como texto y no como select', () => {
    renderConProviders(
      <SelectorVariante producto={producto()} generos={['UNISEX']} {...base} />,
    )

    expect(screen.getByText('Género: Unisex')).toBeInTheDocument()
    expect(screen.queryByLabelText('Género')).not.toBeInTheDocument()
  })

  it('con varios generos muestra el select y avisa del cambio', async () => {
    const onGenero = vi.fn()
    const user = userEvent.setup()
    const p = producto({
      genero: 'HOMBRE',
      stock: stockPorGenero({ HOMBRE: { M: 2 }, MUJER: { M: 3 } }),
    })
    renderConProviders(
      <SelectorVariante
        producto={p}
        generos={['HOMBRE', 'MUJER']}
        {...base}
        genero="HOMBRE"
        onGenero={onGenero}
      />,
    )

    await user.selectOptions(screen.getByLabelText('Género'), 'MUJER')

    expect(onGenero).toHaveBeenCalledWith('MUJER')
  })

  it('deshabilita los talles sin stock y avisa del talle elegido', async () => {
    const onTalle = vi.fn()
    const user = userEvent.setup()
    const p = producto({
      stock: stockPorGenero({ UNISEX: { S: 0, M: 3 } }),
    })
    renderConProviders(
      <SelectorVariante producto={p} generos={['UNISEX']} {...base} onTalle={onTalle} />,
    )

    const opcionS = screen.getByRole('option', { name: 'S' })
    expect(opcionS).toBeDisabled()

    await user.selectOptions(screen.getByLabelText('Talle'), 'M')

    expect(onTalle).toHaveBeenCalledWith('M')
  })
})
