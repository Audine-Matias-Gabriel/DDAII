import { useCallback, useEffect, useState } from 'react'
import { obtenerProducto } from '@/services/productoService'
import { ApiError } from '@/services/api'
import type { Producto } from '@/types/Producto'

export type UseProducto = {
  producto: Producto | null
  cargando: boolean
  /** Va tipado como ApiError para que la página distinga 404 de otros fallos. */
  error: ApiError | null
  recargar: () => void
}

type Estado = {
  producto: Producto | null
  cargando: boolean
  error: ApiError | null
}

const INICIAL: Estado = { producto: null, cargando: true, error: null }

function aApiError(e: unknown): ApiError {
  return e instanceof ApiError ? e : new ApiError('Error inesperado', 0)
}

/** Carga un producto por id, volviendo a pedirlo si cambia el parámetro de ruta. */
export function useProducto(id: string | undefined): UseProducto {
  const [estado, setEstado] = useState<Estado>(INICIAL)
  const [intentos, setIntentos] = useState(0)

  useEffect(() => {
    if (!id) {
      return
    }

    let vigente = true

    obtenerProducto(id)
      .then((producto) => {
        if (vigente) {
          setEstado({ producto, cargando: false, error: null })
        }
      })
      .catch((e: unknown) => {
        if (vigente) {
          setEstado({ producto: null, cargando: false, error: aApiError(e) })
        }
      })

    return () => {
      vigente = false
    }
  }, [id, intentos])

  const recargar = useCallback(() => {
    setEstado((prev) => ({ ...prev, cargando: true, error: null }))
    setIntentos((n) => n + 1)
  }, [])

  if (!id) {
    return {
      producto: null,
      cargando: false,
      error: new ApiError('Falta el identificador del producto en la URL.', 400),
      recargar,
    }
  }

  return { ...estado, recargar }
}