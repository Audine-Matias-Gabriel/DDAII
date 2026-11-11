'use strict';

const fs = require('fs');
const path = require('path');

let passed = 0;
let failed = 0;

function test(name, fn) {
  try {
    fn();
    console.log('✓', name);
    passed++;
  } catch (err) {
    console.error('✗', name, '-', err.message);
    failed++;
  }
}

function assert(cond, msg) {
  if (!cond) throw new Error(msg || 'assert failed');
}

// --- Pruebas del frontend mínimo (P1) ---
test('existe index.html', () => {
  assert(fs.existsSync(path.join(__dirname, 'index.html')), 'index.html no existe');
});

test('existe script.js', () => {
  assert(fs.existsSync(path.join(__dirname, 'script.js')), 'script.js no existe');
});

test('index.html contiene sección de catálogo', () => {
  const html = fs.readFileSync(path.join(__dirname, 'index.html'), 'utf8');
  assert(html.includes('catalogo') || html.includes('producto'), 'falta sección de catálogo');
});

test('index.html contiene botón premium / suscripciones', () => {
  const html = fs.readFileSync(path.join(__dirname, 'index.html'), 'utf8');
  assert(html.includes('premium') || html.includes('suscripciones'), 'falta flujo de premium');
});

test('script.js usa el contrato REST paginado (contenido, pagina, tamano)', () => {
  const js = fs.readFileSync(path.join(__dirname, 'script.js'), 'utf8');
  assert(js.includes('pagina.contenido'), 'debe leer contenido del PageResponse');
  assert(js.includes("params.set('pagina'"), 'debe enviar pagina');
  assert(js.includes("params.set('tamano'"), 'debe enviar tamano');
});

test('script.js consume recomendaciones y similares', () => {
  const js = fs.readFileSync(path.join(__dirname, 'script.js'), 'utf8');
  assert(js.includes('/api/recomendaciones'), 'falta llamada a recomendaciones');
  assert(js.includes('/similares'), 'falta llamada a similares');
});

console.log(`\n${passed} prueba(s) pasada(s), ${failed} fallida(s).`);
process.exit(failed > 0 ? 1 : 0);
