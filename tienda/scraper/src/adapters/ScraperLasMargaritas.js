const ScraperVtex = require("./ScraperVtex");

class ScraperLasMargaritas extends ScraperVtex {
  getLogPrefix() {
    return "[LasMargaritas]";
  }

  inferGender(product) {
    const text = [product.productName, product.description, ...(product.categories || [])]
      .join(" ").toLowerCase();
    if (/\b(hombre|masculin[oa]s?|men|man|caballero)\b/.test(text)) return "Hombre";
    if (/\b(niñ[oa]s?|infantil|kids|junior)\b/.test(text)) return "Infantil";
    if (/\bunisex\b/.test(text)) return "Unisex";
    // Tienda predominantemente femenina: si no hay marca explícita, asumimos Mujer.
    return "Mujer";
  }
}

module.exports = ScraperLasMargaritas;