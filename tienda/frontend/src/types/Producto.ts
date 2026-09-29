export type Categoria =
  | 'REMPERA'
  | 'PANTALON'
  | 'BUZO'
  | 'CAMPERA'
  | 'ZAPATILLA'
  | 'ZAPATO'
  | 'ACCESORIO'

export const CATEGORIAS: readonly Categoria[] = [
  'REMPERA',
  'PANTALON',
  'BUZO',
  'CAMPERA',
  'ZAPATILLA',
  'ZAPATO',
  'ACCESORIO',
]

export type EstadoProducto = 'NUEVO' | 'USADO'

export type Talle = 'XS' | 'S' | 'M' | 'L' | 'XL' | 'XXL' | 'UNICO'

export const TALLES: readonly Talle[] = ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'UNICO']

export interface Producto {
  id: string
  nombre: string
  descripcion: string
  precio: number
  stock: number
  categoria: Categoria
  estado: EstadoProducto
  talles: Talle[]
  imagenUrl?: string
  tiendaId: string
  creadoEn: string
}

export type ProductoEntrada = Omit<Producto, 'id' | 'creadoEn'>
