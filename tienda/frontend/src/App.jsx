import { useState, useEffect } from 'react'
import Navbar from './components/Navbar'
import Home from './pages/Home'
import ProductDetail from './pages/ProductDetail'
import Login from './pages/Login'
import Register from './pages/Register'
import Plans from './pages/Plans'
import Favorites from './pages/Favorites'
import NotFound from './pages/NotFound'
import { AuthProvider } from './context/AuthContext'
import { FavoritesProvider } from './context/FavoritesContext'

export default function App() {
  const [route, setRoute] = useState(getHashRoute())

  useEffect(() => {
    const onHashChange = () => setRoute(getHashRoute())
    window.addEventListener('hashchange', onHashChange)
    return () => window.removeEventListener('hashchange', onHashChange)
  }, [])

  function getHashRoute() {
    const hash = window.location.hash.replace('#/', '')
    return hash || '/'
  }

  function navigate(path) {
    window.location.hash = path
  }

  // Extraer id de producto de rutas tipo /producto/123
  const productIdMatch = route.match(/^\/producto\/(.+)$/)
  const productId = productIdMatch ? productIdMatch[1] : null

  return (
    <AuthProvider>
      <FavoritesProvider>
        <Navbar route={route} navigate={navigate} />
        <main className="container py-4">
          {route === '/' || route === '/catalogo' ? (
            <Home navigate={navigate} />
          ) : productId ? (
            <ProductDetail id={productId} navigate={navigate} />
          ) : route === '/login' ? (
            <Login navigate={navigate} />
          ) : route === '/registro' ? (
            <Register navigate={navigate} />
          ) : route === '/planes' ? (
            <Plans navigate={navigate} />
          ) : route === '/favoritos' ? (
            <Favorites navigate={navigate} />
          ) : (
            <NotFound />
          )}
        </main>
      </FavoritesProvider>
    </AuthProvider>
  )
}
