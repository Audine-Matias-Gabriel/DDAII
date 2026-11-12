import { useState, useEffect } from 'react'
import { api } from '../api'
import ProductCard from '../components/ProductCard'

export default function Home({ navigate }) {
  const [productos, setProductos] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')
  const [filtros, setFiltros] = useState({
    texto: '',
    categoria: '',
    genero: ''
  })

  useEffect(() => {
    cargar()
  }, [filtros.texto, filtros.categoria, filtros.genero])

  async function cargar() {
    setLoading(true)
    setMsg('')
    try {
      const data = await api.getProductos({
        texto: filtros.texto || undefined,
        categoria: filtros.categoria || undefined,
        genero: filtros.genero || undefined,
        pagina: 0,
        tamano: 20
      })
      setProductos(data.contenido || data || [])
    } catch (e) {
      setMsg('No se pudo cargar el catálogo. ¿Está levantado el backend en http://localhost:8080?')
    } finally {
      setLoading(false)
    }
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
          onChange={(e) => setFiltros({ ...filtros, texto: e.target.value })}
        />
        <select
          value={filtros.categoria}
          onChange={(e) => setFiltros({ ...filtros, categoria: e.target.value })}
        >
          <option value="">Todas las categorías</option>
          <option value="REMERA">Remera</option>
          <option value="PANTALON">Pantalón</option>
          <option value="CALZADO">Calzado</option>
          <option value="VESTIDO">Vestido</option>
        </select>
        <select
          value={filtros.genero}
          onChange={(e) => setFiltros({ ...filtros, genero: e.target.value })}
        >
          <option value="">Todos los géneros</option>
          <option value="Mujer">Mujer</option>
          <option value="Hombre">Hombre</option>
          <option value="Unisex">Unisex</option>
        </select>
        <button className="btn btn-primary" onClick={cargar}>
          Aplicar filtros
        </button>
      </div>

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
