
const axios = require("axios");

class XimilarClient {
  constructor() {
    this.baseURL =
      "https://api.ximilar.com/similarity/products-new/v2";

    this.token = process.env.XIMILAR_API_TOKEN;
    this.collectionId = process.env.XIMILAR_COLLECTION_ID;

    if (!this.token || !this.collectionId) {
      throw new Error("Faltan las credenciales de Ximilar");
    }

    this.http = axios.create({
      baseURL: this.baseURL,
      timeout: 15000,
      headers: {
        Authorization: `Token ${this.token}`,
        "collection-id": this.collectionId,
        "Content-Type": "application/json"
      }
    });
  }

  async insert(records) {
    const response = await this.http.post("/insert", {
      records
    });

    return response.data;
  }

  async visualSearch(imageUrl, limit = 5) {
    const response = await this.http.post("/visualKNN", {
      query_record: {
        _url: imageUrl
      },
      k: limit,
      fields_to_return: [
        "_id",
        "_url",
        "product_id"
      ]
    });

    return response.data;
  }
}

module.exports = XimilarClient;
