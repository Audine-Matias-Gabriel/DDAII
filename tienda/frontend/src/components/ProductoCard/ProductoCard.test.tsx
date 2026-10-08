import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { producto, renderConProviders } from '@/test/test-utils'
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

  it('deshabilita el boton si no hay stock', () => {
    renderConProviders(<ProductoCard producto={producto({ stock: 0 })} onAgregar={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Sin stock' })).toBeDisabled()
  })

  it('llama onAgregar con el producto', async () => {
    const onAgregar = vi.fn()
    const p = producto()
    const user = userEvent.setup()
    renderConProviders(<ProductoCard producto={p} onAgregar={onAgregar} />)

    await user.click(screen.getByRole('button', { name: 'Agregar' }))

    expect(onAgregar).toHaveBeenCalledWith(p)
  })
})
