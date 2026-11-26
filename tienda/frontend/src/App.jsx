import { useState, useEffect } from 'react'
import Navbar from './components/Navbar'
import Landing from './pages/Landing'
import Home from './pages/Home'
import ProductDetail from './pages/ProductDetail'
import Login from './pages/Login'
import Register from './pages/Register'
import Plans from './pages/Plans'
import Favorites from './pages/Favorites'
import BusquedaVisual from './pages/BusquedaVisual'
import NotFound from './pages/NotFound'
import PagoResultado from './pages/PagoResultado'
import { AuthProvider } from './context/AuthContext'
import { FavoritesProvider } from './context/FavoritesContext'
import { filtrosDesdeQuery } from './query'

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

  // Al cambiar de página se arranca desde arriba (la landing es larga)
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [route])

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

  // '/catalogo?tienda=1' -> ruta '/catalogo' y query 'tienda=1' (filtros del catálogo)
  const [ruta, query = ''] = route.split('?')

  // Extraer id de producto de rutas tipo /producto/123
  const productIdMatch = ruta.match(/^\/producto\/(.+)$/)
  const productId = productIdMatch ? productIdMatch[1] : null

  // Retorno de Mercado Pago: se decide en el JSX, después de los hooks
  const pagoPath = isPagoPath(pathname) ? pathname : null
  const esLanding = !pagoPath && ruta === '/'

  return (
    <AuthProvider>
      <FavoritesProvider>
        <Navbar route={ruta} navigate={navigate} />
        <main className={esLanding ? 'landing-main' : 'container py-4'}>
          {pagoPath ? (
            <PagoResultado exito={pagoPath === '/pago/exito'} navigate={navigate} />
          ) : ruta === '/' ? (
            <Landing />
          ) : ruta === '/catalogo' ? (
            // key: si cambian los filtros de la URL (ej. desde la landing), el catálogo arranca de nuevo con ellos
            <Home key={route} navigate={navigate} filtrosIniciales={filtrosDesdeQuery(query)} />
          ) : productId ? (
            <ProductDetail id={productId} navigate={navigate} />
          ) : ruta === '/login' ? (
            <Login navigate={navigate} />
          ) : ruta === '/registro' ? (
            <Register navigate={navigate} />
          ) : ruta === '/planes' ? (
            <Plans navigate={navigate} />
          ) : ruta === '/favoritos' ? (
            <Favorites navigate={navigate} />
          ) : ruta === '/busqueda-visual' ? (
            <BusquedaVisual navigate={navigate} />
          ) : (
            <NotFound navigate={navigate} />
          )}
        </main>
      </FavoritesProvider>
    </AuthProvider>
  )
}
