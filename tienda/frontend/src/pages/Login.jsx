import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

export default function Login({ navigate }) {
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const { login } = useAuth()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login({ email: form.email, password: form.password })
      navigate('/catalogo')
    } catch (err) {
      setError(err.message || 'Error al iniciar sesión')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="page auth-page">
      <div className="auth-card">
        <h2>Ingresar</h2>
        {error && <div className="mensaje err">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Email</label>
            <input
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </div>
          <div className="field">
            <label>Contraseña</label>
            <input
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>
        <p className="hint">
          ¿No tienes cuenta?{' '}
          <button type="button" className="link" onClick={() => navigate('/registro')}>
            Crear cuenta
          </button>
        </p>
      </div>
    </section>
  )
}
