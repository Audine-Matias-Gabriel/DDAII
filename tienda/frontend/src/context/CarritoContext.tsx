import { createContext, useCallback, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { crearPedido } from '@/services/productoService'
import { formatMoneda } from '@/lib/formatters'
import type { Producto } from '@/types/Producto'

export type ItemCarrito = {
  producto: Producto
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
  agregar: (producto: Producto) => void
  cambiarCantidad: (id: string, cantidad: number) => void
  quitar: (id: string) => void
  vaciar: () => void
  confirmar: () => Promise<ResultadoCompra>
}

export const CarritoContext = createContext<CarritoContextValue | null>(null)

// Estado en memoria a propósito: es un prototipo de demostración.
// La cantidad nunca supera el stock que trajo el producto del catálogo.
export function CarritoProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ItemCarrito[]>([])

  const agregar = useCallback((producto: Producto) => {
    setItems((prev) => {
      const existente = prev.find((item) => item.producto.id === producto.id)

      if (!existente) {
        return producto.stock > 0 ? [...prev, { producto, cantidad: 1 }] : prev
      }

      return prev.map((item) =>
        item.producto.id === producto.id
          ? { ...item, cantidad: Math.min(item.cantidad + 1, producto.stock) }
          : item,
      )
    })
  }, [])

  /** cantidad <= 0 quita la fila; si no, se limita al stock disponible. */
  const cambiarCantidad = useCallback((id: string, cantidad: number) => {
    setItems((prev) =>
      prev.flatMap((item) => {
        if (item.producto.id !== id) {
          return [item]
        }
        if (cantidad <= 0) {
          return []
        }
        return [{ ...item, cantidad: Math.min(cantidad, item.producto.stock) }]
      }),
    )
  }, [])

  const quitar = useCallback((id: string) => {
    setItems((prev) => prev.filter((item) => item.producto.id !== id))
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
   * POST /api/pedidos: el backend valida el stock, calcula los totales y
   * descuenta inventario. Solo se manda productoId y cantidad.
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