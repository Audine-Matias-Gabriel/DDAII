import { useState, useEffect } from 'react'
import { api } from '../api'

const ANCHO = 640
const ALTO = 220
const MARGEN = { izq: 62, der: 20, arriba: 20, abajo: 42 }

/**
 * Gráfico simple de la evolución diaria de precios.
 * No usa librerías externas: dibuja un SVG con la serie que devuelve el backend.
 */
export default function PriceHistoryChart({ productoId }) {
  const [historial, setHistorial] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError('')

    api.getHistorialPrecios(productoId)
      .then((data) => {
        if (vigente) setHistorial(Array.isArray(data) ? data : [])
      })
      .catch(() => {
        if (vigente) setError('No se pudo cargar el historial de precios.')
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })

    return () => { vigente = false }
  }, [productoId])

  const titulo = <h2>Evolución de precios</h2>

  if (cargando) {
    return (
      <section className="historial-precios">
        {titulo}
        <p className="mensaje">Cargando historial...</p>
      </section>
    )
  }

  if (error) {
    return (
      <section className="historial-precios">
        {titulo}
        <p className="mensaje err">{error}</p>
      </section>
    )
  }

  const puntos = historial
    .map((p) => ({ fecha: p.fecha, precio: Number(p.precio) }))
    .filter((p) => Number.isFinite(p.precio))

  if (puntos.length === 0) {
    return (
      <section className="historial-precios">
        {titulo}
        <p className="mensaje">Todavía no hay snapshots de precio para este producto.</p>
      </section>
    )
  }

  const precios = puntos.map((p) => p.precio)
  const min = Math.min(...precios)
  const max = Math.max(...precios)
  const rango = max - min || 1
  const baseY = ALTO - MARGEN.abajo

  const x = (i) => MARGEN.izq +
    (i * (ANCHO - MARGEN.izq - MARGEN.der)) / Math.max(puntos.length - 1, 1)
  const y = (precio) => MARGEN.arriba +
    (1 - (precio - min) / rango) * (baseY - MARGEN.arriba)

  const linea = puntos
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${x(i).toFixed(1)} ${y(p.precio).toFixed(1)}`)
    .join(' ')
  const area = `${linea} L ${x(puntos.length - 1).toFixed(1)} ${baseY} L ${x(0).toFixed(1)} ${baseY} Z`

  return (
    <section className="historial-precios">
      {titulo}
      <p className="hint">Precios guardados en los snapshots diarios del scraper.</p>
      <svg
        className="historial-chart"
        viewBox={`0 0 ${ANCHO} ${ALTO}`}
        role="img"
        aria-label="Gráfico de la evolución del precio"
      >
        <line className="historial-eje-linea" x1={MARGEN.izq} y1={MARGEN.arriba}
          x2={MARGEN.izq} y2={baseY} />
        <line className="historial-eje-linea" x1={MARGEN.izq} y1={baseY}
          x2={ANCHO - MARGEN.der} y2={baseY} />

        <path className="historial-area" d={area} />
        <path className="historial-linea" d={linea} />

        {puntos.map((p, i) => (
          <circle key={p.fecha} className="historial-punto"
            cx={x(i)} cy={y(p.precio)} r="3.5">
            <title>{`${p.fecha}: $${p.precio}`}</title>
          </circle>
        ))}

        <text className="historial-eje-texto" x={MARGEN.izq - 8} y={MARGEN.arriba + 4} textAnchor="end">
          ${max}
        </text>
        <text className="historial-eje-texto" x={MARGEN.izq - 8} y={baseY} textAnchor="end">
          ${min}
        </text>
        <text className="historial-eje-texto" x={MARGEN.izq} y={ALTO - 12} textAnchor="start">
          {puntos[0].fecha}
        </text>
        <text className="historial-eje-texto" x={ANCHO - MARGEN.der} y={ALTO - 12} textAnchor="end">
          {puntos[puntos.length - 1].fecha}
        </text>
      </svg>
    </section>
  )
}
