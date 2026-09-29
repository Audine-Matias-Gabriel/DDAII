import { Link } from 'react-router-dom'
import { PRODUCTOS } from '@/data'
import { useCarrito } from '@/hooks/useCarrito'
import { Input } from '@/components/Input/Input'
import { ProductoCard } from '@/components/ProductoCard/ProductoCard'
import styles from './ShopPage.module.css'

export function ShopPage() {
  const { agregar } = useCarrito()

  return (
    <div className={styles.pagina}>
      <div className={styles.encabezado}>
        <h1 className={styles.titulo}>Catálogo</h1>
        <Link to="/productos/nuevo" className={styles.publicar}>
          Publicar producto
        </Link>
      </div>

      <div className={styles.buscador}>
        <Input label="Buscar" type="search" placeholder="Buscar por nombre o descripción" />
      </div>

      <div className={styles.grilla}>
        {PRODUCTOS.map((producto) => (
          <ProductoCard key={producto.id} producto={producto} onAgregar={agregar} />
        ))}
      </div>
    </div>
  )
}
