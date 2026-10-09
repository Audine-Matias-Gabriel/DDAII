import {
  claveItem,
  etiquetaGenero,
  generosDisponibles,
  primerGenero,
  primerTalleConStock,
  stockDe,
  stockTotal,
  tallesDe,
} from './variantes'
import { producto, stockPorGenero } from '@/test/test-utils'

describe('variantes', () => {
  it('generosDisponibles devuelve las claves del stock', () => {
    const p = producto({
      stock: stockPorGenero({ HOMBRE: { M: 1 }, MUJER: { M: 1 } }),
    })

    expect(generosDisponibles(p)).toEqual(['HOMBRE', 'MUJER'])
  })

  it('primerGenero prioriza el genero declarado si tiene stock', () => {
    const p = producto({
      genero: 'MUJER',
      stock: stockPorGenero({ HOMBRE: { M: 1 }, MUJER: { M: 1 } }),
    })

    expect(primerGenero(p)).toBe('MUJER')
  })

  it('primerGenero cae al primero cuando el declarado no esta en el stock', () => {
    const p = producto({
      genero: 'UNISEX',
      stock: stockPorGenero({ HOMBRE: { M: 1 } }),
    })

    expect(primerGenero(p)).toBe('HOMBRE')
  })

  it('primerGenero devuelve vacio sin stock', () => {
    expect(primerGenero(producto({ stock: stockPorGenero({}) }))).toBe('')
  })

  it('tallesDe y stockDe leen la celda de la variante', () => {
    const p = producto({ stock: stockPorGenero({ UNISEX: { S: 3, M: 0 } }) })

    expect(tallesDe(p, 'UNISEX')).toEqual(['S', 'M'])
    expect(stockDe(p, 'UNISEX', 'S')).toBe(3)
    expect(stockDe(p, 'UNISEX', 'M')).toBe(0)
    expect(stockDe(p, 'UNISEX', 'X')).toBe(0)
  })

  it('stockDe devuelve 0 sin genero o talle', () => {
    const p = producto()

    expect(stockDe(p, '', 'M')).toBe(0)
    expect(stockDe(p, 'UNISEX', '')).toBe(0)
  })

  it('stockTotal suma todas las celdas', () => {
    const p = producto({
      stock: stockPorGenero({
        HOMBRE: { S: 2, M: 3 },
        MUJER: { S: 1, M: 4 },
      }),
    })

    expect(stockTotal(p)).toBe(10)
  })

  it('primerTalleConStock saltea los talles en cero', () => {
    const p = producto({ stock: stockPorGenero({ UNISEX: { S: 0, M: 2 } }) })

    expect(primerTalleConStock(p, 'UNISEX')).toBe('M')
    expect(primerTalleConStock(p, 'HOMBRE')).toBe('')
  })

  it('claveItem distingue prenda, genero y talle', () => {
    expect(claveItem('5', 'HOMBRE', 'M')).toBe('5|HOMBRE|M')
    expect(claveItem('5', 'HOMBRE', 'M')).not.toBe(claveItem('5', 'MUJER', 'M'))
    expect(claveItem('5', 'HOMBRE', 'M')).not.toBe(claveItem('5', 'HOMBRE', 'L'))
  })

  it('etiquetaGenero traduce a texto legible', () => {
    expect(etiquetaGenero('HOMBRE')).toBe('Hombre')
    expect(etiquetaGenero('MUJER')).toBe('Mujer')
    expect(etiquetaGenero('UNISEX')).toBe('Unisex')
    expect(etiquetaGenero('INFANTIL')).toBe('Infantil')
  })
})
