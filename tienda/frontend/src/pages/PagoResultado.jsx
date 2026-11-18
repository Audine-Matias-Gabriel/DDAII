import { useEffect, useRef, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { api } from '../api'

// Página a la que vuelve Mercado Pago (back URLs /pago/exito y /pago/error).
export default function PagoResultado({ exito, navigate }) {
  const { token, loading: cargandoSesion, restoreSession } = useAuth()
  const [estado, setEstado] = useState(exito ? 'confirmando' : 'error')
  const [msg, setMsg] = useState(exito ? 'Confirmando el pago...' : 'El pago no se completó.')
  const confirmado = useRef(false)

  useEffect(() => {
    if (!exito || cargandoSesion || confirmado.current) return
    confirmado.current = true

    if (!token) {
      setEstado('error')
      setMsg('Iniciá sesión para confirmar tu pago.')
      return
    }

    const params = new URLSearchParams(window.location.search)
    const paymentId = params.get('payment_id')
    const suscripcionId = params.get('external_reference')
    if (!paymentId || !suscripcionId) {
      setEstado('error')
      setMsg('Faltan datos del pago en la URL. No se pudo confirmar.')
      return
    }

    api
      .confirmarSuscripcion(suscripcionId, paymentId)
      .then(() => {
        setEstado('ok')
        setMsg('¡Listo! Ya sos premium.')
        restoreSession()
      })
      .catch((e) => {
        setEstado('error')
        setMsg(e.message || 'No se pudo confirmar el pago.')
      })
  }, [exito, cargandoSesion, token, restoreSession])

  function volver() {
    window.history.replaceState(null, '', '/')
    navigate('/catalogo')
  }

  return (
    <section className="page">
      <h1>{exito ? 'Resultado del pago' : 'Pago no completado'}</h1>
      <div className={`mensaje ${estado === 'ok' ? 'ok' : estado === 'error' ? 'err' : ''}`}>
        {msg}
      </div>
      {exito && !token && !cargandoSesion && (
        <p>
          <button type="button" className="btn btn-primary" onClick={() => {
            window.history.replaceState(null, '', '/')
            navigate('/login')
          }}>
            Iniciar sesión
          </button>
        </p>
      )}
      <button type="button" className="btn btn-primary" onClick={volver}>
        Volver al catálogo
      </button>
    </section>
  )
}
