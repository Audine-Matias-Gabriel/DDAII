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

test('existe src/pages/PagoResultado.jsx y se rutea /pago/exito', () => {
  const pr = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'PagoResultado.jsx'), 'utf8');
  assert(pr.includes('confirmarSuscripcion'), 'falta confirmarSuscripcion en PagoResultado');
  const app = fs.readFileSync(path.join(__dirname, 'src', 'App.jsx'), 'utf8');
  assert(app.includes('/pago/exito'), 'falta ruta /pago/exito en App');
  assert(app.includes('/pago/error'), 'falta ruta /pago/error en App');
  const api = fs.readFileSync(path.join(__dirname, 'src', 'api.js'), 'utf8');
  assert(api.includes('/confirmar?paymentId='), 'falta endpoint confirmar en api.js');
});

test('existe src/pages/ProductDetail.jsx con recomendaciones y similares', () => {
  const detail = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'ProductDetail.jsx'), 'utf8');
  assert(detail.includes('getRecomendaciones'), 'falta llamadas a recomendaciones');
  assert(detail.includes('getSimilares'), 'falta llamadas a similares');
});

test('la tarjeta del catálogo no muestra la descripción', () => {
  const card = fs.readFileSync(path.join(__dirname, 'src', 'components', 'ProductCard.jsx'), 'utf8');
  assert(!card.includes('producto.descripcion'), 'ProductCard muestra la descripción');
});

test('la sesión guardada se refresca con /api/auth/me (premium al día)', () => {
  const auth = fs.readFileSync(path.join(__dirname, 'src', 'context', 'AuthContext.jsx'), 'utf8');
  const efecto = auth.slice(auth.indexOf('useEffect('), auth.indexOf('const saveSession'));
  assert(efecto.includes('api.me()'), 'AuthProvider no refresca el usuario al cargar');
  const plans = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'Plans.jsx'), 'utf8');
  assert(/if \(isAuthenticated\) restoreSession\(\)/.test(plans), 'Planes no refresca el usuario al entrar');
});

test('las páginas importan los componentes que usan', () => {
  const componentes = fs.readdirSync(path.join(__dirname, 'src', 'components'))
    .filter((f) => f.endsWith('.jsx'))
    .map((f) => f.replace('.jsx', ''));
  const dirPages = path.join(__dirname, 'src', 'pages');
  for (const archivo of fs.readdirSync(dirPages).filter((f) => f.endsWith('.jsx'))) {
    const fuente = fs.readFileSync(path.join(dirPages, archivo), 'utf8');
    for (const c of componentes) {
      if (fuente.includes(`<${c}`)) {
        assert(new RegExp(`import\\s+${c}\\s+from`).test(fuente), `${archivo} usa <${c}> sin importarlo`);
      }
    }
  }
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

// --- Pruebas que ejecutan módulos ESM (import dinámico) ---

async function testsAsync() {
  const { pathToFileURL } = require('url');
  const { armarQuery } = await import(
    pathToFileURL(path.join(__dirname, 'src', 'query.js')).href
  );

  test('armarQuery descarta filtros undefined, null y vacíos', () => {
    const q = armarQuery({ texto: undefined, categoria: null, genero: '', pagina: 0 });
    assert(!q.includes('undefined'), `query con "undefined": ${q}`);
    assert(!q.includes('null'), `query con "null": ${q}`);
    assert(q === 'pagina=0', `se esperaba "pagina=0" y salió "${q}"`);
  });

  test('armarQuery conserva los filtros con valor', () => {
    const q = armarQuery({ categoria: 'remeras', genero: 'mujer', texto: undefined });
    assert(q === 'categoria=remeras&genero=mujer', `salió "${q}"`);
  });

  test('armarQuery sin filtros devuelve string vacío', () => {
    assert(armarQuery({ texto: undefined }) === '', 'debería ser ""');
    assert(armarQuery() === '', 'debería ser "" sin params');
  });

  test('api.getProductos usa armarQuery', () => {
    const api = fs.readFileSync(path.join(__dirname, 'src', 'api.js'), 'utf8');
    assert(api.includes('armarQuery(params)'), 'getProductos no usa armarQuery');
  });
}

testsAsync()
  .catch((err) => {
    console.error('✗ error cargando módulos ESM -', err.message);
    failed++;
  })
  .finally(() => {
    console.log(`\n${passed} prueba(s) pasada(s), ${failed} fallida(s).`);
    process.exit(failed > 0 ? 1 : 0);
  });
