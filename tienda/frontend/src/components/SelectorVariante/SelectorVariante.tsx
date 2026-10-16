import { useId } from 'react'
import type { Genero, Producto } from '@/types/Producto'
import { etiquetaGenero, stockDe } from '@/lib/variantes'
import styles from './SelectorVariante.module.css'

export type SelectorVarianteProps = {
  producto: Producto
  generos: Genero[]
  talles: string[]
  genero: Genero | ''
  talle: string
  onGenero: (genero: Genero) => void
  onTalle: (talle: string) => void
  compacto?: boolean
}

/**
 * Selectores de género y talle de una variante.
 *
 * El género solo se muestra como <select> cuando el producto tiene más de una
 * clave; con una sola se indica como texto. Los talles sin stock quedan
 * deshabilitados.
 */
export function SelectorVariante({
  producto,
  generos,
  talles,
  genero,
  talle,
  onGenero,
  onTalle,
  compacto = false,
}: SelectorVarianteProps) {
  const id = useId()
  const idGenero = `${id}-genero`
  const idTalle = `${id}-talle`

  return (
    <div
      className={[styles.selector, compacto && styles.compacto]
        .filter(Boolean)
        .join(' ')}
    >
      {generos.length > 1 ? (
        <div className={styles.campo}>
          <label className={styles.label} htmlFor={idGenero}>
            Género
          </label>
          <select
            id={idGenero}
            className={styles.select}
            value={genero}
            onChange={(e) => onGenero(e.target.value as Genero)}
          >
            {generos.map((g) => (
              <option key={g} value={g}>
                {etiquetaGenero(g)}
              </option>
            ))}
          </select>
        </div>
      ) : (
        generos.length === 1 && (
          <p className={styles.generoUnico}>
            Género: {etiquetaGenero(generos[0])}
          </p>
        )
      )}

      <div className={styles.campo}>
        <label className={styles.label} htmlFor={idTalle}>
          Talle
        </label>
        <select
          id={idTalle}
          className={styles.select}
          value={talle}
          onChange={(e) => onTalle(e.target.value)}
        >
          <option value="" disabled>
            Elegir talle
          </option>
          {talles.map((t) => (
            <option
              key={t}
              value={t}
              disabled={stockDe(producto, genero, t) === 0}
            >
              {t}
            </option>
          ))}
        </select>
      </div>
    </div>
  )
}
