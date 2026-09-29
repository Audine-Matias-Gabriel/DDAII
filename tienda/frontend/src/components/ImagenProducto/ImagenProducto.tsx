import { useState } from 'react'
import { colorDesdeTexto, iniciales } from '@/lib/formatters'
import styles from './ImagenProducto.module.css'

export type ImagenProductoProps = {
  nombre: string
  imagenUrl?: string
  className?: string
}

/**
 * Si el producto trae imagenUrl la muestra; si no, dibuja un placeholder con
 * iniciales y un color estable derivado del nombre. Nunca se rompe la grilla.
 */
export function ImagenProducto({ nombre, imagenUrl, className }: ImagenProductoProps) {
  const [fallo, setFallo] = useState(false)
  const clases = [styles.contenedor, className].filter(Boolean).join(' ')

  if (!imagenUrl || fallo) {
    return (
      <div
        className={`${clases} ${styles.placeholder}`}
        style={{ backgroundColor: colorDesdeTexto(nombre) }}
        role="img"
        aria-label={nombre}
      >
        {iniciales(nombre)}
      </div>
    )
  }

  return (
    <img
      className={clases}
      src={imagenUrl}
      alt={nombre}
      loading="lazy"
      onError={() => setFallo(true)}
    />
  )
}
