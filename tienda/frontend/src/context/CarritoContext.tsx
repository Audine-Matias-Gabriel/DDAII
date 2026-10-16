import { createContext, useCallback, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { crearPedido } from '@/services/productoService'
import { formatMoneda } from '@/lib/formatters'
import { claveItem, stockDe } from '@/lib/variantes'
import type { Genero, Producto } from '@/types/Producto'

export type ItemCarrito = {
  producto: Producto
  genero: Genero
  talle: string
  cantidad: number
}

export type ResultadoCompra = {
  ok: boolean
  mensaje: string
}

export type CarritoContextValue = {
  items: ItemCarrito[]
  cantidadTotal: number
  total: number
  agregar: (producto: Producto, genero: Genero, talle: string) => void
  cambiarCantidad: (clave: string, cantidad: number) => void
  quitar: (clave: string) => void
  vaciar: () => void
  confirmar: () => Promise<ResultadoCompra>
}

export const CarritoContext = createContext<CarritoContextValue | null>(null)

// Estado en memoria a propósito: es un prototipo de demostración.
// La cantidad nunca supera el stock de esa variante (género + talle) que trajo
// el catálogo.
export function CarritoProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ItemCarrito[]>([])

  const agregar = useCallback(
    (producto: Producto, genero: Genero, talle: string) => {
      setItems((prev) => {
        const clave = claveItem(producto.id, genero, talle)
        const disponible = stockDe(producto, genero, talle)

        if (disponible <= 0) {
          return prev
        }

        const existente = prev.find(
          (item) => claveItem(item.producto.id, item.genero, item.talle) === clave,
        )

        if (!existente) {
          return [...prev, { producto, genero, talle, cantidad: 1 }]
        }

        return prev.map((item) =>
          claveItem(item.producto.id, item.genero, item.talle) === clave
            ? { ...item, cantidad: Math.min(item.cantidad + 1, disponible) }
            : item,
        )
      })
    },
    [],
  )

  /** cantidad <= 0 quita la fila; si no, se limita al stock de esa variante. */
  const cambiarCantidad = useCallback((clave: string, cantidad: number) => {
    setItems((prev) =>
      prev.flatMap((item) => {
        if (claveItem(item.producto.id, item.genero, item.talle) !== clave) {
          return [item]
        }
        if (cantidad <= 0) {
          return []
        }
        const disponible = stockDe(item.producto, item.genero, item.talle)
        return [{ ...item, cantidad: Math.min(cantidad, disponible) }]
      }),
    )
  }, [])

  const quitar = useCallback((clave: string) => {
    setItems((prev) =>
      prev.filter(
        (item) =>
          claveItem(item.producto.id, item.genero, item.talle) !== clave,
      ),
    )
  }, [])

  const vaciar = useCallback(() => setItems([]), [])

  const cantidadTotal = useMemo(
    () => items.reduce((acc, item) => acc + item.cantidad, 0),
    [items],
  )

  const total = useMemo(
    () => items.reduce((acc, item) => acc + item.producto.precio * item.cantidad, 0),
    [items],
  )

  /**
   * POST /api/pedidos: el backend valida el stock de la variante, calcula los
   * totales y descuenta inventario. Se manda productoId, cantidad, género y
   * talle.
   */
  const confirmar = useCallback(async (): Promise<ResultadoCompra> => {
    if (items.length === 0) {
      return { ok: false, mensaje: 'El carrito está vacío.' }
    }

    try {
      const pedido = await crearPedido(
        items.map((item) => ({
          productoId: item.producto.id,
          cantidad: item.cantidad,
          genero: item.genero,
          talle: item.talle,
        })),
      )

      setItems([])

      return {
        ok: true,
        mensaje: `Pedido #${pedido.id} confirmado por ${formatMoneda(pedido.total)}.`,
      }
    } catch (e) {
      return {
        ok: false,
        mensaje: e instanceof Error ? e.message : 'No se pudo confirmar la compra.',
      }
    }
  }, [items])

  const valor = useMemo(
    () => ({ items, cantidadTotal, total, agregar, cambiarCantidad, quitar, vaciar, confirmar }),
    [items, cantidadTotal, total, agregar, cambiarCantidad, quitar, vaciar, confirmar],
  )

  return <CarritoContext value={valor}>{children}</CarritoContext>
}
