import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { api } from '../api'
import { useAuth } from './AuthContext'

const FavoritesContext = createContext(null)

// El backend devuelve FavoritoDTO (productoId), mientras las tarjetas usan `id`.
function productoDeFavorito(favorito) {
  return {
    id: favorito.productoId,
    nombre: favorito.nombre,
    precio: favorito.precio,
    imagenUrl: favorito.imagenUrl,
    disponible: favorito.disponible
  }
}

export function FavoritesProvider({ children }) {
  const { isAuthenticated } = useAuth()
  const [favorites, setFavorites] = useState([]) // IDs de la sesión actual
  const [favoriteProducts, setFavoriteProducts] = useState([]) // productos de usuarios registrados
  const [loading, setLoading] = useState(
    () => typeof localStorage !== 'undefined' && !!localStorage.getItem('token')
  )

  useEffect(() => {
    let vigente = true
    const limpiar = () => { vigente = false }

    if (!isAuthenticated) {
      // Invitado: favoritos en memoria. También evita mostrar los favoritos
      // del usuario anterior después de cerrar sesión.
      setFavorites([])
      setFavoriteProducts([])
      setLoading(false)
      return limpiar
    }

    setLoading(true)
    api.getFavoritos()
      .then((data) => {
        if (!vigente) return
        const productos = (Array.isArray(data) ? data : []).map(productoDeFavorito)
        setFavoriteProducts(productos)
        setFavorites(productos.map((p) => p.id))
      })
      .catch(() => {
        if (!vigente) return
        setFavoriteProducts([])
        setFavorites([])
      })
      .finally(() => {
        if (vigente) setLoading(false)
      })

    return limpiar
  }, [isAuthenticated])

  const addFavorite = useCallback(async (producto) => {
    if (isAuthenticated) {
      try {
        await api.addFavorito(producto.id)
      } catch (e) {
        // Si el backend falla, se mantiene la marca en la sesión actual.
      }
    }
    setFavorites((prev) => prev.includes(producto.id) ? prev : [...prev, producto.id])
    setFavoriteProducts((prev) =>
      prev.some((p) => p.id === producto.id) ? prev : [...prev, producto]
    )
  }, [isAuthenticated])

  const removeFavorite = useCallback(async (productoId) => {
    if (isAuthenticated) {
      try {
        await api.removeFavorito(productoId)
      } catch (e) {
        // Puede no existir en el backend; de todas formas se quita de la vista.
      }
    }
    setFavorites((prev) => prev.filter((id) => id !== productoId))
    setFavoriteProducts((prev) => prev.filter((p) => p.id !== productoId))
  }, [isAuthenticated])

  const isFavorite = useCallback((productoId) => {
    return favorites.some((id) => id === productoId)
  }, [favorites])

  const toggleFavorite = useCallback(async (producto) => {
    if (isFavorite(producto.id)) {
      await removeFavorite(producto.id)
    } else {
      await addFavorite(producto)
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
