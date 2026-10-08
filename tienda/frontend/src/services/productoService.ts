import { apiGet, apiPost } from './api'
import type { Pedido } from '@/types/Pedido'
import type { Producto } from '@/types/Producto'

/**
 * Forma que devuelve el backend: id y tiendaId numéricos, y nulls explícitos
 * (Jackson serializa los nulls, así que imagenUrl viene como null y no undefined).
 */
type ProductoCrudo = {
  id: number
  nombre: string
  descripcion: string
  precio: number
  stock: number
  categoria: Producto['categoria']
  genero: Producto['genero']
  estado: Producto['estado']
  talles: string[] | null
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
    stock: crudo.stock,
    categoria: crudo.categoria,
    genero: crudo.genero,
    estado: crudo.estado,
    talles: crudo.talles ?? [],
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
}

/**
 * POST /api/pedidos
 *
 * Solo se mandan productoId y cantidad: el precio lo resuelve el backend desde
 * la base, así el cliente no puede manipularlo. El productoId viaja como número
 * porque en el backend es un Long.
 */
export async function crearPedido(detalles: ItemPedido[]): Promise<Pedido> {
  return apiPost<Pedido>('/api/pedidos', {
    detalles: detalles.map((detalle) => ({
      productoId: Number(detalle.productoId),
      cantidad: detalle.cantidad,
    })),
  })
}