import { render, screen } from '@testing-library/react'
import { Badge } from './Badge'
import styles from './Badge.module.css'

describe('Badge', () => {
  it('renderiza el contenido', () => {
    render(<Badge>Usado</Badge>)

    expect(screen.getByText('Usado')).toBeInTheDocument()
  })

  it('usa el tono neutro por defecto', () => {
    render(<Badge>Usado</Badge>)

    expect(screen.getByText('Usado')).toHaveClass(styles.neutro)
  })

  it('aplica el tono indicado', () => {
    render(<Badge tono="exito">Disponible</Badge>)

    expect(screen.getByText('Disponible')).toHaveClass(styles.exito)
  })
})
