import { useEffect, useRef, useState } from 'react'
import { useCarrito } from '@/hooks/useCarrito'
import { Button } from '@/components/Button/Button'
import styles from './CarritoMenu.module.css'

/** Botón con contador que abre una lista con los productos agregados. */
export function CarritoMenu() {
  const { productos, vaciar } = useCarrito()
  const [abierto, setAbierto] = useState(false)
  const contenedorRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!abierto) return

    const onClickFuera = (e: MouseEvent) => {
      if (!contenedorRef.current?.contains(e.target as Node)) {
        setAbierto(false)
      }
    }
    const onEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setAbierto(false)
    }

    document.addEventListener('mousedown', onClickFuera)
    document.addEventListener('keydown', onEscape)
    return () => {
      document.removeEventListener('mousedown', onClickFuera)
      document.removeEventListener('keydown', onEscape)
    }
  }, [abierto])

  return (
    <div className={styles.contenedor} ref={contenedorRef}>
      <Button
        variante="secundario"
        tamano="sm"
        onClick={() => setAbierto((v) => !v)}
        aria-expanded={abierto}
        aria-haspopup="true"
      >
        Carrito
        {productos.length > 0 && <span className={styles.contador}>{productos.length}</span>}
      </Button>

      {abierto && (
        <div className={styles.desplegable} role="menu">
          <p className={styles.titulo}>Productos agregados</p>

          {productos.length === 0 ? (
            <p className={styles.vacio}>Todavía no agregaste nada.</p>
          ) : (
            <ul className={styles.lista}>
              {productos.map((producto) => (
                <li key={producto.id} className={styles.item}>
                  {producto.nombre}
                </li>
              ))}
            </ul>
          )}

          {productos.length > 0 && (
            <button className={styles.vaciar} onClick={vaciar}>
              Vaciar
            </button>
          )}
        </div>
      )}
    </div>
  )
}
