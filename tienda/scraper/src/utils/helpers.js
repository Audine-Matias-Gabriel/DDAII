function cleanText(value) {
  return value == null ? "" : String(value).replace(/\s+/g, " ").trim();
}
function absoluteUrl(base, path) {
  if (!path) return "";
  try { return new URL(path, base).toString(); } catch { return ""; }
}
function sleep(ms) { return new Promise(resolve => setTimeout(resolve, ms)); }
module.exports = { cleanText, absoluteUrl, sleep };
