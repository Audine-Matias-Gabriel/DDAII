import { useNavigate, useParams } from 'react-router-dom'
import { PRODUCTOS, obtenerTienda } from '@/data'
import { useCarrito } from '@/hooks/useCarrito'
import { formatFecha, formatMoneda } from '@/lib/formatters'
import { Badge } from '@/components/Badge/Badge'
import { Button } from '@/components/Button/Button'
import { EmptyState } from '@/components/EmptyState/EmptyState'
import { ImagenProducto } from '@/components/ImagenProducto/ImagenProducto'
import styles from './ProductoDetallePage.module.css'

export function ProductoDetallePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { agregar } = useCarrito()

  const producto = PRODUCTOS.find((p) => p.id === id)

  if (!producto) {
    return (
      <EmptyState
        titulo="Producto no encontrado"
        descripcion="El artículo que buscás no existe."
        accion={
          <Button variante="secundario" onClick={() => navigate('/')}>
            Volver al catálogo
          </Button>
        }
      />
    )
  }

  const tienda = obtenerTienda(producto.tiendaId)

  return (
    <div className={styles.pagina}>
      <Button variante="secundario" tamano="sm" onClick={() => navigate('/')}>
        Volver
      </Button>

      <article className={styles.detalle}>
        <ImagenProducto
          nombre={producto.nombre}
          imagenUrl={producto.imagenUrl}
          className={styles.imagen}
        />

        <div className={styles.info}>
          {tienda && <p className={styles.tienda}>{tienda.nombre}</p>}

          <h1 className={styles.nombre}>{producto.nombre}</h1>

          <p className={styles.precio}>{formatMoneda(producto.precio)}</p>

          <div className={styles.badges}>
            <Badge tono="primario">{producto.categoria}</Badge>
            {producto.estado === 'USADO' && <Badge tono="neutro">Usado</Badge>}
            <Badge tono={producto.stock > 0 ? 'exito' : 'error'}>
              {producto.stock > 0 ? `${producto.stock} en stock` : 'Sin stock'}
            </Badge>
          </div>

          {producto.talles.length > 0 && (
            <p className={styles.meta}>Talles: {producto.talles.join(' · ')}</p>
          )}

          <p className={styles.descripcion}>{producto.descripcion}</p>

          <p className={styles.meta}>Publicado el {formatFecha(producto.creadoEn)}</p>

          <Button disabled={producto.stock === 0} onClick={() => agregar(producto)}>
            Agregar al carrito
          </Button>
        </div>
      </article>
    </div>
  )
}
