import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { api } from '../api'

export default function Plans({ navigate }) {
  const { user, isAuthenticated, restoreSession } = useAuth()
  const [loading, setLoading] = useState(false)
  const [verificando, setVerificando] = useState(false)
  const [msg, setMsg] = useState('')
  const [msgError, setMsgError] = useState(false)
  const [aprobada, setAprobada] = useState(false)
  const [hayPendiente, setHayPendiente] = useState(false)

  // Premium si el backend ya lo marcó o si hay una suscripción aprobada
  // (el flag premium se activa por un evento, puede tardar un instante).
  const esPremium = !!user?.premium || aprobada

  function mostrar(texto, error = false) {
    setMsg(texto)
    setMsgError(error)
  }

  // En localhost Mercado Pago no vuelve solo a /pago/exito, así que acá se
  // le pregunta al backend si alguna suscripción pendiente ya se pagó.
  async function verificar(manual) {
    setVerificando(true)
    try {
      const suscripciones = await api.verificarSuscripciones()
      if (suscripciones.some((s) => s.estado === 'APROBADA')) {
        setAprobada(true)
        setHayPendiente(false)
        mostrar('¡Listo! Ya sos premium.')
        restoreSession()
      } else {
        const pendiente = suscripciones.some((s) => s.estado === 'PENDIENTE')
        setHayPendiente(pendiente)
        if (manual) {
          mostrar(pendiente
            ? 'Todavía no figura un pago aprobado. Si recién pagaste, probá de nuevo en unos segundos.'
            : 'No hay pagos pendientes.', true)
        }
      }
    } catch (e) {
      if (manual) mostrar(e.message || 'No se pudo verificar el pago.', true)
    } finally {
      setVerificando(false)
    }
  }

  // Al entrar a Planes y al volver a la pestaña (por ejemplo, después de
  // pagar en Mercado Pago).
  useEffect(() => {
    if (!isAuthenticated || user?.premium) return
    verificar(false)
    const alVolver = () => {
      if (document.visibilityState === 'visible') verificar(false)
    }
    document.addEventListener('visibilitychange', alVolver)
    return () => document.removeEventListener('visibilitychange', alVolver)
  }, [isAuthenticated, user?.premium])

  async function irPremium() {
    if (!isAuthenticated) {
      mostrar('Primero iniciá sesión o creá una cuenta.', true)
      navigate('/login')
      return
    }
    if (esPremium) return
    setLoading(true)
    setMsg('')
    try {
      const data = await api.createSuscripcion(user.id)
      if (data.urlPago) {
        window.location.href = data.urlPago
      } else {
        mostrar('No se pudo obtener la URL de pago.', true)
      }
    } catch (e) {
      mostrar(e.message || 'Error al crear el pago premium.', true)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="page plans-page">
      <h1>Planes</h1>
      {msg && <div className={`mensaje ${msgError ? 'err' : 'ok'}`}>{msg}</div>}

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
              disabled={loading || esPremium}
            >
              {esPremium ? 'Ya sos premium' : loading ? 'Procesando...' : 'Hacerme premium'}
            </button>
            {isAuthenticated && !esPremium && hayPendiente && (
              <p className="hint">
                ¿Ya pagaste en Mercado Pago?{' '}
                <button
                  type="button"
                  className="link"
                  onClick={() => verificar(true)}
                  disabled={verificando}
                >
                  {verificando ? 'Verificando...' : 'Verificar pago'}
                </button>
              </p>
            )}
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
