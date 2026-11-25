import { useState, useEffect, useRef } from 'react'
import { api } from '../api'
import ProductCard from '../components/ProductCard'
import { rutaCatalogo } from '../query'
import { TAMANO_PAGINA, hayMasPaginas, unirProductos } from '../paginacion'

const FILTROS_VACIOS = {
  texto: '',
  categoria: '',
  genero: '',
  tienda: '',
  precioMin: '',
  precioMax: ''
}

// Precio válido (número >= 0) o undefined para no mandarlo.
function precio(valor) {
  if (valor === '' || valor == null) return undefined
  const n = Number(valor)
  return Number.isFinite(n) && n >= 0 ? n : undefined
}

// filtrosIniciales: los que vienen en la URL, ej. #/catalogo?tienda=1 desde la landing
export default function Home({ navigate, filtrosIniciales = {} }) {
  const [productos, setProductos] = useState([])
  const [tiendas, setTiendas] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')
  const [filtros, setFiltros] = useState(() => ({ ...FILTROS_VACIOS, ...filtrosIniciales }))
  // Paginación: total de productos con estos filtros y última página cargada (la primera es 0)
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [cargandoMas, setCargandoMas] = useState(false)
  const [msgMas, setMsgMas] = useState('')
  // Número de la última consulta: si cambian dos filtros seguidos, una respuesta
  // vieja que llega tarde no pisa a la nueva.
  const ultimaConsulta = useRef(0)

  const min = precio(filtros.precioMin)
  const max = precio(filtros.precioMax)
  const rangoInvalido = min !== undefined && max !== undefined && min > max

  useEffect(() => {
    api.getTiendas()
      .then((lista) => setTiendas(Array.isArray(lista) ? lista : []))
      .catch(() => setTiendas([]))
  }, [])

  useEffect(() => {
    cargar()
  }, [filtros.texto, filtros.categoria, filtros.genero, filtros.tienda, filtros.precioMin, filtros.precioMax])

  // La URL refleja los filtros (replaceState no dispara hashchange): al volver
  // de un producto o recargar, el catálogo queda filtrado igual.
  useEffect(() => {
    const hash = '#' + rutaCatalogo(filtros)
    if (window.location.hash !== hash) {
      window.history.replaceState(window.history.state, '', hash)
    }
  }, [filtros])

  const tiendaElegida = tiendas.find((t) => String(t.id) === String(filtros.tienda))

  // Los filtros como los espera el backend, para la página pedida
  function parametros(numeroPagina) {
    return {
      texto: filtros.texto || undefined,
      categoria: filtros.categoria || undefined,
      genero: filtros.genero || undefined,
      tienda: filtros.tienda || undefined,
      precioMin: min,
      precioMax: max,
      pagina: numeroPagina,
      tamano: TAMANO_PAGINA
    }
  }

  // Primera página: reemplaza la lista (al entrar o al cambiar los filtros)
  async function cargar() {
    if (rangoInvalido) return
    const consulta = ++ultimaConsulta.current
    setLoading(true)
    setCargandoMas(false)
    setMsg('')
    setMsgMas('')
    try {
      const data = await api.getProductos(parametros(0))
      if (consulta !== ultimaConsulta.current) return
      const lista = data.contenido || data || []
      setProductos(lista)
      setTotal(data.total ?? lista.length)
      setPagina(0)
    } catch (e) {
      if (consulta !== ultimaConsulta.current) return
      setMsg('No se pudo cargar el catálogo. ¿Está levantado el backend en http://localhost:8080?')
    } finally {
      if (consulta === ultimaConsulta.current) setLoading(false)
    }
  }

  // "Mostrar más productos": suma la página siguiente al final de la lista
  async function cargarMas() {
    const consulta = ultimaConsulta.current
    const siguiente = pagina + 1
    setCargandoMas(true)
    setMsgMas('')
    try {
      const data = await api.getProductos(parametros(siguiente))
      // Si mientras tanto cambiaron los filtros, esta página ya no corresponde
      if (consulta !== ultimaConsulta.current) return
      setProductos((actuales) => unirProductos(actuales, data.contenido || []))
      if (data.total != null) setTotal(data.total)
      setPagina(siguiente)
    } catch (e) {
      if (consulta !== ultimaConsulta.current) return
      setMsgMas('No se pudieron cargar más productos. Probá de nuevo.')
    } finally {
      if (consulta === ultimaConsulta.current) setCargandoMas(false)
    }
  }

  function cambiar(campo) {
    return (e) => setFiltros({ ...filtros, [campo]: e.target.value })
  }

  return (
    <section className="page">
      <h1>{tiendaElegida ? `Catálogo de ${tiendaElegida.nombre}` : 'Catálogo de ropa'}</h1>
      {msg && <div className="mensaje err">{msg}</div>}

      <div className="filtros">
        <input
          type="text"
          placeholder="Buscar producto..."
          value={filtros.texto}
          onChange={cambiar('texto')}
        />
        <select value={filtros.categoria} onChange={cambiar('categoria')} aria-label="Categoría">
          <option value="">Todas las categorías</option>
          <option value="REMERA">Remera</option>
          <option value="PANTALON">Pantalón</option>
          <option value="CALZADO">Calzado</option>
          <option value="VESTIDO">Vestido</option>
        </select>
        <select value={filtros.genero} onChange={cambiar('genero')} aria-label="Género">
          <option value="">Todos los géneros</option>
          <option value="Mujer">Mujer</option>
          <option value="Hombre">Hombre</option>
          <option value="Unisex">Unisex</option>
          <option value="Niña">Niña</option>
        </select>
        <select value={filtros.tienda} onChange={cambiar('tienda')} aria-label="Tienda">
          <option value="">Todas las tiendas</option>
          {tiendas.map((t) => (
            <option key={t.id} value={t.id}>{t.nombre}</option>
          ))}
        </select>
        <input
          type="number"
          min="0"
          step="1000"
          placeholder="Precio mín."
          aria-label="Precio mínimo"
          className="filtro-precio"
          value={filtros.precioMin}
          onChange={cambiar('precioMin')}
        />
        <input
          type="number"
          min="0"
          step="1000"
          placeholder="Precio máx."
          aria-label="Precio máximo"
          className="filtro-precio"
          value={filtros.precioMax}
          onChange={cambiar('precioMax')}
        />
        <button className="btn btn-primary" onClick={cargar} disabled={rangoInvalido}>
          Aplicar filtros
        </button>
        <button className="btn btn-outline" onClick={() => setFiltros(FILTROS_VACIOS)}>
          Limpiar
        </button>
      </div>

      {rangoInvalido && (
        <p className="mensaje err">El precio mínimo no puede ser mayor que el máximo.</p>
      )}

      {loading ? (
        <p>Cargando productos...</p>
      ) : productos.length === 0 ? (
        <p className="mensaje">No se encontraron productos.</p>
      ) : (
        <>
          <div className="grid">
            {productos.map((p) => (
              <ProductCard key={p.id} producto={p} navigate={navigate} />
            ))}
          </div>

          <div className="catalogo-mas">
            <p className="catalogo-cuenta">
              Viste {productos.length} de {Math.max(total, productos.length)} productos
            </p>
            <div className="catalogo-progreso" aria-hidden="true">
              <span style={{ width: `${Math.min(100, (productos.length / Math.max(total, 1)) * 100)}%` }} />
            </div>
            {msgMas && <p className="mensaje err">{msgMas}</p>}
            {hayMasPaginas(pagina, total) && (
              <button className="btn btn-primary btn-mas" onClick={cargarMas} disabled={cargandoMas}>
                {cargandoMas ? 'Cargando...' : 'Mostrar más productos'}
              </button>
            )}
          </div>
        </>
      )}
    </section>
  )
}
