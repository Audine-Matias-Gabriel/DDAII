import productosJson from './productos.json'
import tiendasJson from './tiendas.json'
import type { Producto } from '@/types/Producto'
import type { Tienda } from '@/types/Tienda'

/**
 * Datos del catálogo mientras no exista el backend.
 * Cuando esté listo, este archivo es el único que hay que cambiar:
 * estas dos constantes pasan a ser el resultado de un fetch.
 */
export const PRODUCTOS = productosJson as Producto[]
export const TIENDAS = tiendasJson as Tienda[]

export function obtenerTienda(id: string): Tienda | undefined {
  return TIENDAS.find((t) => t.id === id)
}
