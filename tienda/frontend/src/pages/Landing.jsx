import { useEffect, useRef, useState } from 'react'
import { api } from '../api'
import { rutaCatalogo } from '../query'
import {
  dominio,
  elegirDestacados,
  estiloTema,
  formatearPrecio,
  imagenVtex,
  marcasPrincipales,
  ordenarTiendas,
  temaDeTienda,
  unirNombres
} from '../tiendas'
import './Landing.css'

// Cada cuánto pasa sola a la tienda siguiente
const INTERVALO_MS = 6500

// Link al catálogo con el filtro de la tienda ya aplicado: #/catalogo?tienda=1
function hrefTienda(tienda) {
  return '#' + rutaCatalogo({ tienda: tienda.id })
}

// Trae la cantidad de productos, las marcas y las fotos de una tienda.
async function cargarTienda(tienda) {
  try {
    const data = await api.getProductos({ tienda: tienda.id, tamano: 60 })
    const productos = Array.isArray(data?.contenido) ? data.contenido : []
    return {
      ...tienda,
      total: data?.total ?? productos.length,
      destacados: elegirDestacados(productos),
      marcas: marcasPrincipales(productos)
    }
  } catch {
    // Sin productos la tienda igual se muestra, con su link al catálogo
    return { ...tienda, total: null, destacados: [], marcas: [] }
  }
}

export default function Landing() {
  const [tiendas, setTiendas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let vigente = true
    async function cargar() {
      try {
        const lista = await api.getTiendas()
        const activas = ordenarTiendas((Array.isArray(lista) ? lista : []).filter((t) => t.activa !== false))
        const completas = await Promise.all(activas.map(cargarTienda))
        if (vigente) setTiendas(completas)
      } catch {
        if (vigente) setError('No se pudieron cargar las tiendas. ¿Está levantado el backend en http://localhost:8080?')
      } finally {
        if (vigente) setCargando(false)
      }
    }
    cargar()
    return () => {
      vigente = false
    }
  }, [])

  const totalProductos =
    tiendas.length > 0 && tiendas.every((t) => t.total != null)
      ? tiendas.reduce((suma, t) => suma + t.total, 0)
      : null
  const nombres = unirNombres(tiendas.map((t) => t.nombre))
  const sinTiendas = !cargando && tiendas.length === 0

  return (
    <div className="landing">
      <h1 className="landing-franja">
        {tiendas.length > 0 ? (
          <>
            Compará {totalProductos ? `${totalProductos.toLocaleString('es-AR')} productos de ` : ''}
            <strong>{nombres}</strong> en un solo lugar
          </>
        ) : (
          'Las mejores tiendas deportivas en un solo lugar'
        )}
      </h1>

      {cargando ? (
        <div className="carrusel carrusel-esqueleto" aria-busy="true" aria-label="Cargando tiendas" />
      ) : sinTiendas ? (
        <PortadaGenerica />
      ) : (
        <CarruselTiendas tiendas={tiendas} />
      )}

      {error && (
        <div className="landing-aviso">
          <p className="mensaje err">{error}</p>
        </div>
      )}
      {sinTiendas && !error && (
        <div className="landing-aviso">
          <p className="mensaje">Todavía no hay tiendas: el catálogo se llena cuando corre el scraper.</p>
        </div>
      )}

      {!sinTiendas && (
        <section className="landing-seccion" aria-labelledby="titulo-tiendas">
          <header className="landing-encabezado">
            <p className="landing-eyebrow">{tiendas.length || 3} tiendas, un catálogo</p>
            <h2 id="titulo-tiendas">Elegí tu tienda</h2>
            <p>Entrás a una tienda y el catálogo se abre con sus productos ya filtrados.</p>
          </header>
          <ul className="tiendas-grilla">
            {cargando
              ? [0, 1, 2].map((i) => (
                  <li key={i}>
                    <div className="tile tile-esqueleto" />
                  </li>
                ))
              : tiendas.map((t) => (
                  <li key={t.id}>
                    <TarjetaTienda tienda={t} />
                  </li>
                ))}
          </ul>
        </section>
      )}

      <section className="landing-cierre" aria-labelledby="titulo-cierre">
        <div className="landing-cierre-contenido">
          <p className="landing-eyebrow">Todo en un solo lugar</p>
          <h2 id="titulo-cierre">
            {totalProductos ? (
              <>
                <span>{totalProductos.toLocaleString('es-AR')} productos.</span>{' '}
                <span>
                  {tiendas.length} {tiendas.length === 1 ? 'tienda' : 'tiendas'}.
                </span>{' '}
                <span className="landing-cierre-destacado">Un solo catálogo.</span>
              </>
            ) : (
              'Todo el catálogo en un solo lugar.'
            )}
          </h2>
          <p>
            Buscá y filtrá por categoría, género y precio. Cuando encontrás lo que querés, lo comprás
            directo en la tienda.
          </p>
          <a className="landing-boton" href={'#' + rutaCatalogo()}>
            Ver todo el catálogo <span aria-hidden="true">→</span>
          </a>
        </div>
      </section>
    </div>
  )
}

