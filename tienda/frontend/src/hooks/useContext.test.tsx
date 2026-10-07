import { renderHook } from '@testing-library/react'
import { useAuth } from './useAuth'
import { useCarrito } from './useCarrito'

describe('hooks de contexto fuera del provider', () => {
  let errorSpy: ReturnType<typeof vi.spyOn>

  beforeEach(() => {
    errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined)
  })

  afterEach(() => {
    errorSpy.mockRestore()
  })

  it('useAuth lanza si no hay AuthProvider', () => {
    expect(() => renderHook(() => useAuth())).toThrow(/AuthProvider/)
  })

  it('useCarrito lanza si no hay CarritoProvider', () => {
    expect(() => renderHook(() => useCarrito())).toThrow(/CarritoProvider/)
  })
})
