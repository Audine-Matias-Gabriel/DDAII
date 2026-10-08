import { useId } from 'react'
import type { InputHTMLAttributes } from 'react'
import styles from './Input.module.css'

export type InputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'className'> & {
  label: string
  error?: string
}

export function Input({ label, error, id, ...resto }: InputProps) {
  const idGenerado = useId()
  const inputId = id ?? idGenerado
  const errorId = `${inputId}-error`

  return (
    <div className={styles.campo}>
      <label className={styles.label} htmlFor={inputId}>
        {label}
      </label>

      <input
        id={inputId}
        className={styles.input}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? errorId : undefined}
        {...resto}
      />

      {error && (
        <span className={styles.error} id={errorId}>
          {error}
        </span>
      )}
    </div>
  )
}
