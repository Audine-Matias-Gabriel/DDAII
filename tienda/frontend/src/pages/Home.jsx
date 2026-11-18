import { useState, useEffect, useRef } from 'react'
import { api } from '../api'
import ProductCard from '../components/ProductCard'

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

export default function Home({ navigate }) {
  const [productos, setProductos] = useState([])
  const [tiendas, setTiendas] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')
  const [filtros, setFiltros] = useState(FILTROS_VACIOS)
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

  async function cargar() {
    if (rangoInvalido) return
    const consulta = ++ultimaConsulta.current
    setLoading(true)
    setMsg('')
    try {
      const data = await api.getProductos({
        texto: filtros.texto || undefined,
        categoria: filtros.categoria || undefined,
        genero: filtros.genero || undefined,
        tienda: filtros.tienda || undefined,
        precioMin: min,
        precioMax: max,
        pagina: 0,
        tamano: 20
      })
      if (consulta !== ultimaConsulta.current) return
      setProductos(data.contenido || data || [])
    } catch (e) {
      if (consulta !== ultimaConsulta.current) return
      setMsg('No se pudo cargar el catálogo. ¿Está levantado el backend en http://localhost:8080?')
    } finally {
      if (consulta === ultimaConsulta.current) setLoading(false)
    }
  }

  function cambiar(campo) {
    return (e) => setFiltros({ ...filtros, [campo]: e.target.value })
  }

  return (
    <section className="page">
      <h1>Catálogo de ropa</h1>
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
        <div className="grid">
          {productos.map((p) => (
            <ProductCard key={p.id} producto={p} navigate={navigate} />
          ))}
        </div>
      )}
    </section>
  )
}
