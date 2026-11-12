import { useState, useEffect } from 'react'
import { useFavorites } from '../context/FavoritesContext'
import { useAuth } from '../context/AuthContext'
import ProductCard from '../components/ProductCard'

export default function Favorites({ navigate }) {
  const { isAuthenticated, user } = useAuth()
  const { favorites, favoriteProducts, loading, removeFavorite } = useFavorites()

  const items = isAuthenticated ? favoriteProducts : favorites.map((id) => ({ id }))

  return (
    <section className="page favorites-page">
      <h1>Mis favoritos</h1>

      {!isAuthenticated && (
        <div className="mensaje err">
          Estás navegando como <strong>invitado</strong>. Tus favoritos se guardan en
          memoria local y <strong>se pierden al recargar la página</strong>.
          {' '}
          Iniciá sesión para guardarlos en tu cuenta.
        </div>
      )}

      {loading ? (
        <p>Cargando favoritos...</p>
      ) : items.length === 0 ? (
        <p className="mensaje">No tenés productos favoritos.</p>
      ) : (
        <div className="grid">
          {items.map((p) => (
            <ProductCard
              key={p.id}
              producto={p}
              navigate={navigate}
            />
          ))}
        </div>
      )}
    </section>
  )
}
