import { fireEvent, render, screen } from '@testing-library/react'
import { ImagenProducto } from './ImagenProducto'

describe('ImagenProducto', () => {
  it('dibuja un placeholder con iniciales si no hay imagenUrl', () => {
    render(<ImagenProducto nombre="Remera Nike" />)

    const placeholder = screen.getByRole('img', { name: 'Remera Nike' })
    expect(placeholder.tagName).toBe('DIV')
    expect(placeholder).toHaveTextContent('RN')
  })

  it('muestra la imagen cuando hay url', () => {
    render(<ImagenProducto nombre="Remera Nike" imagenUrl="http://img/1.png" />)

    const img = screen.getByRole('img', { name: 'Remera Nike' })
    expect(img.tagName).toBe('IMG')
    expect(img).toHaveAttribute('src', 'http://img/1.png')
  })

  it('cae al placeholder si la imagen falla', () => {
    render(<ImagenProducto nombre="Remera Nike" imagenUrl="http://img/rota.png" />)

    fireEvent.error(screen.getByRole('img', { name: 'Remera Nike' }))

    expect(screen.getByRole('img', { name: 'Remera Nike' }).tagName).toBe('DIV')
  })
})
