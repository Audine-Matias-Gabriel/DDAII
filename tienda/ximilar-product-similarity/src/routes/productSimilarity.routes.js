
const express = require("express");
const router = express.Router();

const controller =
  require("../controllers/ProductSimilarityController");

router.post("/search", controller.search);

module.exports = router;
