import { useCallback, useEffect, useState } from 'react'
import { listarProductos } from '@/services/productoService'
import type { Producto } from '@/types/Producto'

export type UseProductos = {
  productos: Producto[]
  cargando: boolean
  error: string | null
  recargar: () => void
}

type Estado = {
  productos: Producto[]
  cargando: boolean
  error: string | null
}

const INICIAL: Estado = { productos: [], cargando: true, error: null }

/** Carga el catálogo una vez montado el componente. */
export function useProductos(): UseProductos {
  const [estado, setEstado] = useState<Estado>(INICIAL)
  const [intentos, setIntentos] = useState(0)

  useEffect(() => {
    let vigente = true

    listarProductos()
      .then((productos) => {
        if (vigente) {
          setEstado({ productos, cargando: false, error: null })
        }
      })
      .catch((e: unknown) => {
        if (vigente) {
          setEstado((prev) => ({
            ...prev,
            cargando: false,
            error: e instanceof Error ? e.message : 'Error inesperado',
          }))
        }
      })

    return () => {
      vigente = false
    }
  }, [intentos])

  const recargar = useCallback(() => {
    setEstado((prev) => ({ ...prev, cargando: true, error: null }))
    setIntentos((n) => n + 1)
  }, [])

  return { ...estado, recargar }
}