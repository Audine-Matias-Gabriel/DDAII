import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useFavorites } from '../context/FavoritesContext'
import AuthModal from './AuthModal'

export default function Navbar({ route, navigate }) {
  const { user, isAuthenticated, logout, loading } = useAuth()
  const { favorites } = useFavorites()
  const [modalOpen, setModalOpen] = useState(false)

  return (
    <>
      <nav className="navbar">
        <div className="navbar-content">
          <div className="logo" onClick={() => navigate('/')}>
            👕 Tienda
          </div>

          <div className="nav-links">
            <a
              href="#/"
              onClick={(e) => {
                e.preventDefault()
                navigate('/')
              }}
              className={route === '/' || route === '/catalogo' ? 'active' : ''}
            >
              Inicio
            </a>
            <a
              href="#/catalogo"
              onClick={(e) => {
                e.preventDefault()
                navigate('/catalogo')
              }}
              className={route === '/catalogo' ? 'active' : ''}
            >
              Catálogo
            </a>
            <a
              href="#/planes"
              onClick={(e) => {
                e.preventDefault()
                navigate('/planes')
              }}
              className={route === '/planes' ? 'active' : ''}
            >
              Planes
            </a>
          </div>

          <div className="nav-actions">
            {isAuthenticated ? (
              <div className="user-menu">
                <span className="user-greeting">Hola, {user.nombre}</span>
                <button
                  className="btn btn-outline"
                  onClick={() => {
                    logout()
                    navigate('/')
                  }}
                >
                  Cerrar sesión
                </button>
              </div>
            ) : (
              <button className="btn btn-primary" onClick={() => setModalOpen(true)}>
                Ingresar / Crear cuenta
              </button>
            )}
          </div>
        </div>
      </nav>

      {modalOpen && (
        <AuthModal onClose={() => setModalOpen(false)} />
      )}
    </>
  )
}
