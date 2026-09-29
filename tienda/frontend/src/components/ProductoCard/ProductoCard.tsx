import { Link } from 'react-router-dom'
import { formatMoneda } from '@/lib/formatters'
import { obtenerTienda } from '@/data'
import type { Producto } from '@/types/Producto'
import { Badge } from '@/components/Badge/Badge'
import { Button } from '@/components/Button/Button'
import { ImagenProducto } from '@/components/ImagenProducto/ImagenProducto'
import styles from './ProductoCard.module.css'

export type ProductoCardProps = {
  producto: Producto
  onAgregar: (producto: Producto) => void
}

export function ProductoCard({ producto, onAgregar }: ProductoCardProps) {
  const tienda = obtenerTienda(producto.tiendaId)

  return (
    <article className={styles.card}>
      <Link to={`/productos/${producto.id}`} className={styles.enlaceImagen}>
        <ImagenProducto nombre={producto.nombre} imagenUrl={producto.imagenUrl} />
      </Link>

      <div className={styles.cuerpo}>
        <h3 className={styles.nombre}>
          <Link to={`/productos/${producto.id}`}>{producto.nombre}</Link>
        </h3>

        {tienda && <p className={styles.tienda}>{tienda.nombre}</p>}

        <p className={styles.precio}>{formatMoneda(producto.precio)}</p>

        {producto.estado === 'USADO' && <Badge tono="neutro">Usado</Badge>}

        <Button
          variante="primario"
          tamano="sm"
          disabled={producto.stock === 0}
          onClick={() => onAgregar(producto)}
        >
          {producto.stock === 0 ? 'Sin stock' : 'Agregar'}
        </Button>
      </div>
    </article>
  )
}
