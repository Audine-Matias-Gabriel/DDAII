/** Colapsa espacios y recorta. null/undefined → "". */
function limpiar(valor) {
  return valor == null ? "" : String(valor).replace(/\s+/g, " ").trim();
}

/** Para comparar: minúsculas, sin tildes (ñ → n) y espacios colapsados. */
function normalizarTexto(valor) {
  return limpiar(valor)
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .toLowerCase();
}

/** Saca etiquetas HTML y entidades comunes (las descripciones de VTEX traen HTML). */
function quitarHtml(valor) {
  return limpiar(
    String(valor ?? "")
      .replace(/<[^>]*>/g, " ")
      .replace(/&nbsp;/gi, " ")
      .replace(/&amp;/gi, "&")
      .replace(/&lt;/gi, "<")
      .replace(/&gt;/gi, ">")
      .replace(/&quot;/gi, '"')
      .replace(/&#39;/gi, "'")
  );
}

function recortar(valor, max) {
  const texto = limpiar(valor);
  return texto.length > max ? texto.slice(0, max).trim() : texto;
}

/** URL absoluta respecto de `base`; "" si no se puede armar. */
function urlAbsoluta(base, ruta) {
  if (!ruta) return "";
  try {
    return new URL(ruta, base).toString();
  } catch {
    return "";
  }
}

module.exports = { limpiar, normalizarTexto, quitarHtml, recortar, urlAbsoluta };
