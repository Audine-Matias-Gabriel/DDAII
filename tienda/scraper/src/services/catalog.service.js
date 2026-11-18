const fs = require("fs/promises");
const path = require("path");
const { crearExtractor } = require("../factories/extractor.factory");
const { evaluar } = require("../normalizacion/normalizador");
const { publicarProductos } = require("../messaging/rabbitmq.publisher");

function archivoSalida(tienda, dirSalida) {
  return path.join(dirSalida, `${tienda.clave.toLowerCase()}.json`);
}

/** Normaliza, descarta los que no sirven y deduplica por idExterno. */
function normalizarCatalogo(crudos, tienda) {
  const porId = new Map();
  const descartes = { "sin categoría": 0, "sin precio": 0, "inválido": 0, duplicado: 0 };

  for (const crudo of crudos) {
    const { producto, motivo } = evaluar(crudo, tienda);
    if (!producto) {
      descartes[motivo] = (descartes[motivo] || 0) + 1;
    } else if (porId.has(producto.idExterno)) {
      descartes.duplicado += 1;
    } else {
      porId.set(producto.idExterno, producto);
    }
  }
  return { productos: [...porId.values()], descartes };
}

async function guardarSnapshot(tienda, productos, dirSalida) {
  const archivo = archivoSalida(tienda, dirSalida);
  await fs.mkdir(path.dirname(archivo), { recursive: true });
  const contenido = {
    generadoEn: new Date().toISOString(),
    tienda: { clave: tienda.clave, id: tienda.id, nombre: tienda.nombre, url: tienda.url },
    totalProductos: productos.length,
    productos
  };
  await fs.writeFile(archivo, JSON.stringify(contenido, null, 2) + "\n", "utf8");
  return archivo;
}

async function leerSnapshot(tienda, dirSalida) {
  const archivo = archivoSalida(tienda, dirSalida);
  const contenido = JSON.parse(await fs.readFile(archivo, "utf8"));
  if (!Array.isArray(contenido.productos)) {
    throw new Error(`${archivo} no tiene "productos".`);
  }
  return contenido.productos;
}

/**
 * Lee el catálogo de una tienda: extraer → normalizar → guardar output.
 * Si la extracción falla (sin internet, tienda caída) o no trae nada,
 * usa el último output/<clave>.json.
 */
async function obtenerCatalogo(tienda, config, deps = {}) {
  const crear = deps.crearExtractor || crearExtractor;
  const dirSalida = deps.dirSalida || config.dirSalida;
  const log = `[${tienda.nombre}]`;

  let crudos;
  try {
    crudos = await crear(tienda, config).extraer();
    if (!crudos.length) throw new Error("la tienda no devolvió productos");
  } catch (error) {
    console.warn(`${log} Falló la extracción: ${error.message}`);
    console.warn(`${log} usando snapshot`);
    const productos = await leerSnapshot(tienda, dirSalida);
    return { origen: "snapshot", productos, extraidos: 0, descartes: {} };
  }

  const { productos, descartes } = normalizarCatalogo(crudos, tienda);
  const detalle = Object.entries(descartes)
    .filter(([, n]) => n)
    .map(([motivo, n]) => `${motivo}: ${n}`)
    .join(", ");
  const totalDescartes = Object.values(descartes).reduce((a, b) => a + b, 0);
  console.log(
    `${log} Extraídos: ${crudos.length}. Descartados: ${totalDescartes}` +
      (detalle ? ` (${detalle})` : "") +
      `. Válidos: ${productos.length}`
  );

  if (productos.length) {
    const archivo = await guardarSnapshot(tienda, productos, dirSalida);
    console.log(`${log} Guardado ${archivo}`);
  }
  return { origen: "api", productos, extraidos: crudos.length, descartes };
}

/** Corre una tienda completa y publica. Devuelve el resumen. */
async function correrTienda(tienda, config, deps = {}) {
  const publicar = deps.publicar || publicarProductos;
  const catalogo = await obtenerCatalogo(tienda, config, deps);
  const confirmados = await publicar(catalogo.productos, tienda, config);
  console.log(`[${tienda.nombre}] Eventos confirmados: ${confirmados} (origen: ${catalogo.origen})`);
  return { ...catalogo, confirmados };
}

/** Corre las tiendas una por una; una tienda que falla no frena a las demás. */
async function correrTiendas(tiendas, config, deps = {}) {
  let fallas = 0;
  for (const tienda of tiendas) {
    try {
      await correrTienda(tienda, config, deps);
    } catch (error) {
      fallas += 1;
      console.error(`[${tienda.nombre}] Falló: ${error.message}`);
    }
  }
  return fallas;
}

module.exports = {
  normalizarCatalogo,
  obtenerCatalogo,
  correrTienda,
  correrTiendas,
  guardarSnapshot,
  leerSnapshot
};
