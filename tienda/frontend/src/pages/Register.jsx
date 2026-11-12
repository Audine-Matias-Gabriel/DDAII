import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

export default function Register({ navigate }) {
  const [form, setForm] = useState({ nombre: '', email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const { register } = useAuth()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register({
        nombre: form.nombre,
        email: form.email,
        password: form.password
      })
      navigate('/catalogo')
    } catch (err) {
      setError(err.message || 'Error al crear la cuenta')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="page auth-page">
      <div className="auth-card">
        <h2>Crear cuenta</h2>
        {error && <div className="mensaje err">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Nombre</label>
            <input
              required
              value={form.nombre}
              onChange={(e) => setForm({ ...form, nombre: e.target.value })}
            />
          </div>
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
            {loading ? 'Creando...' : 'Crear cuenta'}
          </button>
        </form>
        <p className="hint">
          ¿Ya tienes cuenta?{' '}
          <button type="button" className="link" onClick={() => navigate('/login')}>
            Ingresar
          </button>
        </p>
      </div>
    </section>
  )
}
