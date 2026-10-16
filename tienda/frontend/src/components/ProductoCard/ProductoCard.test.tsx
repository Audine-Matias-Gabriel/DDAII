import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { producto, renderConProviders, stockPorGenero } from '@/test/test-utils'
import { ProductoCard } from './ProductoCard'

describe('ProductoCard', () => {
  it('muestra nombre, precio y tienda', () => {
    renderConProviders(<ProductoCard producto={producto()} onAgregar={vi.fn()} />)

    expect(screen.getByText('Remera Nike')).toBeInTheDocument()
    expect(screen.getByText(/25\.000/)).toBeInTheDocument()
    expect(screen.getByText('Indumentaria Sur')).toBeInTheDocument()
  })

  it('muestra el badge Usado', () => {
    renderConProviders(
      <ProductoCard producto={producto({ estado: 'USADO' })} onAgregar={vi.fn()} />,
    )

    expect(screen.getByText('Usado')).toBeInTheDocument()
  })

  it('deshabilita el boton si no hay stock en ninguna variante', () => {
    renderConProviders(
      <ProductoCard
        producto={producto({
          stock: stockPorGenero({ UNISEX: { S: 0, M: 0 } }),
        })}
        onAgregar={vi.fn()}
      />,
    )

    expect(screen.getByRole('button', { name: 'Sin stock' })).toBeDisabled()
  })

  it('exige elegir un talle antes de agregar', () => {
    renderConProviders(<ProductoCard producto={producto()} onAgregar={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Elegí talle' })).toBeDisabled()
  })

  it('llama onAgregar con el producto y la variante elegida', async () => {
    const onAgregar = vi.fn()
    const p = producto()
    const user = userEvent.setup()
    renderConProviders(<ProductoCard producto={p} onAgregar={onAgregar} />)

    await user.selectOptions(screen.getByLabelText('Talle'), 'M')
    await user.click(screen.getByRole('button', { name: 'Agregar' }))

    expect(onAgregar).toHaveBeenCalledWith(p, 'UNISEX', 'M')
  })

  it('permite elegir genero cuando hay varias variantes', async () => {
    const onAgregar = vi.fn()
    const p = producto({
      genero: 'HOMBRE',
      stock: stockPorGenero({ HOMBRE: { M: 2 }, MUJER: { M: 3 } }),
    })
    const user = userEvent.setup()
    renderConProviders(<ProductoCard producto={p} onAgregar={onAgregar} />)

    await user.selectOptions(screen.getByLabelText('Género'), 'MUJER')
    await user.selectOptions(screen.getByLabelText('Talle'), 'M')
    await user.click(screen.getByRole('button', { name: 'Agregar' }))

    expect(onAgregar).toHaveBeenCalledWith(p, 'MUJER', 'M')
  })
})
