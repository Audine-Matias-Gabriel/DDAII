import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { api } from '../api'

export default function Plans({ navigate }) {
  const { user, isAuthenticated } = useAuth()
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')

  async function irPremium() {
    if (!isAuthenticated) {
      setMsg('Primero iniciá sesión o creá una cuenta.')
      navigate('/login')
      return
    }
    setLoading(true)
    setMsg('')
    try {
      const data = await api.createSuscripcion(user.id)
      if (data.urlPago) {
        window.location.href = data.urlPago
      } else {
        setMsg('No se pudo obtener la URL de pago.')
      }
    } catch (e) {
      setMsg(e.message || 'Error al crear el pago premium.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="page plans-page">
      <h1>Planes</h1>
      {msg && <div className={`mensaje ${msg.startsWith('Primero') ? 'err' : 'ok'}`}>{msg}</div>}

      <div className="plans-grid">
        <div className="plan-card">
          <div className="plan-body">
            <h3>Gratuito</h3>
            <p className="price">$0 / mes</p>
            <p>Catálogo y detalle públicos.</p>
          </div>
        </div>

        <div className="plan-card">
          <div className="plan-body">
            <h3>Premium</h3>
            <p className="price">$999 / mes</p>
            <p>Experiencia premium. Pago de prueba con Mercado Pago (sandbox).</p>
            <button
              className="btn btn-primary"
              onClick={irPremium}
              disabled={loading}
            >
              {loading ? 'Procesando...' : 'Hacerme premium'}
            </button>
          </div>
        </div>
      </div>

      {!isAuthenticated && (
        <p className="hint">
          Para contratar el plan premium necesitás iniciar sesión.
          {' '}
          <button type="button" className="link" onClick={() => navigate('/login')}>
            Ingresar
          </button>
        </p>
      )}
    </section>
  )
}
