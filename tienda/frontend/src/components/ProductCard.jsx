import { useFavorites } from '../context/FavoritesContext'

export default function ProductCard({ producto, navigate }) {
  const { isFavorite, toggleFavorite } = useFavorites()
  const fav = isFavorite(producto.id)

  return (
    <article className="card">
      <div className="card-image">
        {producto.imagenUrl ? (
          <img src={producto.imagenUrl} alt={producto.nombre} loading="lazy" />
        ) : (
          <div className="placeholder-img">👕</div>
        )}
        <button
          className={`fav-btn ${fav ? 'active' : ''}`}
          onClick={(e) => {
            e.stopPropagation()
            toggleFavorite(producto)
          }}
          title={fav ? 'Quitar de favoritos' : 'Agregar a favoritos'}
        >
          {fav ? '♥' : '♡'}
        </button>
      </div>
      <div className="card-body">
        <h3>{producto.nombre}</h3>
        <p className="price">${producto.precio}</p>
        <p className="stock">
          {producto.disponible ? 'Disponible' : 'Sin stock'}
          {' · '}
          Talles: {(producto.tallesDisponibles || []).join(', ')}
        </p>
        <p className="desc">{producto.descripcion || ''}</p>
        <button className="btn btn-outline" onClick={() => navigate(`/producto/${producto.id}`)}>
          Ver detalle
        </button>
      </div>
    </article>
  )
}
