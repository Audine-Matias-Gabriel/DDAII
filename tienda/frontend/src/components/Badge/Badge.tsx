import type { ReactNode } from 'react'
import styles from './Badge.module.css'

export type BadgeProps = {
  children: ReactNode
  tono?: 'neutro' | 'exito' | 'error' | 'primario'
  className?: string
}

export function Badge({ children, tono = 'neutro', className }: BadgeProps) {
  return <span className={[styles.badge, styles[tono], className].filter(Boolean).join(' ')}>{children}</span>
}
