import { createContext, useCallback, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import type { Producto } from '@/types/Producto'

export type CarritoContextValue = {
  productos: Producto[]
  agregar: (producto: Producto) => void
  vaciar: () => void
}

export const CarritoContext = createContext<CarritoContextValue | null>(null)

// Estado en memoria a propósito: es un prototipo de demostración.
export function CarritoProvider({ children }: { children: ReactNode }) {
  const [productos, setProductos] = useState<Producto[]>([])

  const agregar = useCallback((producto: Producto) => {
    setProductos((prev) => (prev.some((p) => p.id === producto.id) ? prev : [...prev, producto]))
  }, [])

  const vaciar = useCallback(() => setProductos([]), [])

  const valor = useMemo(() => ({ productos, agregar, vaciar }), [productos, agregar, vaciar])

  return <CarritoContext value={valor}>{children}</CarritoContext>
}
