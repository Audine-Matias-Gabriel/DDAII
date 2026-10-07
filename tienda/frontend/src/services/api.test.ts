import { ApiError, apiGet, apiPost } from './api'

const fetchMock = vi.fn()

beforeEach(() => {
  fetchMock.mockReset()
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

function respuestaJson(body: unknown, status = 200, statusText = 'OK') {
  return new Response(JSON.stringify(body), {
    status,
    statusText,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('apiGet', () => {
  it('devuelve el JSON cuando la respuesta es OK', async () => {
    fetchMock.mockResolvedValue(respuestaJson([{ id: 1 }]))

    await expect(apiGet('/api/productos')).resolves.toEqual([{ id: 1 }])
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/api/productos'),
      undefined,
    )
  })
})

describe('apiPost', () => {
  it('manda POST con JSON y devuelve la respuesta', async () => {
    fetchMock.mockResolvedValue(respuestaJson({ id: 15 }))

    await expect(apiPost('/api/pedidos', { detalles: [] })).resolves.toEqual({ id: 15 })

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toContain('/api/pedidos')
    expect(init.method).toBe('POST')
    expect(init.headers).toEqual({ 'Content-Type': 'application/json' })
    expect(JSON.parse(String(init.body))).toEqual({ detalles: [] })
  })
})

describe('errores', () => {
  it('usa el mensaje { error } del backend', async () => {
    fetchMock.mockResolvedValue(respuestaJson({ error: 'Stock insuficiente' }, 409, 'Conflict'))

    const error = await apiGet('/api/productos/5').catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({
      name: 'ApiError',
      status: 409,
      message: 'Stock insuficiente',
    })
  })

  it('cae a un mensaje generico si el body no es JSON', async () => {
    fetchMock.mockResolvedValue(new Response('boom', { status: 500, statusText: 'Server Error' }))

    await expect(apiGet('/api/productos')).rejects.toMatchObject({
      status: 500,
      message: 'Error 500 (Server Error)',
    })
  })

  it('cae a un mensaje generico si el body no trae el campo error', async () => {
    fetchMock.mockResolvedValue(respuestaJson({ mensaje: 'nope' }, 400, 'Bad Request'))

    await expect(apiGet('/api/productos')).rejects.toMatchObject({
      status: 400,
      message: 'Error 400 (Bad Request)',
    })
  })

  it('devuelve ApiError con status 0 si fetch falla', async () => {
    fetchMock.mockRejectedValue(new TypeError('Failed to fetch'))

    const error = await apiGet('/api/productos').catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 0 })
    expect((error as ApiError).message).toContain('No se pudo conectar')
  })
})
