import type { ReactNode } from 'react'
import styles from './EmptyState.module.css'

export type EmptyStateProps = {
  titulo: string
  descripcion?: string
  accion?: ReactNode
}

export function EmptyState({ titulo, descripcion, accion }: EmptyStateProps) {
  return (
    <div className={styles.contenedor}>
      <h3 className={styles.titulo}>{titulo}</h3>
      {descripcion && <p className={styles.descripcion}>{descripcion}</p>}
      {accion && <div className={styles.accion}>{accion}</div>}
    </div>
  )
}
