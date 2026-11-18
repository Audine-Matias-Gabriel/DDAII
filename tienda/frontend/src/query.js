// Arma el query string para la API descartando los filtros vacíos.
// Sin esto, URLSearchParams convierte undefined en el texto "undefined"
// y el backend termina buscando literalmente "undefined".
export function armarQuery(params = {}) {
  const limpios = Object.entries(params).filter(
    ([, valor]) => valor !== undefined && valor !== null && valor !== ''
  )
  return new URLSearchParams(limpios).toString()
}
