const formatoMoneda = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'ARS',
  maximumFractionDigits: 0,
})

const formatoFecha = new Intl.DateTimeFormat('es-AR', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
})

export function formatMoneda(valor: number): string {
  return formatoMoneda.format(valor)
}

export function formatFecha(iso: string): string {
  return formatoFecha.format(new Date(iso))
}

export function iniciales(nombre: string): string {
  return nombre
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase() ?? '')
    .join('')
}

/** Elige un color estable a partir de un texto, para los placeholders sin imagen. */
export function colorDesdeTexto(texto: string): string {
  const paleta = ['var(--color-avatar-1)', 'var(--color-avatar-2)', 'var(--color-avatar-3)', 'var(--color-avatar-4)']
  let suma = 0
  for (let i = 0; i < texto.length; i++) {
    suma += texto.charCodeAt(i)
  }
  return paleta[suma % paleta.length]
}
