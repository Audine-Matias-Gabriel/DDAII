// Datos y utilidades de la landing: identidad visual de cada tienda y elección de
// los productos que se muestran. Sin JSX ni CSS para poder probarlo con Node (test.cjs).

// Colores tomados de la marca de cada tienda y textos propios de la landing.
const TEMAS = {
  sportline: {
    bajada: 'Zapatillas y ropa deportiva',
    lema: 'Running, training y lifestyle para moverte todos los días.',
    fondo: '#1f1f1f',
    fondo2: '#3b3b3b',
    texto: '#ffffff',
    acento: '#f2a93b',
    acentoTexto: '#1a1a1a',
    marca: '#f2a93b',
    marcaTexto: '#1a1a1a',
    tinte: '#f6efe3'
  },
  newsport: {
    bajada: 'Fútbol, running y training',
    lema: 'Camisetas, zapatillas y ropa para vivir el deporte.',
    fondo: '#e30613',
    fondo2: '#a3000a',
    texto: '#ffffff',
    acento: '#111111',
    acentoTexto: '#ffffff',
    marca: '#e30613',
    marcaTexto: '#ffffff',
    tinte: '#fdeeee'
  },
  grid: {
    bajada: 'Sneakers y streetwear',
    lema: 'Cultura urbana, básquet y las zapatillas del momento.',
    fondo: '#ffd200',
    fondo2: '#ffb800',
    texto: '#111111',
    acento: '#111111',
    acentoTexto: '#ffd200',
    marca: '#ffd200',
    marcaTexto: '#111111',
    tinte: '#fff8d9'
  }
}

const TEMA_GENERICO = {
  bajada: 'Ropa y calzado',
  lema: 'Ropa y calzado deportivo para todos los días.',
  fondo: '#0f172a',
  fondo2: '#1e3a8a',
  texto: '#ffffff',
  acento: '#60a5fa',
  acentoTexto: '#0f172a',
  marca: '#2563eb',
  marcaTexto: '#ffffff',
  tinte: '#eef2ff'
}

// Orden en la landing; las tiendas que no están en la lista van al final.
const ORDEN = ['sportline', 'newsport', 'grid']

// 'Sportline' -> 'sportline', 'New Sport' -> 'newsport'
export function claveTienda(nombre = '') {
  return String(nombre)
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '')
}

export function temaDeTienda(tienda) {
  return TEMAS[claveTienda(tienda?.nombre)] || TEMA_GENERICO
}

// Variables CSS del tema para usar en style={...}
export function estiloTema(tienda) {
  const t = temaDeTienda(tienda)
  return {
    '--t-fondo': t.fondo,
    '--t-fondo-2': t.fondo2,
    '--t-texto': t.texto,
    '--t-acento': t.acento,
    '--t-acento-texto': t.acentoTexto,
    '--t-marca': t.marca,
    '--t-marca-texto': t.marcaTexto,
    '--t-tinte': t.tinte
  }
}

export function ordenarTiendas(tiendas = []) {
  const posicion = (t) => {
    const i = ORDEN.indexOf(claveTienda(t.nombre))
    return i === -1 ? ORDEN.length : i
  }
  return [...tiendas].sort((a, b) => posicion(a) - posicion(b) || a.id - b.id)
}

// Elige los productos con imagen que se ven en la landing: primero una zapatilla
// (la imagen grande), después ropa para variar y más zapatillas para completar.
export function elegirDestacados(productos = [], cantidad = 4) {
  const conImagen = productos.filter((p) => p && p.imagenUrl)
  const zapatillas = conImagen.filter((p) => /zapatilla/i.test(p.nombre || ''))
  const ropa = conImagen.filter((p) => p.categoria && p.categoria !== 'CALZADO')
  const preferidos = [zapatillas[0], ropa[0], ropa[1], zapatillas[1], zapatillas[2], ...conImagen]
  const elegidos = []
  for (const p of preferidos) {
    if (elegidos.length === cantidad) break
    if (p && !elegidos.some((e) => e.id === p.id)) elegidos.push(p)
  }
  return elegidos
}

// 'ADIDAS ORIGINALS' -> 'Adidas Originals'; las siglas cortas quedan igual (NBA, MLB)
function nombreMarca(marca) {
  return marca
    .trim()
    .split(/\s+/)
    .map((p) => (p.length <= 3 && p === p.toUpperCase() ? p : p[0].toUpperCase() + p.slice(1).toLowerCase()))
    .join(' ')
}

// Las marcas que más aparecen entre los productos, sin repetir por mayúsculas.
export function marcasPrincipales(productos = [], cantidad = 3) {
  const cuenta = new Map()
  for (const p of productos) {
    if (!p?.marca || !p.marca.trim()) continue
    const nombre = nombreMarca(p.marca)
    const clave = nombre.toLowerCase()
    const actual = cuenta.get(clave) || { nombre, veces: 0 }
    actual.veces++
    cuenta.set(clave, actual)
  }
  return [...cuenta.values()]
    .sort((a, b) => b.veces - a.veces)
    .slice(0, cantidad)
    .map((m) => m.nombre)
}

// VTEX sirve la misma imagen en otro tamaño agregando -ancho-alto al id:
// /arquivos/ids/123/foto.jpg -> /arquivos/ids/123-600-600/foto.jpg
export function imagenVtex(url, lado) {
  if (!url) return url
  return url.replace(/(\/arquivos\/ids\/\d+)(-\d+-\d+)?\//, `$1-${lado}-${lado}/`)
}

const PESOS = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'ARS',
  maximumFractionDigits: 0
})

export function formatearPrecio(precio) {
  const n = Number(precio)
  return Number.isFinite(n) ? PESOS.format(n) : ''
}

// 'https://www.sportline.com.ar' -> 'sportline.com.ar'
export function dominio(url) {
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return ''
  }
}

// ['Sportline', 'Newsport', 'Grid'] -> 'Sportline, Newsport y Grid'
export function unirNombres(nombres = []) {
  if (nombres.length <= 1) return nombres.join('')
  return `${nombres.slice(0, -1).join(', ')} y ${nombres[nombres.length - 1]}`
}
