import { useState, useEffect } from 'react'
import Navbar from './components/Navbar'
import Home from './pages/Home'
import ProductDetail from './pages/ProductDetail'
import Login from './pages/Login'
import Register from './pages/Register'
import Plans from './pages/Plans'
import Favorites from './pages/Favorites'
import NotFound from './pages/NotFound'
import PagoResultado from './pages/PagoResultado'
import { AuthProvider } from './context/AuthContext'
import { FavoritesProvider } from './context/FavoritesContext'

export default function App() {
  const [route, setRoute] = useState(getHashRoute())
  const [pathname, setPathname] = useState(window.location.pathname)

  useEffect(() => {
    const onHashChange = () => {
      // Si se cambia el hash estando en /pago/..., salir de ese path
      if (isPagoPath(window.location.pathname)) {
        window.history.replaceState(null, '', '/' + window.location.hash)
      }
      setPathname(window.location.pathname)
      setRoute(getHashRoute())
    }
    window.addEventListener('hashchange', onHashChange)
    return () => window.removeEventListener('hashchange', onHashChange)
  }, [])

  function getHashRoute() {
    // '#/planes' -> '/planes' (se conserva la barra inicial)
    const hash = window.location.hash.replace(/^#/, '')
    return hash || '/'
  }

  function isPagoPath(p) {
    return p === '/pago/exito' || p === '/pago/error'
  }

  function navigate(path) {
    // Mercado Pago vuelve a /pago/exito o /pago/error (path, no hash):
    // antes de cambiar el hash hay que volver a la raíz.
    if (window.location.pathname !== '/') {
      window.history.replaceState(null, '', '/' + window.location.hash)
    }
    setPathname('/')
    if (getHashRoute() === path) {
      setRoute(path) // el hash no cambia, así que no hay evento hashchange
    } else {
      window.location.hash = path
    }
  }

  // Extraer id de producto de rutas tipo /producto/123
  const productIdMatch = route.match(/^\/producto\/(.+)$/)
  const productId = productIdMatch ? productIdMatch[1] : null

  // Retorno de Mercado Pago: se decide en el JSX, después de los hooks
  const pagoPath = isPagoPath(pathname) ? pathname : null

  return (
    <AuthProvider>
      <FavoritesProvider>
        <Navbar route={route} navigate={navigate} />
        <main className="container py-4">
          {pagoPath ? (
            <PagoResultado exito={pagoPath === '/pago/exito'} navigate={navigate} />
          ) : route === '/' || route === '/catalogo' ? (
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
