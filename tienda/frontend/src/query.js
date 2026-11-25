// Arma el query string para la API descartando los filtros vacíos.
// Sin esto, URLSearchParams convierte undefined en el texto "undefined"
// y el backend termina buscando literalmente "undefined".
export function armarQuery(params = {}) {
  const limpios = Object.entries(params).filter(
    ([, valor]) => valor !== undefined && valor !== null && valor !== ''
  )
  return new URLSearchParams(limpios).toString()
}

// Filtros del catálogo que se pueden pasar en la URL: #/catalogo?tienda=1&genero=Mujer
export const FILTROS_CATALOGO = ['texto', 'categoria', 'genero', 'tienda', 'precioMin', 'precioMax']

// Lee del query string ('tienda=1&genero=Mujer') solo los filtros conocidos del catálogo.
export function filtrosDesdeQuery(query = '') {
  const params = new URLSearchParams(query)
  const filtros = {}
  for (const campo of FILTROS_CATALOGO) {
    const valor = params.get(campo)
    if (valor) filtros[campo] = valor
  }
  return filtros
}

// Ruta del catálogo con los filtros aplicados: rutaCatalogo({ tienda: 1 }) -> '/catalogo?tienda=1'
export function rutaCatalogo(filtros = {}) {
  const conocidos = Object.fromEntries(
    Object.entries(filtros).filter(([campo]) => FILTROS_CATALOGO.includes(campo))
  )
  const query = armarQuery(conocidos)
  return query ? `/catalogo?${query}` : '/catalogo'
}
