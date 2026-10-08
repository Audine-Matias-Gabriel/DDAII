import tiendasJson from './tiendas.json'
import type { Tienda } from '@/types/Tienda'

/**
 * El catálogo vive en el backend (GET /api/productos), pero no hay API de
 * tiendas, así que este archivo queda estático.
 * Los ids coinciden con el tiendaId numérico que devuelve el backend.
 */
export const TIENDAS = tiendasJson as Tienda[]

export function obtenerTienda(id: string): Tienda | undefined {
  return TIENDAS.find((t) => t.id === id)
}