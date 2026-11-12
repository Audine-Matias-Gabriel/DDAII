import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { api } from '../api'

const FavoritesContext = createContext(null)

export function FavoritesProvider({ children }) {
  const [favorites, setFavorites] = useState([]) // array de IDs para invitados
  const [favoriteProducts, setFavoriteProducts] = useState([]) // objetos producto para usuarios registrados
  const [loading, setLoading] = useState(false)

  const addFavorite = useCallback(async (producto) => {
    setFavorites((prev) => [...prev, producto.id])
    setFavoriteProducts((prev) => [...prev, producto])
  }, [])

  const removeFavorite = useCallback((productoId) => {
    setFavorites((prev) => prev.filter((id) => id !== productoId))
    setFavoriteProducts((prev) => prev.filter((p) => p.id !== productoId))
  }, [])

  const isFavorite = useCallback((productoId) => {
    return favorites.some((id) => id === productoId)
  }, [favorites])

  const toggleFavorite = useCallback(async (producto) => {
    if (isFavorite(producto.id)) {
      removeFavorite(producto.id)
    } else {
      addFavorite(producto)
    }
  }, [isFavorite, addFavorite, removeFavorite])

  return (
    <FavoritesContext.Provider
      value={{
        favorites,
        favoriteProducts,
        loading,
        addFavorite,
        removeFavorite,
        isFavorite,
        toggleFavorite
      }}
    >
      {children}
    </FavoritesContext.Provider>
  )
}

export function useFavorites() {
  const ctx = useContext(FavoritesContext)
  if (!ctx) throw new Error('useFavorites debe usarse dentro de FavoritesProvider')
  return ctx
}
