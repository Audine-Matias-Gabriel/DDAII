import { useNavigate, useParams } from 'react-router-dom'
import { obtenerTienda } from '@/data'
import { useCarrito } from '@/hooks/useCarrito'
import { useProducto } from '@/hooks/useProducto'
import { useVariante } from '@/hooks/useVariante'
import { formatFecha, formatMoneda } from '@/lib/formatters'
import { stockTotal } from '@/lib/variantes'
import type { Genero } from '@/types/Producto'
import { Badge } from '@/components/Badge/Badge'
import { Button } from '@/components/Button/Button'
import { EmptyState } from '@/components/EmptyState/EmptyState'
import { ImagenProducto } from '@/components/ImagenProducto/ImagenProducto'
import { SelectorVariante } from '@/components/SelectorVariante/SelectorVariante'
import styles from './ProductoDetallePage.module.css'

export function ProductoDetallePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { agregar } = useCarrito()
  const { producto, cargando, error, recargar } = useProducto(id)
  const { generos, talles, genero, talle, setGenero, setTalle, stock, listo } =
    useVariante(producto ?? null)

  const volver = () => navigate('/')

  if (cargando) {
    return <p className={styles.estado}>Cargando producto...</p>
  }

  if (error?.status === 404) {
    return (
      <EmptyState
        titulo="Producto no encontrado"
        descripcion="El artículo que buscás no existe."
        accion={
          <Button variante="secundario" onClick={volver}>
            Volver al catálogo
          </Button>
        }
      />
    )
  }

  if (error || !producto) {
    return (
      <EmptyState
        titulo="No se pudo cargar el producto"
        descripcion={error?.message ?? 'Error inesperado.'}
        accion={
          <Button variante="secundario" onClick={recargar}>
            Reintentar
          </Button>
        }
      />
    )
  }

  const tienda = obtenerTienda(producto.tiendaId)
  const disponible = stockTotal(producto)

  return (
    <div className={styles.pagina}>
      <Button variante="secundario" tamano="sm" onClick={volver}>
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
            <Badge tono={disponible > 0 ? 'exito' : 'error'}>
              {disponible > 0 ? `${disponible} en stock` : 'Sin stock'}
            </Badge>
          </div>

          <SelectorVariante
            producto={producto}
            generos={generos}
            talles={talles}
            genero={genero}
            talle={talle}
            onGenero={setGenero}
            onTalle={setTalle}
          />

          <p className={styles.meta}>
            {talle
              ? stock > 0
                ? `${stock} unidades en talle ${talle}`
                : `Sin stock en talle ${talle}`
              : 'Elegí un talle para ver la disponibilidad'}
          </p>

          <p className={styles.descripcion}>{producto.descripcion}</p>

          <p className={styles.meta}>Publicado el {formatFecha(producto.creadoEn)}</p>

          <Button
            disabled={!listo}
            onClick={() => agregar(producto, genero as Genero, talle)}
          >
            Agregar al carrito
          </Button>
        </div>
      </article>
    </div>
  )
}