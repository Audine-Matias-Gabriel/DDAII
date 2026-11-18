// Corre 10 casos contra la API y evalúa cada resultado con una regla esperada.
const B = "http://localhost:8080";
const casos = [
  { id: 36,  e: "contenido",      esperado: "Remeras de mujer (o unisex), Puma o precio cercano" },
  { id: 279, e: "contenido",      esperado: "Remeras de hombre, NBA, precio cercano (~$25.000)" },
  { id: 5,   e: "contenido",      esperado: "Remeras/camisetas de hombre, adidas, precio alto" },
  { id: 153, e: "contenido",      esperado: "Pantalones/calzas de mujer, adidas" },
  { id: 58,  e: "contenido",      esperado: "Pantalones/shorts de hombre, adidas" },
  { id: 287, e: "contenido",      esperado: "Calzado de mujer, Nike, precio cercano (~$170.000)" },
  { id: 2,   e: "contenido",      esperado: "Calzado unisex o de cualquier género, Crocs o precio cercano" },
  { id: 147, e: "contenido",      esperado: "Pantalones unisex/niños, precio bajo" },
  { id: 36,  e: "complementaria", esperado: "Conjunto: un pantalón y un calzado de mujer (o unisex)" },
  { id: 1,   e: "complementaria", esperado: "Conjunto: una remera y un pantalón de hombre (o unisex)" }
];
const COMPL = { REMERA: ["PANTALON", "CALZADO"], PANTALON: ["REMERA", "CALZADO"], CALZADO: ["REMERA", "PANTALON"], VESTIDO: ["CALZADO"] };
const generoOk = (a, b) => a === b || a === "Unisex" || b === "Unisex";

(async () => {
  const filas = [];
  const crudo = [];
  let n = 0;
  let ok = 0;
  for (const c of casos) {
    n++;
    const p = await (await fetch(`${B}/api/productos/${c.id}`)).json();
    const r = await (await fetch(`${B}/api/recomendaciones?productoId=${c.id}&limite=4&estrategia=${c.e}`)).json();
    let pasa;
    let motivo;
    if (c.e === "contenido") {
      const malos = r.filter(x => x.categoria !== p.categoria || !generoOk(p.genero, x.genero));
      pasa = r.length === 4 && malos.length === 0;
      motivo = pasa ? "misma categoría y género compatible" : `${malos.length} fuera de categoría/género`;
    } else {
      const cats = new Set(r.map(x => x.categoria));
      const faltan = (COMPL[p.categoria] || []).filter(k => !cats.has(k));
      const malos = r.filter(x => x.categoria === p.categoria || !generoOk(p.genero, x.genero));
      pasa = faltan.length === 0 && malos.length === 0;
      motivo = pasa ? "trae todas las categorías que combinan" : `faltan ${faltan.join(",")} / ${malos.length} no combinan`;
    }
    if (pasa) ok++;
    const precioMedio = r.length
      ? Math.round(r.reduce((s, x) => s + Math.abs(x.precio - p.precio) / p.precio, 0) / r.length * 100)
      : null;
    const obtenido = r
      .map(x => `${x.nombre} (${x.categoria}, ${x.genero}, $${x.precio}, ${x.tienda && x.tienda.nombre}, ${x.puntaje})`)
      .join("<br>");
    filas.push(`| ${n} | ${p.nombre} — ${p.categoria}, ${p.genero}, ${p.marca}, $${p.precio} (${p.tienda.nombre}) | \`${c.e}\` | ${c.esperado} | ${obtenido} | ${pasa ? "✅" : "❌"} ${motivo}; dif. de precio media ${precioMedio} % |`);
    crudo.push({
      caso: n,
      estrategia: c.e,
      entrada: { id: p.id, nombre: p.nombre, categoria: p.categoria, genero: p.genero, marca: p.marca, precio: p.precio, tienda: p.tienda.nombre },
      esperado: c.esperado,
      obtenido: r,
      pasa
    });
  }
  require("fs").writeFileSync(process.argv[2], JSON.stringify({ fecha: new Date().toISOString(), aciertos: `${ok}/${n}`, casos: crudo }, null, 2) + "\n");
  require("fs").writeFileSync(process.argv[3], filas.join("\n") + "\n");
  console.log(`ACIERTOS ${ok}/${n}`);
  console.log(filas.join("\n"));
})();
