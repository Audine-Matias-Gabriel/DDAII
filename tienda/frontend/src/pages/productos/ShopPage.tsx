import { useState } from 'react'
import { useCarrito } from '@/hooks/useCarrito'
import { useProductos } from '@/hooks/useProductos'
import { Button } from '@/components/Button/Button'
import { EmptyState } from '@/components/EmptyState/EmptyState'
import { Input } from '@/components/Input/Input'
import { ProductoCard } from '@/components/ProductoCard/ProductoCard'
import styles from './ShopPage.module.css'

export function ShopPage() {
  const { agregar } = useCarrito()
  const { productos, cargando, error, recargar } = useProductos()
  const [busqueda, setBusqueda] = useState('')

  const criterio = busqueda.trim().toLowerCase()
  const filtrados = productos.filter((p) => p.nombre.toLowerCase().includes(criterio))

  return (
    <div className={styles.pagina}>
      <div className={styles.encabezado}>
        <h1 className={styles.titulo}>Catálogo</h1>
      </div>

      <div className={styles.buscador}>
        <Input
          label="Buscar"
          type="search"
          placeholder="Buscar por nombre"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
        />
      </div>

      {cargando && <p className={styles.estado}>Cargando catálogo...</p>}

      {!cargando && error && (
        <EmptyState
          titulo="No se pudo cargar el catálogo"
          descripcion={error}
          accion={
            <Button variante="secundario" onClick={recargar}>
              Reintentar
            </Button>
          }
        />
      )}

      {!cargando && !error && filtrados.length === 0 && (
        <EmptyState
          titulo="Sin resultados"
          descripcion={
            criterio
              ? `No hay productos que coincidan con "${busqueda.trim()}".`
              : 'Todavía no hay productos en el catálogo.'
          }
        />
      )}

      {!cargando && !error && filtrados.length > 0 && (
        <div className={styles.grilla}>
          {filtrados.map((producto) => (
            <ProductoCard key={producto.id} producto={producto} onAgregar={agregar} />
          ))}
        </div>
      )}
    </div>
  )
}