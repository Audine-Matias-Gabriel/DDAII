import { Link } from 'react-router-dom'
import { formatMoneda } from '@/lib/formatters'
import { stockTotal } from '@/lib/variantes'
import { obtenerTienda } from '@/data'
import { useVariante } from '@/hooks/useVariante'
import type { Genero, Producto } from '@/types/Producto'
import { Badge } from '@/components/Badge/Badge'
import { Button } from '@/components/Button/Button'
import { ImagenProducto } from '@/components/ImagenProducto/ImagenProducto'
import { SelectorVariante } from '@/components/SelectorVariante/SelectorVariante'
import styles from './ProductoCard.module.css'

export type ProductoCardProps = {
  producto: Producto
  onAgregar: (producto: Producto, genero: Genero, talle: string) => void
}

export function ProductoCard({ producto, onAgregar }: ProductoCardProps) {
  const tienda = obtenerTienda(producto.tiendaId)
  const { generos, talles, genero, talle, setGenero, setTalle, listo } =
    useVariante(producto)
  const sinStock = stockTotal(producto) === 0

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

        <SelectorVariante
          producto={producto}
          generos={generos}
          talles={talles}
          genero={genero}
          talle={talle}
          onGenero={setGenero}
          onTalle={setTalle}
          compacto
        />

        <Button
          variante="primario"
          tamano="sm"
          disabled={!listo}
          onClick={() => onAgregar(producto, genero as Genero, talle)}
        >
          {sinStock ? 'Sin stock' : listo ? 'Agregar' : 'Elegí talle'}
        </Button>
      </div>
    </article>
  )
}
