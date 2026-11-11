
const XimilarClient = require("../clients/XimilarClient");

class ProductIndexerService {
  constructor() {
    this.ximilar = new XimilarClient();
  }

  async indexProduct(product) {
    if (!product.id || !product.imageUrl) {
      throw new Error("El producto necesita ID e imagen");
    }

    const result = await this.ximilar.insert([
      {
        _id: String(product.id),
        _url: product.imageUrl,
        product_id: String(product.id),
        product_name: product.name,
        store: product.store
      }
    ]);

    return result;
  }
}

module.exports = ProductIndexerService;
