import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { api } from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    if (storedToken && storedUser) {
      setToken(storedToken)
      setUser(JSON.parse(storedUser))
    }
    setLoading(false)
  }, [])

  const saveSession = useCallback((tokenValue, userValue) => {
    setToken(tokenValue)
    setUser(userValue)
    localStorage.setItem('token', tokenValue)
    localStorage.setItem('user', JSON.stringify(userValue))
  }, [])

  const login = async (credentials) => {
    const data = await api.login(credentials)
    saveSession(data.token, data.usuario)
    return data
  }

  const register = async (credentials) => {
    const data = await api.register(credentials)
    if (data.token && data.usuario) {
      saveSession(data.token, data.usuario)
    }
    return data
  }

  const logout = async () => {
    try {
      await api.logout()
    } catch (e) {
      // Ignorar errores de red al cerrar sesión
    }
    setToken(null)
    setUser(null)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const restoreSession = async () => {
    if (!token) return
    try {
      const data = await api.me()
      setUser(data.usuario || data.user || data)
      localStorage.setItem('user', JSON.stringify(data.usuario || data.user || data))
    } catch (e) {
      // Token expirado o inválido
      logout()
    }
  }

  const isAuthenticated = !!token && !!user

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated,
        login,
        register,
        logout,
        restoreSession
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}
