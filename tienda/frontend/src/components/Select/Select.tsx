import { useId } from 'react'
import type { SelectHTMLAttributes } from 'react'
import styles from './Select.module.css'

export type SelectOpcion = {
  value: string
  label: string
}

export type SelectProps = Omit<SelectHTMLAttributes<HTMLSelectElement>, 'className'> & {
  label: string
  opciones: SelectOpcion[]
  error?: string
  placeholder?: string
}

export function Select({
  label,
  opciones,
  error,
  placeholder,
  id,
  ...resto
}: SelectProps) {
  const idGenerado = useId()
  const selectId = id ?? idGenerado
  const errorId = `${selectId}-error`

  return (
    <div className={styles.campo}>
      <label className={styles.label} htmlFor={selectId}>
        {label}
      </label>

      <select
        id={selectId}
        className={styles.select}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? errorId : undefined}
        {...resto}
      >
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {opciones.map((opcion) => (
          <option key={opcion.value} value={opcion.value}>
            {opcion.label}
          </option>
        ))}
      </select>

      {error && (
        <span className={styles.error} id={errorId}>
          {error}
        </span>
      )}
    </div>
  )
}
