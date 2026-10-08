export type EstadoPedido =
  | 'PENDIENTE'
  | 'PAGADO'
  | 'ENVIADO'
  | 'ENTREGADO'
  | 'CANCELADO'

export type DetallePedido = {
  id: number
  productoId: number
  cantidad: number
  precioUnitario: number
  subtotal: number
}

/** Respuesta de POST /api/pedidos: el backend ya calculó todos los totales. */
export type Pedido = {
  id: number
  clienteId: number | null
  tiendaId: number | null
  fecha: string
  estado: EstadoPedido
  total: number
  detalles: DetallePedido[]
}