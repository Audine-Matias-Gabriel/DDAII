const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  readonly status: number

  constructor(mensaje: string, status: number) {
    super(mensaje)
    this.name = 'ApiError'
    this.status = status
  }
}

/** El backend responde los errores como { "error": "..." }. */
async function mensajeDeError(response: Response): Promise<string> {
  try {
    const body: unknown = await response.json()
    if (body && typeof body === 'object' && 'error' in body) {
      const detalle = (body as { error: unknown }).error
      if (typeof detalle === 'string' && detalle.length > 0) {
        return detalle
      }
    }
  } catch {
    // El body no era JSON: se cae al mensaje genérico de abajo.
  }
  return `Error ${response.status} (${response.statusText || 'sin detalle'})`
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response

  try {
    response = await fetch(`${API_BASE}${path}`, init)
  } catch {
    throw new ApiError(
      `No se pudo conectar con el backend en ${API_BASE}. ¿Está spring-boot:run levantado?`,
      0,
    )
  }

  if (!response.ok) {
    throw new ApiError(await mensajeDeError(response), response.status)
  }

  return (await response.json()) as T
}

export function apiGet<T>(path: string): Promise<T> {
  return request<T>(path)
}

export function apiPost<T>(path: string, body: unknown): Promise<T> {
  return request<T>(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}