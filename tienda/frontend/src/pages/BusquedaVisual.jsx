import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../context/AuthContext'
import ProductCard from '../components/ProductCard'

const MAX_BYTES = 5 * 1024 * 1024
const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/tiff']

export default function BusquedaVisual({ navigate }) {
  const { user, loading } = useAuth()
  const esPremium = user?.premium === true
  const [archivo, setArchivo] = useState(null)
  const [vistaPrevia, setVistaPrevia] = useState(null)
  const [resultados, setResultados] = useState([])
  const [cargando, setCargando] = useState(false)
  const [msg, setMsg] = useState('')
  const [msgError, setMsgError] = useState(false)

  // La vista previa es una URL local del archivo elegido; se libera al cambiar la foto.
  useEffect(() => {
    if (!archivo) {
      setVistaPrevia(null)
      return undefined
    }
    const url = URL.createObjectURL(archivo)
    setVistaPrevia(url)
    return () => URL.revokeObjectURL(url)
  }, [archivo])

  function elegirArchivo(e) {
    const archivoElegido = e.target.files?.[0] || null
    setResultados([])
    setMsg('')
    setMsgError(false)

    if (!archivoElegido) {
      setArchivo(null)
      return
    }
    if (!TIPOS_PERMITIDOS.includes(archivoElegido.type)) {
      setArchivo(null)
      setMsg('Elegí una imagen JPEG, PNG o TIFF.')
      setMsgError(true)
      return
    }
    if (archivoElegido.size > MAX_BYTES) {
      setArchivo(null)
      setMsg('La imagen supera el tamaño máximo de 5 MB.')
      setMsgError(true)
      return
    }
    setArchivo(archivoElegido)
  }

  async function buscar(e) {
    e.preventDefault()
    if (!archivo) {
      setMsg('Primero elegí una foto.')
      setMsgError(true)
      return
    }
    setCargando(true)
    setMsg('')
    setMsgError(false)
    setResultados([])
    try {
      const datos = await api.buscarVisual(archivo, 8)
      const encontrados = Array.isArray(datos) ? datos : []
      setResultados(encontrados)
      if (encontrados.length === 0) {
        setMsg('No encontramos prendas parecidas en el catálogo.')
        setMsgError(true)
      }
    } catch (err) {
      setMsg(err.message || 'No se pudo realizar la búsqueda visual.')
      setMsgError(true)
    } finally {
      setCargando(false)
    }
  }

  if (loading) return <p>Cargando...</p>

  // El backend también lo valida; acá se evita gastar créditos con un usuario no premium.
  if (!esPremium) {
    return (
      <section className="page busqueda-visual-page">
        <h1>Búsqueda visual</h1>
        <div className="mensaje err">
          Esta función es exclusiva para usuarios premium.
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/planes')}>
          Ver planes
        </button>
      </section>
    )
  }

  return (
    <section className="page busqueda-visual-page">
      <h1>Búsqueda visual</h1>
      <p className="visual-hint">
        Subí una foto de una prenda y te mostramos los productos del catálogo más parecidos.
      </p>

      <form className="visual-form" onSubmit={buscar}>
        <label htmlFor="foto-visual">Foto de la prenda</label>
        <input
          id="foto-visual"
          name="imagen"
          type="file"
          accept="image/jpeg,image/png,image/tiff"
          onChange={elegirArchivo}
        />

        {vistaPrevia && (
          <div className="visual-preview">
            <img src={vistaPrevia} alt="Vista previa de la foto elegida" />
            <div>
              <strong>{archivo.name}</strong>
              <p className="visual-hint">Lista para buscar.</p>
            </div>
          </div>
        )}

        <div className="visual-actions">
          <button className="btn btn-primary" type="submit" disabled={!archivo || cargando}>
            {cargando ? 'Buscando...' : 'Buscar prendas parecidas'}
          </button>
          {cargando && <span className="visual-hint">Esto puede tardar unos segundos.</span>}
        </div>
      </form>

      {msg && <div className={`mensaje ${msgError ? 'err' : 'ok'}`}>{msg}</div>}

      {resultados.length > 0 && (
        <div className="visual-results">
          <h2>Productos más parecidos</h2>
          <div className="grid">
            {resultados.map((resultado, i) => {
              const distancia = resultado.distancia == null
                ? null
                : Number(resultado.distancia)
              return (
                <div className="resultado" key={`${resultado.producto?.id}-${i}`}>
                  <span className="resultado-distancia">
                    {distancia == null || Number.isNaN(distancia)
                      ? 'Parecido'
                      : `Distancia ${distancia.toFixed(2)}`}
                  </span>
                  <ProductCard producto={resultado.producto} navigate={navigate} />
                </div>
              )
            })}
          </div>
        </div>
      )}
    </section>
  )
}