// Carrusel de portada: una diapositiva grande por tienda, cada una lleva a su catálogo.
function CarruselTiendas({ tiendas }) {
  const [actual, setActual] = useState(0)
  const [pausaManual, setPausaManual] = useState(false)
  const [mouseEncima, setMouseEncima] = useState(false)
  const [focoTeclado, setFocoTeclado] = useState(false)
  const menosMovimiento = usePrefiereMenosMovimiento()
  const inicioToque = useRef(null)

  const cantidad = tiendas.length
  const corriendo = cantidad > 1 && !pausaManual && !mouseEncima && !focoTeclado && !menosMovimiento

  useEffect(() => {
    if (!corriendo) return
    const id = setTimeout(() => setActual((i) => (i + 1) % cantidad), INTERVALO_MS)
    return () => clearTimeout(id)
  }, [actual, corriendo, cantidad])

  const ir = (i) => setActual(((i % cantidad) + cantidad) % cantidad)

  function alTocar(e) {
    inicioToque.current = e.touches[0].clientX
  }

  function alSoltar(e) {
    if (inicioToque.current == null) return
    const recorrido = e.changedTouches[0].clientX - inicioToque.current
    inicioToque.current = null
    if (Math.abs(recorrido) > 50) ir(actual + (recorrido < 0 ? 1 : -1))
  }

  return (
    <section
      className="carrusel"
      aria-roledescription="carrusel"
      aria-label="Tiendas"
      onMouseEnter={() => setMouseEncima(true)}
      onMouseLeave={() => setMouseEncima(false)}
      onFocus={(e) => {
        // Solo el foco de teclado pausa: un clic con el mouse no lo deja frenado
        if (e.target.matches?.(':focus-visible')) setFocoTeclado(true)
      }}
      onBlur={(e) => {
        if (!e.currentTarget.contains(e.relatedTarget)) setFocoTeclado(false)
      }}
      onTouchStart={alTocar}
      onTouchEnd={alSoltar}
    >
      <div className="carrusel-pista" aria-live={corriendo ? 'off' : 'polite'}>
        {tiendas.map((t, i) => (
          <SlideTienda key={t.id} tienda={t} activa={i === actual} posicion={i + 1} cantidad={cantidad} />
        ))}
      </div>

      {cantidad > 1 && (
        <div className="carrusel-controles" style={estiloTema(tiendas[actual])}>
          <div className="carrusel-tabs">
            {tiendas.map((t, i) => (
              <button
                key={t.id}
                type="button"
                className={`carrusel-tab${i === actual ? ' activa' : ''}`}
                aria-label={`Mostrar ${t.nombre}`}
                aria-current={i === actual ? 'true' : undefined}
                onClick={() => ir(i)}
              >
                <span className="carrusel-tab-nombre">{t.nombre}</span>
                <span className="carrusel-tab-barra">
                  {i === actual && (
                    <span
                      key={`${actual}-${corriendo}`}
                      className={`carrusel-tab-progreso${corriendo ? ' corriendo' : ''}`}
                      style={{ animationDuration: `${INTERVALO_MS}ms` }}
                    />
                  )}
                </span>
              </button>
            ))}
          </div>
          <div className="carrusel-botones">
            {!menosMovimiento && (
              <button
                type="button"
                className="carrusel-boton"
                onClick={() => setPausaManual((p) => !p)}
                aria-label={pausaManual ? 'Reanudar el carrusel' : 'Pausar el carrusel'}
              >
                {pausaManual ? <IconoPlay /> : <IconoPausa />}
              </button>
            )}
            <button
              type="button"
              className="carrusel-boton carrusel-flecha"
              onClick={() => ir(actual - 1)}
              aria-label="Tienda anterior"
            >
              <IconoFlecha izquierda />
            </button>
            <button
              type="button"
              className="carrusel-boton carrusel-flecha"
              onClick={() => ir(actual + 1)}
              aria-label="Tienda siguiente"
            >
              <IconoFlecha />
            </button>
          </div>
        </div>
      )}
    </section>
  )
}

