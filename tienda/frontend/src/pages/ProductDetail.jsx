import { useState, useEffect } from 'react'
import { api } from '../api'
import { useFavorites } from '../context/FavoritesContext'

export default function ProductDetail({ id, navigate }) {
  const [producto, setProducto] = useState(null)
  const [recomendaciones, setRecomendaciones] = useState([])
  const [similares, setSimilares] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')
  const { toggleFavorite, isFavorite } = useFavorites()

  useEffect(() => {
    cargarProducto()
    cargarRecomendaciones()
    cargarSimilares()
  }, [id])

  async function cargarProducto() {
    try {
      const p = await api.getProducto(id)
      setProducto(p)
    } catch (e) {
      setMsg('No se pudo cargar el detalle del producto.')
    } finally {
      setLoading(false)
    }
  }

  async function cargarRecomendaciones() {
    try {
      const data = await api.getRecomendaciones(id, 4)
      setRecomendaciones(data || [])
    } catch (e) {
      // silencioso
    }
  }

  async function cargarSimilares() {
    try {
      const data = await api.getSimilares(id, 4)
      setSimilares(data || [])
    } catch (e) {
      // silencioso
    }
  }

  if (loading) return <p>Cargando detalle...</p>
  if (!producto) return <div className="mensaje err">{msg}</div>

  const fav = isFavorite(producto.id)

  return (
    <section className="page detail">
      <button className="btn btn-outline" onClick={() => navigate('/catalogo')}>
        ← Volver al catálogo
      </button>

      <div className="detail-grid">
        <div className="detail-image">
          {producto.imagenUrl ? (
            <img src={producto.imagenUrl} alt={producto.nombre} />
          ) : (
            <div className="placeholder-img">👕</div>
          )}
        </div>

        <div className="detail-info">
          <h1>{producto.nombre}</h1>
          <p className="price">${producto.precio}</p>
          <p className="stock">
            {producto.disponible ? 'Disponible' : 'Sin stock'}
          </p>
          <p className="talles">
            Talles: {(producto.tallesDisponibles || []).join(', ')}
          </p>
          <p>{producto.descripcion || ''}</p>
          <p className="marca">
            Marca: {producto.marca || '-'} · Tienda: {producto.tienda?.nombre || '-'}
          </p>
          {producto.urlOriginal && (
            <a
              href={producto.urlOriginal}
              target="_blank"
              rel="noopener noreferrer"
              className="btn btn-primary"
            >
              Ver en tienda oficial
            </a>
          )}
          <button
            className={`fav-btn ${fav ? 'active' : ''} mt-2`}
            onClick={() => toggleFavorite(producto)}
            title={fav ? 'Quitar de favoritos' : 'Agregar a favoritos'}
          >
            {fav ? '♥ Quitar favorito' : '♡ Agregar a favoritos'}
          </button>
        </div>
      </div>

      <section className="recomendaciones">
        <h2>Te puede gustar</h2>
        {recomendaciones.length > 0 ? (
          <div className="grid-small">
            {recomendaciones.map((p) => (
              <ProductCard key={p.id} producto={p} navigate={navigate} />
            ))}
          </div>
        ) : (
          <p className="mensaje">Sin recomendaciones.</p>
        )}
      </section>

      <section className="similares">
        <h2>Prendas similares en otras tiendas</h2>
        {similares.length > 0 ? (
          <div className="grid-small">
            {similares.map((p) => (
              <ProductCard key={p.id} producto={p} navigate={navigate} />
            ))}
          </div>
        ) : (
          <p className="mensaje">Sin similares en otras tiendas.</p>
        )}
      </section>
    </section>
  )
}
