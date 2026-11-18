// Usuario pertenece a KNOWFIT; no se extrae del catálogo de Sportline.
function createUsuario({ id = null, nombre, email, premium = false }) {
  return { id, nombre: String(nombre || ""), email: String(email || ""), premium: Boolean(premium) };
}
module.exports = { createUsuario };
