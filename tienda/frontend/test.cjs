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

// --- Pruebas del frontend React + Vite (P1) ---

test('existe package.json con react y vite', () => {
  const pkg = JSON.parse(
    fs.readFileSync(path.join(__dirname, 'package.json'), 'utf8')
  );
  assert(pkg.dependencies.react, 'falta dependencia react');
  assert(pkg.devDependencies.vite, 'falta devDependency vite');
});

test('existe vite.config.js', () => {
  assert(fs.existsSync(path.join(__dirname, 'vite.config.js')), 'vite.config.js no existe');
});

test('existe index.html con div#root', () => {
  const html = fs.readFileSync(path.join(__dirname, 'index.html'), 'utf8');
  assert(html.includes('id="root"'), 'falta div#root en index.html');
});

test('existe src/main.jsx', () => {
  assert(fs.existsSync(path.join(__dirname, 'src', 'main.jsx')), 'src/main.jsx no existe');
});

test('existe src/App.jsx con enrutador', () => {
  const app = fs.readFileSync(path.join(__dirname, 'src', 'App.jsx'), 'utf8');
  assert(app.includes('AuthProvider'), 'falta AuthProvider en App');
  assert(app.includes('FavoritesProvider'), 'falta FavoritesProvider en App');
});

test('existe src/api.js con endpoints de auth y favoritos', () => {
  const api = fs.readFileSync(path.join(__dirname, 'src', 'api.js'), 'utf8');
  assert(api.includes('/api/auth/registro'), 'falta endpoint registro');
  assert(api.includes('/api/auth/login'), 'falta endpoint login');
  assert(api.includes('/api/auth/logout'), 'falta endpoint logout');
  assert(api.includes('/api/auth/me'), 'falta endpoint me');
  assert(api.includes('/api/favoritos'), 'falta endpoint favoritos');
  assert(api.includes('Authorization'), 'falta header Authorization Bearer');
});

test('existe src/context/AuthContext.jsx', () => {
  const ctx = fs.readFileSync(path.join(__dirname, 'src', 'context', 'AuthContext.jsx'), 'utf8');
  assert(ctx.includes('localStorage'), 'falta persistencia en localStorage');
  assert(ctx.includes('isAuthenticated'), 'falta isAuthenticated');
  assert(ctx.includes('login'), 'falta login');
  assert(ctx.includes('register'), 'falta register');
  assert(ctx.includes('logout'), 'falta logout');
});

test('existe src/context/FavoritesContext.jsx', () => {
  const ctx = fs.readFileSync(path.join(__dirname, 'src', 'context', 'FavoritesContext.jsx'), 'utf8');
  assert(ctx.includes('addFavorite'), 'falta addFavorite');
  assert(ctx.includes('removeFavorite'), 'falta removeFavorite');
  assert(ctx.includes('isFavorite'), 'falta isFavorite');
  assert(ctx.includes('toggleFavorite'), 'falta toggleFavorite');
});

test('existe src/components/Navbar.jsx con botón de cuenta', () => {
  const nav = fs.readFileSync(path.join(__dirname, 'src', 'components', 'Navbar.jsx'), 'utf8');
  assert(nav.includes('Ingresar / Crear cuenta'), 'falta botón de cuenta');
  assert(nav.includes('Cerrar sesión'), 'falta botón cerrar sesión');
});

test('existe src/components/AuthModal.jsx', () => {
  assert(fs.existsSync(path.join(__dirname, 'src', 'components', 'AuthModal.jsx')), 'AuthModal no existe');
});

test('existe src/pages/Login.jsx', () => {
  assert(fs.existsSync(path.join(__dirname, 'src', 'pages', 'Login.jsx')), 'Login page no existe');
});

test('existe src/pages/Register.jsx', () => {
  assert(fs.existsSync(path.join(__dirname, 'src', 'pages', 'Register.jsx')), 'Register page no existe');
});

test('existe src/pages/Favorites.jsx', () => {
  assert(fs.existsSync(path.join(__dirname, 'src', 'pages', 'Favorites.jsx')), 'Favorites page no existe');
});

test('existe src/pages/Plans.jsx con flujo premium', () => {
  const plans = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'Plans.jsx'), 'utf8');
  assert(plans.includes('createSuscripcion'), 'falta createSuscripcion');
  assert(plans.includes('urlPago'), 'falta urlPago');
});

test('existe src/pages/ProductDetail.jsx con recomendaciones y similares', () => {
  const detail = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'ProductDetail.jsx'), 'utf8');
  assert(detail.includes('getRecomendaciones'), 'falta llamadas a recomendaciones');
  assert(detail.includes('getSimilares'), 'falta llamadas a similares');
});

test('existe .env.example con VITE_API_URL', () => {
  const env = fs.readFileSync(path.join(__dirname, '.env.example'), 'utf8');
  assert(env.includes('VITE_API_URL'), 'falta VITE_API_URL en .env.example');
});

test('existe Dockerfile del frontend (build Vite)', () => {
  const df = fs.readFileSync(path.join(__dirname, 'Dockerfile'), 'utf8');
  assert(df.includes('npm run build'), 'falta build de Vite en Dockerfile');
  assert(df.includes('serve'), 'falta servidor estático en Dockerfile');
});

console.log(`\n${passed} prueba(s) pasada(s), ${failed} fallida(s).`);
process.exit(failed > 0 ? 1 : 0);
