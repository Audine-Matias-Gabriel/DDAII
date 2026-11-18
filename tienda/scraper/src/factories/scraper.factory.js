const ScraperSportline = require("../adapters/ScraperSportline");
const ScraperLasMargaritas = require("../adapters/ScraperLasMargaritas");

const strategies = {
  SPORTLINE: ScraperSportline,
  LASMARGARITAS: ScraperLasMargaritas
};

function createScraper(storeAdapter, config) {
  const Strategy = strategies[String(storeAdapter).toUpperCase()];

  if (!Strategy) {
    throw new Error(
      `Scraper no registrado: ${storeAdapter}. Disponibles: ${Object.keys(strategies).join(", ")}`
    );
  }

  return new Strategy(config);
}

module.exports = { createScraper };