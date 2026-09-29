import type { ButtonHTMLAttributes } from 'react'
import styles from './Button.module.css'

type Variante = 'primario' | 'secundario' | 'peligro'
type Tamano = 'md' | 'sm'

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variante?: Variante
  tamano?: Tamano
  cargando?: boolean
}

export function Button({
  variante = 'primario',
  tamano = 'md',
  cargando = false,
  className,
  children,
  disabled,
  ...resto
}: ButtonProps) {
  const clases = [styles.boton, styles[variante], styles[tamano], className]
    .filter(Boolean)
    .join(' ')

  return (
    <button
      className={clases}
      disabled={disabled || cargando}
      aria-busy={cargando}
      {...resto}
    >
      {cargando ? 'Cargando...' : children}
    </button>
  )
}
