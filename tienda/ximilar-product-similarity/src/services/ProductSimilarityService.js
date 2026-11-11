
const XimilarClient = require("../clients/XimilarClient");

class ProductSimilarityService {
  constructor() {
    this.ximilar = new XimilarClient();
  }

  async search(imageUrl) {
    if (!imageUrl) {
      throw new Error("Se requiere una imagen para buscar");
    }

    const result = await this.ximilar.visualSearch(imageUrl, 5);

    return {
      success: true,
      total: result.answer_count || 0,

      results: (result.answer_records || []).map(
        (record, index) => ({
          productId: record.product_id || record._id,
          imageUrl: record._url,
          distance: result.answer_distances?.[index] ?? null
        })
      )
    };
  }
}

module.exports = ProductSimilarityService;
