
require("dotenv").config();
const axios = require("axios");

// CONFIGURACIÓN
const VTEX_URL =
  "https://www.sportline.com.ar/api/catalog_system/pub/products/search";

const XIMILAR_URL =
  "https://api.ximilar.com/similarity/products-new/v2/insert";

const PRODUCTS_TO_INSERT = 10;

// Producto que ya habías cargado anteriormente.
// Lo excluimos para buscar otros productos.
const EXCLUDED_IDS = ["2024612"];

async function getSportlineProducts() {
  console.log("Consultando catálogo de Sportline...");

  const response = await axios.get(VTEX_URL, {
    params: {
      _from: 50,
      _to: 99
    },
    timeout: 30000
  });

  return response.data;
}

function extractProducts(products) {
  const selected = [];
  const usedIds = new Set(EXCLUDED_IDS);

  for (const product of products) {
    const productId = String(product.productId || "");

    if (!productId || usedIds.has(productId)) {
      continue;
    }

    // Obtener imágenes de los artículos/variantes del producto.
    const images = (product.items || [])
      .flatMap(item => item.images || [])
      .map(image => image.imageUrl)
      .filter(Boolean);

    if (images.length === 0) {
      continue;
    }

    // Una imagen principal por producto.
    selected.push({
      _id: productId,
      _url: images[0],
      name: product.productName || "Producto Sportline",
      brand: product.brand || "Sportline"
    });

    usedIds.add(productId);

    if (selected.length >= PRODUCTS_TO_INSERT) {
      break;
    }
  }

  return selected;
}

async function insertProducts(records) {
  console.log("\nEnviando productos a Ximilar...");

  const response = await axios.post(
    XIMILAR_URL,
    { records },
    {
      headers: {
        Authorization: `Token ${process.env.XIMILAR_API_TOKEN}`,
        "collection-id": process.env.XIMILAR_COLLECTION_ID,
        "Content-Type": "application/json"
      },
      timeout: 60000
    }
  );

  return response.data;
}

async function main() {
  try {
    // Comprobar credenciales.
    if (
      !process.env.XIMILAR_API_TOKEN ||
      !process.env.XIMILAR_COLLECTION_ID
    ) {
      throw new Error(
        "Faltan XIMILAR_API_TOKEN o XIMILAR_COLLECTION_ID en .env"
      );
    }

    // 1. Obtener catálogo.
    const products = await getSportlineProducts();

    // 2. Seleccionar productos diferentes.
    const records = extractProducts(products);

    console.log(`\nProductos encontrados: ${records.length}`);

    if (records.length < PRODUCTS_TO_INSERT) {
      throw new Error(
        `Solo se encontraron ${records.length} productos válidos. ` +
        "No se enviaron los productos a Ximilar."
      );
    }

    // 3. Mostrar productos seleccionados.
    console.log("\nProductos seleccionados:");

    records.forEach((product, index) => {
      console.log(`${index + 1}. ${product.name}`);
      console.log(`   ID: ${product._id}`);
      console.log(`   Imagen: ${product._url}`);
    });

    // 4. Insertar en Ximilar.
    const result = await insertProducts(records);

    console.log("\nRespuesta de Ximilar:");
    console.log(JSON.stringify(result, null, 2));

    // 5. Resumen.
    console.log("\n========== RESUMEN ==========");
    console.log("Productos enviados:", records.length);
    console.log(
      "Productos procesados:",
      result.answer_count ?? "No informado"
    );
    console.log(
      "Productos omitidos:",
      result.skipped_records?.length ?? 0
    );

    if (result.skipped_records?.length) {
      console.log("\nProductos omitidos:");
      result.skipped_records.forEach(product => {
        console.log(
          `ID: ${product._id} | Motivo: ${product.reason}`
        );
      });
    }

    console.log("\nProceso finalizado.");

  } catch (error) {
    console.error("\nError durante la carga:");

    if (error.response) {
      console.error("HTTP:", error.response.status);
      console.error(
        JSON.stringify(error.response.data, null, 2)
      );
    } else {
      console.error(error.message);
    }
  }
}

main();
