// https://www.sportline.com.ar/
// https://www.lasmargaritas.com.ar/
require("dotenv").config();

const axios = require("axios");

async function testProductSimilarity() {
  try {
    const token = process.env.XIMILAR_API_TOKEN;
    const collectionId = process.env.XIMILAR_COLLECTION_ID;

    if (!token || !collectionId) {
      throw new Error("Faltan XIMILAR_API_TOKEN o XIMILAR_COLLECTION_ID en .env");
    }

    // Obtener productos de Sportline
    const catalogResponse = await axios.get(
      "https://www.sportline.com.ar/api/catalog_system/pub/products/search?_from=0&_to=9"
    );

    const products = catalogResponse.data;

    const product = products.find(
      p => p.items?.[0]?.images?.[0]?.imageUrl
    );

    if (!product) {
      throw new Error("No se encontró una imagen de producto en Sportline");
    }

    const imageUrl = "https://sportline.vteximg.com.br/arquivos/ids/2015409/015070GC214BD33_1.jpg.jpg?v=639230932564170000";

    console.log("Producto de prueba:", product.productName);
    console.log("Imagen:", imageUrl);

    // Consultar Product Similarity
    const response = await axios.post(
      "https://api.ximilar.com/similarity/products-new/v2/visualKNN",
      {
        query_record: {
          _url: imageUrl
        },
        k: 5,
        fields_to_return: [
          "_id",
          "_url",
          "product_id"
        ]
      },
      {
        headers: {
          Authorization: `Token ${token}`,
          "collection-id": collectionId,
          "Content-Type": "application/json"
        }
      }
    );

    console.log("\nResultados de Product Similarity:");
    console.log(JSON.stringify(response.data, null, 2));

  } catch (error) {
    console.error(
      "Error:",
      error.response?.data || error.message
    );
  }
}

testProductSimilarity();
