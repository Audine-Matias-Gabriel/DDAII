import type { Genero, Producto, StockPorVariante } from '@/types/Producto'

/** Una variante concreta que se puede agregar al carrito. */
export type Variante = {
  genero: Genero
  talle: string
}

/** Géneros cargados en el stock del producto, en el orden de las claves. */
export function generosDisponibles(producto: Producto): Genero[] {
  return Object.keys(producto.stock) as Genero[]
}

/** Género por defecto: el declarado en el producto si tiene stock, si no el primero. */
export function primerGenero(producto: Producto): Genero | '' {
  const generos = generosDisponibles(producto)
  if (producto.genero && generos.includes(producto.genero)) {
    return producto.genero
  }
  return generos[0] ?? ''
}

/** Talles de un género, en el orden de las claves del mapa. */
export function tallesDe(producto: Producto, genero: Genero | ''): string[] {
  if (!genero) {
    return []
  }
  return Object.keys(producto.stock[genero] ?? {})
}

/** Unidades disponibles para la celda (género, talle). 0 si no existe. */
export function stockDe(
  producto: Producto,
  genero: Genero | '',
  talle: string,
): number {
  if (!genero || !talle) {
    return 0
  }
  return producto.stock[genero]?.[talle] ?? 0
}

/** Suma de todas las celdas; reemplaza al viejo stock plano. */
export function stockTotal(producto: Producto): number {
  return Object.values(producto.stock).reduce(
    (acc, porTalle) => acc + Object.values(porTalle).reduce((a, n) => a + n, 0),
    0,
  )
}

/** Primer talle de un género con unidades disponibles, o '' si no hay. */
export function primerTalleConStock(
  producto: Producto,
  genero: Genero | '',
): string {
  return (
    tallesDe(producto, genero).find(
      (talle) => stockDe(producto, genero, talle) > 0,
    ) ?? ''
  )
}

/** Nombre legible de un género para la UI. */
export function etiquetaGenero(genero: Genero): string {
  const etiquetas: Record<Genero, string> = {
    HOMBRE: 'Hombre',
    MUJER: 'Mujer',
    UNISEX: 'Unisex',
    INFANTIL: 'Infantil',
  }
  return etiquetas[genero]
}

/**
 * Clave estable de una línea del carrito: la misma prenda en distinto talle
 * o género son filas distintas.
 */
export function claveItem(
  productoId: string,
  genero: Genero,
  talle: string,
): string {
  return `${productoId}|${genero}|${talle}`
}

/** Vacía un stock por variante (útil para productos sin datos). */
export const stockVacio: StockPorVariante = {} as StockPorVariante
