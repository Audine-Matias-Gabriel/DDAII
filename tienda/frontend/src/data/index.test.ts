import { TIENDAS, obtenerTienda } from './index'

describe('obtenerTienda', () => {
  it('devuelve la tienda cuando el id existe', () => {
    const tienda = obtenerTienda('1')

    expect(tienda?.nombre).toBe('Indumentaria Sur')
  })

  it('devuelve undefined cuando el id no existe', () => {
    expect(obtenerTienda('999')).toBeUndefined()
  })
})

describe('TIENDAS', () => {
  it('tiene 4 tiendas con ids string "1".."4"', () => {
    expect(TIENDAS).toHaveLength(4)
    expect(TIENDAS.map((tienda) => tienda.id)).toEqual(['1', '2', '3', '4'])
  })
})
