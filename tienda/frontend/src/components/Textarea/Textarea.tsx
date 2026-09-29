import { useId } from 'react'
import type { TextareaHTMLAttributes } from 'react'
import styles from './Textarea.module.css'

export type TextareaProps = Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, 'className'> & {
  label: string
  error?: string
}

export function Textarea({ label, error, id, ...resto }: TextareaProps) {
  const idGenerado = useId()
  const textareaId = id ?? idGenerado
  const errorId = `${textareaId}-error`

  return (
    <div className={styles.campo}>
      <label className={styles.label} htmlFor={textareaId}>
        {label}
      </label>

      <textarea
        id={textareaId}
        className={styles.textarea}
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
