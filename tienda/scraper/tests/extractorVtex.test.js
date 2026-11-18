const test = require("node:test");
const assert = require("node:assert/strict");
const ExtractorVtex = require("../src/extractores/ExtractorVtex");
const Extractor = require("../src/extractores/Extractor");

const tienda = { clave: "SPORTLINE", id: 1, nombre: "Sportline", plataforma: "VTEX", url: "https://www.sportline.com.ar/" };

/** Producto en el formato real de /api/catalog_system/pub/products/search. */
function productoVtex(id) {
  return {
    productId: String(id),
    productName: "Remera Running Dry Fit Mujer",
    brand: "Nike",
    description: "<p>Remera liviana</p>",
    link: `https://www.sportline.com.ar/remera-${id}/p`,
    categories: ["/MUJER/INDUMENTARIA/REMERAS/", "/MUJER/INDUMENTARIA/", "/MUJER/"],
    items: [
      {
        itemId: `${id}-M`,
        variations: ["Talle"],
        Talle: ["M"],
        images: [{ imageUrl: "https://sportline.vteximg.com.br/arquivos/ids/1/remera.jpg" }],
        sellers: [{ sellerDefault: true, commertialOffer: { Price: 25999, AvailableQuantity: 3, IsAvailable: true } }]
      },
      {
        itemId: `${id}-L`,
        variations: ["Talle"],
        Talle: ["L"],
        images: [],
        sellers: [{ sellerDefault: true, commertialOffer: { Price: 25999, AvailableQuantity: 0, IsAvailable: false } }]
      }
    ]
  };
}

function httpFalso(paginas) {
  const llamadas = [];
  return {
    llamadas,
    async get(url, { params }) {
      llamadas.push({ url, ...params });
      return { data: paginas[llamadas.length - 1] || [] };
    }
  };
}

test("Extractor es abstracta", () => {
  assert.throws(() => new Extractor(tienda), /abstracta/);
  class SinImplementar extends Extractor {}
  return assert.rejects(new SinImplementar(tienda).extraer(), /debe implementar extraer/);
});

test("convierte un producto VTEX a producto crudo con un SKU por item", () => {
  const extractor = new ExtractorVtex(tienda, { httpClient: httpFalso([]) });
  const crudo = extractor.aCrudo(productoVtex(2024612));

  assert.equal(crudo.idExterno, "2024612");
  assert.equal(crudo.nombre, "Remera Running Dry Fit Mujer");
  assert.equal(crudo.marca, "Nike");
  assert.equal(crudo.urlOriginal, "https://www.sportline.com.ar/remera-2024612/p");
  assert.equal(crudo.imagenUrl, "https://sportline.vteximg.com.br/arquivos/ids/1/remera.jpg");
  assert.deepEqual(crudo.rutasCategoria, productoVtex(1).categories);
  assert.deepEqual(crudo.skus, [
    { talle: "M", precio: 25999, disponible: true },
    { talle: "L", precio: 25999, disponible: false }
  ]);
});

test("arma la URL absoluta desde un link relativo", () => {
  const extractor = new ExtractorVtex(tienda, { httpClient: httpFalso([]) });
  const raw = { ...productoVtex(1), link: "/remera-1/p" };
  assert.equal(extractor.aCrudo(raw).urlOriginal, "https://www.sportline.com.ar/remera-1/p");
});

test("lee talles del formato viejo { name, values } como respaldo", () => {
  assert.deepEqual(
    ExtractorVtex.tallesDeItem({ variations: [{ name: "Talle", values: ["40", "41"] }] }),
    ["40", "41"]
  );
  assert.deepEqual(ExtractorVtex.tallesDeItem({ variations: ["Color"], Color: ["Rojo"] }), []);
});

test("pagina contra la API de la tienda hasta una página vacía", async () => {
  const http = httpFalso([
    [productoVtex(1), productoVtex(2)],
    [productoVtex(3), productoVtex(4)],
    []
  ]);
  const extractor = new ExtractorVtex(tienda, {
    httpClient: http,
    limites: { tamanoPagina: 2, maxProductos: 10, demoraMs: 0 }
  });

  const crudos = await extractor.extraer();

  assert.deepEqual(crudos.map(c => c.idExterno), ["1", "2", "3", "4"]);
  assert.equal(http.llamadas.length, 3);
  assert.equal(http.llamadas[0].url, "https://www.sportline.com.ar/api/catalog_system/pub/products/search");
  assert.deepEqual(
    http.llamadas.map(l => [l._from, l._to]),
    [[0, 1], [2, 3], [4, 5]]
  );
});

test("respeta maxProductos", async () => {
  const http = httpFalso([[productoVtex(1), productoVtex(2)], [productoVtex(3)]]);
  const extractor = new ExtractorVtex(tienda, {
    httpClient: http,
    limites: { tamanoPagina: 2, maxProductos: 3, demoraMs: 0 }
  });
  const crudos = await extractor.extraer();
  assert.equal(crudos.length, 3);
  assert.deepEqual(http.llamadas.map(l => [l._from, l._to]), [[0, 1], [2, 2]]);
});

test("falla si la API no devuelve un array", async () => {
  const extractor = new ExtractorVtex(tienda, {
    httpClient: { get: async () => ({ data: "<html>" }) },
    limites: { demoraMs: 0 }
  });
  await assert.rejects(extractor.extraer(), /no es un array/);
});
