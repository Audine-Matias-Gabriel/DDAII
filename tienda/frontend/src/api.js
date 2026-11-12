const API_BASE = import.meta.env.VITE_API_URL || ''

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
    throw new Error(err.error || err.message || `Error ${r.status}`)
  }
  return r.json()
}

export const api = {
  // Productos
  async getProductos(params = {}) {
    const search = new URLSearchParams(params).toString()
    const r = await fetch(`${API_BASE}/api/productos${search ? `?${search}` : ''}`)
    return handleResponse(r)
  },

  async getProducto(id) {
    const r = await fetch(`${API_BASE}/api/productos/${id}`)
    return handleResponse(r)
  },

  async getRecomendaciones(productoId, limite = 4) {
    const r = await fetch(`${API_BASE}/api/recomendaciones?productoId=${productoId}&limite=${limite}`)
    return handleResponse(r)
  },

  async getSimilares(id, limite = 4) {
    const r = await fetch(`${API_BASE}/api/productos/${id}/similares?limite=${limite}`)
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
  }
}
