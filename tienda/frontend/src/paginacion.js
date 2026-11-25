// "Mostrar más productos": el catálogo pide de a TAMANO_PAGINA productos y los va sumando.

export const TAMANO_PAGINA = 20

// Suma la página nueva al final de la lista sin repetir productos (por id).
export function unirProductos(actuales = [], nuevos = []) {
  const ids = new Set(actuales.map((p) => p.id))
  return [...actuales, ...nuevos.filter((p) => !ids.has(p.id))]
}

// ¿Quedan productos por pedir después de la página `pagina`? (la primera es la 0)
export function hayMasPaginas(pagina, total, tamano = TAMANO_PAGINA) {
  return (pagina + 1) * tamano < total
}
