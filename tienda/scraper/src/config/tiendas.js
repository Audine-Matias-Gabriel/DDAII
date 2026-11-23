/**
 * Tiendas que lee el scraper. Son datos, no código: agregar una tienda VTEX
 * es agregar una entrada acá. Otra plataforma necesita un extractor nuevo
 * (ver src/factories/extractor.factory.js).
 *
 * Campos:
 *   clave       identificador para TIENDAS y --tienda (mayúsculas)
 *   id          tiendaId del evento ProductoScrapeado
 *   nombre      nombre de la tienda en el backend (el upsert la busca por nombre)
 *   plataforma  elige el extractor (VTEX)
 *   url         dominio de la tienda; para VTEX la URL de la API se arma sola
 *   activa      si corre cuando TIENDAS no está definida
 * Opcionales:
 *   generoPorDefecto  género cuando el producto no dice nada (por defecto Unisex)
 *   maxProductos      pisa MAX_PRODUCTS para esta tienda
 */
module.exports = [
  {
    clave: "SPORTLINE",
    id: 1,
    nombre: "Sportline",
    plataforma: "VTEX",
    url: "https://www.sportline.com.ar",
    activa: true
  },
  {
    clave: "GRID",
    id: 2,
    nombre: "Grid",
    plataforma: "VTEX",
    url: "https://www.grid.com.ar",
    activa: true
  },
  {
    clave: "NEWSPORT",
    id: 3,
    nombre: "Newsport",
    plataforma: "VTEX",
    url: "https://www.newsport.com.ar",
    activa: true
  }
];
