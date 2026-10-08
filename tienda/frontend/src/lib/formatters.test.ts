import { colorDesdeTexto, formatFecha, formatMoneda, iniciales } from './formatters'

describe('formatMoneda', () => {
  it('formatea en pesos argentinos sin decimales', () => {
    expect(formatMoneda(25000)).toContain('$')
    expect(formatMoneda(25000)).toMatch(/25\.000/)
  })

  it('formatea el cero', () => {
    expect(formatMoneda(0)).toMatch(/\$.*0/)
  })
})

describe('formatFecha', () => {
  it('formatea una fecha ISO como dd/mm/aaaa', () => {
    expect(formatFecha('2024-03-15T12:00:00')).toBe('15/03/2024')
  })
})

describe('iniciales', () => {
  it('toma la inicial de las dos primeras palabras', () => {
    expect(iniciales('Juan Perez')).toBe('JP')
  })

  it('devuelve vacio si el nombre esta vacio', () => {
    expect(iniciales('')).toBe('')
  })

  it('ignora espacios sobrantes y usa como maximo dos palabras', () => {
    expect(iniciales('  ana  maria lopez ')).toBe('AM')
  })
})

describe('colorDesdeTexto', () => {
  it('es determinista para el mismo texto', () => {
    expect(colorDesdeTexto('Remera Nike')).toBe(colorDesdeTexto('Remera Nike'))
  })

  it('devuelve un color de la paleta', () => {
    expect(colorDesdeTexto('Remera Nike')).toMatch(/var\(--color-avatar-[1-4]\)/)
  })
})
