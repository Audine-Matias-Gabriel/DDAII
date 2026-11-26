import { armarQuery } from './query.js'

const API_BASE =import.meta.env.VITE_API_URL || ''

function getToken() {
  if (typeof localStorage === 'undefined') return null
  return localStorage.getItem('token')
}

function authHeaders() {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function handleResponse(r) {
  if (!r.ok) {
    const err = await r.json().catch(() => ({}))
    // El backend responde { estado, error, mensaje, ruta }: "mensaje" es el texto para el usuario
    throw new Error(err.mensaje || err.error || err.message || `Error ${r.status}`)
  }
  return r.json()
}

export const api = {
  // Productos
  async getProductos(params = {}) {
    const search = armarQuery(params)
    const r = await fetch(`${API_BASE}/api/productos${search ? `?${search}` : ''}`)
    return handleResponse(r)
  },

  async getTiendas() {
    const r = await fetch(`${API_BASE}/api/tiendas`)
    return handleResponse(r)
  },

  async getProducto(id) {
    const r = await fetch(`${API_BASE}/api/productos/${id}`)
    return handleResponse(r)
  },

  async getRecomendaciones(productoId, limite = 4, estrategia) {
    const estrategiaParam = estrategia ? `&estrategia=${encodeURIComponent(estrategia)}` : ''
    // Con token, el backend personaliza por marcas/categorías de interés (si el usuario es premium).
    const r = await fetch(
      `${API_BASE}/api/recomendaciones?productoId=${productoId}&limite=${limite}${estrategiaParam}`,
      { headers: authHeaders() }
    )
    return handleResponse(r)
  },

  // Registra una vista para personalizar recomendaciones futuras (solo premium en el backend).
  async registrarVista(productoId) {
    const r = await fetch(`${API_BASE}/api/recomendaciones/vistas/${productoId}`, {
      method: 'POST',
      headers: authHeaders()
    })
    if (!r.ok) return handleResponse(r)
    return r.status === 204 ? null : r.json().catch(() => null)
  },

  async getSimilares(id, limite = 4) {
    const r = await fetch(`${API_BASE}/api/productos/${id}/similares?limite=${limite}`)
    return handleResponse(r)
  },

  // Serie temporal de precios (snapshots diarios). El gráfico solo se muestra a premium.
  async getHistorialPrecios(id) {
    const r = await fetch(`${API_BASE}/api/productos/${id}/historial-precios`)
    return handleResponse(r)
  },

  // Búsqueda visual premium (multipart: el navegador pone el boundary de Content-Type)
  async buscarVisual(file, limite = 8) {
    const form = new FormData()
    form.append('imagen', file)
    form.append('limite', String(limite))
    const r = await fetch(`${API_BASE}/api/premium/busqueda-visual`, {
      method: 'POST',
      headers: authHeaders(),
      body: form
    })
    return handleResponse(r)
  },

  // Auth
  async register({ nombre, email, password }) {
    const r = await fetch(`${API_BASE}/api/auth/registro`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre, email, password })
    })
    return handleResponse(r)
  },

  async login({ email, password }) {
    const r = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
    return handleResponse(r)
  },

  async logout() {
    const r = await fetch(`${API_BASE}/api/auth/logout`, {
      method: 'POST',
      headers: authHeaders()
    })
    return handleResponse(r)
  },

  async me() {
    const r = await fetch(`${API_BASE}/api/auth/me`, {
      headers: authHeaders()
    })
    return handleResponse(r)
  },

  // Favoritos (solo usuarios registrados)
  async getFavoritos() {
    const r = await fetch(`${API_BASE}/api/favoritos`, {
      headers: authHeaders()
    })
    return handleResponse(r)
  },

  async addFavorito(productoId) {
    const r = await fetch(`${API_BASE}/api/favoritos/${productoId}`, {
      method: 'POST',
      headers: authHeaders()
    })
    return handleResponse(r)
  },

  async removeFavorito(productoId) {
    const r = await fetch(`${API_BASE}/api/favoritos/${productoId}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
    return handleResponse(r)
  },

  // Suscripciones / Premium
  async createSuscripcion(usuarioId) {
    const r = await fetch(`${API_BASE}/api/suscripciones`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...authHeaders()
      },
      body: JSON.stringify({ usuarioId })
    })
    return handleResponse(r)
  },

  // Precio del plan premium: el mismo que se cobra en Mercado Pago (público)
  async getPlanPremium() {
    const r = await fetch(`${API_BASE}/api/suscripciones/plan`)
    return handleResponse(r)
  },

  // Busca en Mercado Pago pagos de las suscripciones pendientes del usuario
  // (en localhost Mercado Pago no vuelve solo a /pago/exito)
  async verificarSuscripciones() {
    const r = await fetch(`${API_BASE}/api/suscripciones/verificar`, {
      method: 'POST',
      headers: authHeaders()
    })
    return handleResponse(r)
  },

  // Confirma el pago al volver de Mercado Pago (/pago/exito)
  async confirmarSuscripcion(suscripcionId, paymentId) {
    const id = encodeURIComponent(suscripcionId)
    const pago = encodeURIComponent(paymentId)
    const r = await fetch(`${API_BASE}/api/suscripciones/${id}/confirmar?paymentId=${pago}`, {
      method: 'POST',
      headers: authHeaders()
    })
    return handleResponse(r)
  }
}
