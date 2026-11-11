
require("dotenv").config();

const express = require("express");
const app = express();

app.use(express.json());

app.use(
  "/api/visual-search",
  require("./src/routes/productSimilarity.routes")
);

const PORT = process.env.PORT || 3010;

app.listen(PORT, () => {
  console.log(`Ximilar module running on port ${PORT}`);
});
