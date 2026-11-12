import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

export default function AuthModal({ onClose }) {
  const [mode, setMode] = useState('login') // 'login' | 'register'
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const { login, register } = useAuth()

  const [form, setForm] = useState({
    nombre: '',
    email: '',
    password: ''
  })

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (mode === 'login') {
        await login({ email: form.email, password: form.password })
      } else {
        await register({
          nombre: form.nombre,
          email: form.email,
          password: form.password
        })
      }
      onClose()
    } catch (err) {
      setError(err.message || 'Error al completar la operación')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{mode === 'login' ? 'Ingresar' : 'Crear cuenta'}</h2>
          <button className="close" onClick={onClose}>
            ✕
          </button>
        </div>

        {error && <div className="mensaje err">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          {mode === 'register' && (
            <div className="field">
              <label>Nombre</label>
              <input
                required
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                placeholder="Juan Pérez"
              />
            </div>
          )}
          <div className="field">
            <label>Email</label>
            <input
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="juan@test.com"
            />
          </div>
          <div className="field">
            <label>Contraseña</label>
            <input
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="••••••••"
            />
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Procesando...' : mode === 'login' ? 'Ingresar' : 'Crear cuenta'}
          </button>

          <p className="mode-switch">
            ¿No tienes cuenta?{' '}
            <button
              type="button"
              className="link"
              onClick={() => setMode('register')}
            >
              Crear cuenta
            </button>
            {' '}
            ¿Ya tienes cuenta?{' '}
            <button
              type="button"
              className="link"
              onClick={() => setMode('login')}
            >
              Ingresar
            </button>
          </p>
        </form>
      </div>
    </div>
  )
}
