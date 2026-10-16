import { apiGet, apiPost } from './api'
import type { Pedido } from '@/types/Pedido'
import type { Genero, Producto, StockPorVariante } from '@/types/Producto'

/**
 * Forma que devuelve el backend: id y tiendaId numéricos, nulls explícitos
 * (Jackson serializa los nulls, así que imagenUrl viene como null y no
 * undefined) y el stock como mapa género -> talle -> unidades.
 */
type ProductoCrudo = {
  id: number
  nombre: string
  descripcion: string
  precio: number
  stock: Record<string, Record<string, number>>
  categoria: Producto['categoria']
  genero: Producto['genero']
  estado: Producto['estado']
  tiendaId: number
  imagenUrl: string | null
  creadoEn: string
}

/** Normaliza la respuesta cruda al tipo que usa la UI. */
function aProducto(crudo: ProductoCrudo): Producto {
  return {
    id: String(crudo.id),
    nombre: crudo.nombre,
    descripcion: crudo.descripcion,
    precio: crudo.precio,
    stock: (crudo.stock ?? {}) as StockPorVariante,
    categoria: crudo.categoria,
    genero: crudo.genero,
    estado: crudo.estado,
    imagenUrl: crudo.imagenUrl ?? undefined,
    tiendaId: String(crudo.tiendaId),
    creadoEn: crudo.creadoEn,
  }
}

export async function listarProductos(): Promise<Producto[]> {
  const crudos = await apiGet<ProductoCrudo[]>('/api/productos')
  return crudos.map(aProducto)
}

export async function obtenerProducto(id: string): Promise<Producto> {
  return aProducto(await apiGet<ProductoCrudo>(`/api/productos/${id}`))
}

export type ItemPedido = {
  productoId: string
  cantidad: number
  genero: Genero
  talle: string
}

/**
 * POST /api/pedidos
 *
 * Solo se mandan productoId, cantidad y la variante (género/talle): el precio
 * lo resuelve el backend desde la base, así el cliente no puede manipularlo.
 * El productoId viaja como número porque en el backend es un Long.
 */
export async function crearPedido(detalles: ItemPedido[]): Promise<Pedido> {
  return apiPost<Pedido>('/api/pedidos', {
    detalles: detalles.map((detalle) => ({
      productoId: Number(detalle.productoId),
      cantidad: detalle.cantidad,
      genero: detalle.genero,
      talle: detalle.talle,
    })),
  })
}