function SlideTienda({ tienda, activa, posicion, cantidad }) {
  const tema = temaDeTienda(tienda)
  const [principal, ...resto] = tienda.destacados
  const secundarios = resto.slice(0, 2)

  return (
    <div
      className={`slide-grupo${activa ? ' activa' : ''}`}
      role="group"
      aria-roledescription="diapositiva"
      aria-label={`${posicion} de ${cantidad}: ${tienda.nombre}`}
      style={estiloTema(tienda)}
    >
      <a className="slide" href={hrefTienda(tienda)} aria-label={`Ver los productos de ${tienda.nombre} en el catálogo`}>
        <span className="slide-marca-agua" aria-hidden="true">
          {tienda.nombre}
        </span>
        <div className="slide-contenido">
          <div className="slide-texto">
            <p className="slide-eyebrow">{tema.bajada}</p>
            <h2 className="slide-titulo">{tienda.nombre}</h2>
            <p className="slide-lema">{tema.lema}</p>
            {tienda.marcas.length > 0 && (
              <ul className="slide-marcas">
                {tienda.marcas.map((m) => (
                  <li key={m}>{m}</li>
                ))}
              </ul>
            )}
            <span className="slide-cta">
              Ver productos <span className="slide-cta-flecha" aria-hidden="true">→</span>
            </span>
          </div>

          {principal && (
            <div className="slide-collage" aria-hidden="true">
              <figure className="pieza pieza-principal">
                <img src={imagenVtex(principal.imagenUrl, 700)} alt="" decoding="async" />
                <figcaption>
                  <span>{principal.nombre}</span>
                  <strong>{formatearPrecio(principal.precio)}</strong>
                </figcaption>
              </figure>
              {secundarios.map((p, i) => (
                <figure key={p.id} className={`pieza pieza-chica pieza-${i + 1}`}>
                  <img src={imagenVtex(p.imagenUrl, 360)} alt="" decoding="async" />
                </figure>
              ))}
              {tienda.total != null && (
                <span className="slide-sticker">
                  <strong>{tienda.total}</strong> productos
                </span>
              )}
            </div>
          )}
        </div>
      </a>
    </div>
  )
}

function TarjetaTienda({ tienda }) {
  const tema = temaDeTienda(tienda)
  // Otra foto que la del carrusel, para no repetir
  const foto = tienda.destacados[3] || tienda.destacados[0]
  const sitio = dominio(tienda.urlBase)

  return (
    <a className="tile" href={hrefTienda(tienda)} style={estiloTema(tienda)}>
      <div className="tile-imagen">
        {foto ? (
          <img src={imagenVtex(foto.imagenUrl, 600)} alt="" loading="lazy" decoding="async" />
        ) : (
          <span className="tile-placeholder" aria-hidden="true">
            👟
          </span>
        )}
      </div>
      <div className="tile-cuerpo">
        <h3>{tienda.nombre}</h3>
        <p className="tile-bajada">{tema.bajada}</p>
        <p className="tile-detalle">
          {tienda.total != null ? `${tienda.total} productos` : 'Ver productos'}
          {sitio && ` · ${sitio}`}
        </p>
        <span className="tile-cta">
          Descubrí <span aria-hidden="true">→</span>
        </span>
      </div>
    </a>
  )
}

// Portada cuando todavía no hay tiendas (el scraper no corrió)
function PortadaGenerica() {
  return (
    <div className="carrusel">
      <div className="slide-grupo activa" style={estiloTema(null)}>
        <a className="slide" href={'#' + rutaCatalogo()}>
          <div className="slide-contenido">
            <div className="slide-texto">
              <p className="slide-eyebrow">Agregador de ropa</p>
              <h2 className="slide-titulo">Catálogo</h2>
              <p className="slide-lema">Ropa y calzado deportivo de varias tiendas en un solo lugar.</p>
              <span className="slide-cta">
                Ver catálogo <span className="slide-cta-flecha" aria-hidden="true">→</span>
              </span>
            </div>
          </div>
        </a>
      </div>
    </div>
  )
}

// true si el sistema pide menos animaciones: el carrusel no pasa solo
function usePrefiereMenosMovimiento() {
  const consulta = '(prefers-reduced-motion: reduce)'
  const [reducir, setReducir] = useState(
    () => typeof window !== 'undefined' && !!window.matchMedia?.(consulta).matches
  )

  useEffect(() => {
    const mq = window.matchMedia?.(consulta)
    if (!mq) return
    const cambiar = () => setReducir(mq.matches)
    mq.addEventListener?.('change', cambiar)
    return () => mq.removeEventListener?.('change', cambiar)
  }, [])

  return reducir
}

function IconoFlecha({ izquierda = false }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {izquierda ? <path d="M15 5l-7 7 7 7" /> : <path d="M9 5l7 7-7 7" />}
    </svg>
  )
}

function IconoPausa() {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <rect x="6.5" y="5" width="3.5" height="14" rx="1" />
      <rect x="14" y="5" width="3.5" height="14" rx="1" />
    </svg>
  )
}

function IconoPlay() {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M8 5.5v13a1 1 0 0 0 1.5.86l10.5-6.5a1 1 0 0 0 0-1.72L9.5 4.64A1 1 0 0 0 8 5.5z" />
    </svg>
  )
}
