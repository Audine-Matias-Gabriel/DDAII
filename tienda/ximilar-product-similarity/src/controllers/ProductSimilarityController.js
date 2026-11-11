
const ProductSimilarityService =
  require("../services/ProductSimilarityService");

const similarityService = new ProductSimilarityService();

async function search(req, res) {
  try {
    const { imageUrl } = req.body;

    const result = await similarityService.search(imageUrl);

    res.status(200).json(result);

  } catch (error) {
    console.error("Visual Search Error:", error.message);

    res.status(502).json({
      success: false,
      message: "No se pudo realizar la búsqueda visual"
    });
  }
}

module.exports = { search };
