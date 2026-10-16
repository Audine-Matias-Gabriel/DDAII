export type Categoria =
  | 'REMPERA'
  | 'PANTALON'
  | 'BUZO'
  | 'CAMPERA'
  | 'ZAPATILLA'
  | 'ZAPATO'
  | 'ACCESORIO'

export type EstadoProducto = 'NUEVO' | 'USADO'

export type Genero = 'HOMBRE' | 'MUJER' | 'UNISEX' | 'INFANTIL'

/**
 * Stock por variante: cada género apunta a un mapa talle -> unidades.
 * Los talles son strings libres (el backend no tiene enum de talles)
 * y salen de las claves de este mapa.
 */
export type StockPorVariante = Record<Genero, Record<string, number>>

export interface Producto {
  id: string
  nombre: string
  descripcion: string
  precio: number
  stock: StockPorVariante
  categoria: Categoria
  genero?: Genero
  estado: EstadoProducto
  imagenUrl?: string
  tiendaId: string
  creadoEn: string
}
