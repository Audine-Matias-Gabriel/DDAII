const ScraperVtex = require("./ScraperVtex");

class ScraperSportline extends ScraperVtex {
  getLogPrefix() {
    return "[Sportline]";
  }
}

module.exports = ScraperSportline;