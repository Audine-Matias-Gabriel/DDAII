export type Categoria =
  | 'REMPERA'
  | 'PANTALON'
  | 'BUZO'
  | 'CAMPERA'
  | 'ZAPATILLA'
  | 'ZAPATO'
  | 'ACCESORIO'

export type EstadoProducto = 'NUEVO' | 'USADO'

/** El backend no tiene enum de talles: los persiste como strings libres. */
export type Genero = 'HOMBRE' | 'MUJER' | 'UNISEX' | 'INFANTIL'

export interface Producto {
  id: string
  nombre: string
  descripcion: string
  precio: number
  stock: number
  categoria: Categoria
  genero?: Genero
  estado: EstadoProducto
  talles: string[]
  imagenUrl?: string
  tiendaId: string
  creadoEn: string
}