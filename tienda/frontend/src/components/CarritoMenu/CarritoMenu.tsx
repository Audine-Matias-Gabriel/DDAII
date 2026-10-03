import { useEffect, useRef, useState } from 'react'
import { useCarrito } from '@/hooks/useCarrito'
import { formatMoneda } from '@/lib/formatters'
import type { ResultadoCompra } from '@/context/CarritoContext'
import { Button } from '@/components/Button/Button'
import styles from './CarritoMenu.module.css'

/** Botón con contador que abre el carrito y permite confirmar la compra. */
export function CarritoMenu() {
  const { items, cantidadTotal, total, cambiarCantidad, quitar, vaciar, confirmar } = useCarrito()
  const [abierto, setAbierto] = useState(false)
  const [confirmando, setConfirmando] = useState(false)
  const [resultado, setResultado] = useState<ResultadoCompra | null>(null)
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

  const cerrar = () => {
    setAbierto(false)
    setResultado(null)
  }

  const onConfirmar = async () => {
    setConfirmando(true)
    setResultado(null)

    try {
      setResultado(await confirmar())
    } finally {
      setConfirmando(false)
    }
  }

  return (
    <div className={styles.contenedor} ref={contenedorRef}>
      <Button
        variante="secundario"
        tamano="sm"
        onClick={() => (abierto ? cerrar() : setAbierto(true))}
        aria-expanded={abierto}
        aria-haspopup="true"
      >
        Carrito
        {cantidadTotal > 0 && <span className={styles.contador}>{cantidadTotal}</span>}
      </Button>

      {abierto && (
        <div className={styles.desplegable} role="menu">
          <p className={styles.titulo}>Productos agregados</p>

          {resultado && (
            <p className={resultado.ok ? styles.exito : styles.error}>{resultado.mensaje}</p>
          )}

          {items.length === 0 ? (
            <p className={styles.vacio}>Todavía no agregaste nada.</p>
          ) : (
            <ul className={styles.lista}>
              {items.map((item) => (
                <li key={item.producto.id} className={styles.item}>
                  <div className={styles.itemInfo}>
                    <p className={styles.itemNombre}>{item.producto.nombre}</p>
                    <p className={styles.itemPrecio}>
                      {formatMoneda(item.producto.precio)} c/u
                    </p>
                  </div>

                  <div className={styles.controles}>
                    <button
                      type="button"
                      className={styles.paso}
                      onClick={() => cambiarCantidad(item.producto.id, item.cantidad - 1)}
                      aria-label={`Quitar una unidad de ${item.producto.nombre}`}
                    >
                      −
                    </button>
                    <span className={styles.cantidad}>{item.cantidad}</span>
                    <button
                      type="button"
                      className={styles.paso}
                      onClick={() => cambiarCantidad(item.producto.id, item.cantidad + 1)}
                      disabled={item.cantidad >= item.producto.stock}
                      aria-label={`Agregar una unidad de ${item.producto.nombre}`}
                    >
                      +
                    </button>
                  </div>

                  <p className={styles.subtotal}>
                    {formatMoneda(item.producto.precio * item.cantidad)}
                  </p>

                  <button
                    type="button"
                    className={styles.quitar}
                    onClick={() => quitar(item.producto.id)}
                  >
                    Quitar
                  </button>
                </li>
              ))}
            </ul>
          )}

          {items.length > 0 && (
            <div className={styles.pie}>
              <div className={styles.totalFila}>
                <span>Total</span>
                <span>{formatMoneda(total)}</span>
              </div>

              <Button
                variante="primario"
                tamano="sm"
                cargando={confirmando}
                onClick={onConfirmar}
                className={styles.confirmar}
              >
                Confirmar compra
              </Button>

              <button type="button" className={styles.vaciar} onClick={vaciar}>
                Vaciar
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}