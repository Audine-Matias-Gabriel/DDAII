function createTienda({ id, nombre, url }) {
  return {
    id: Number(id),
    nombre: String(nombre),
    url: String(url).replace(/\/$/, "")
  };
}
module.exports = { createTienda };
