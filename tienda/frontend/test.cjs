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

test('FavoritesContext sincroniza los favoritos con el backend', () => {
  const ctx = fs.readFileSync(path.join(__dirname, 'src', 'context', 'FavoritesContext.jsx'), 'utf8');
  assert(ctx.includes('api.getFavoritos()'), 'no carga los favoritos del usuario');
  assert(ctx.includes('api.addFavorito('), 'no guarda favoritos en el backend');
  assert(ctx.includes('api.removeFavorito('), 'no quita favoritos del backend');
});

test('existe src/components/Navbar.jsx con botón de cuenta', () => {
  const nav = fs.readFileSync(path.join(__dirname, 'src', 'components', 'Navbar.jsx'), 'utf8');
  assert(nav.includes('Ingresar / Crear cuenta'), 'falta botón de cuenta');
  assert(nav.includes('Cerrar sesión'), 'falta botón cerrar sesión');
});

test('Navbar tiene una pestaña de Favoritos solo para usuarios logueados', () => {
  const nav = fs.readFileSync(path.join(__dirname, 'src', 'components', 'Navbar.jsx'), 'utf8');
  assert(nav.includes('isAuthenticated && ('), 'la pestaña no está condicionada al login');
  assert(nav.includes('href="#/favoritos"'), 'falta el enlace a /favoritos');
  assert(nav.includes("navigate('/favoritos')"), 'el enlace no navega a /favoritos');
  assert(nav.includes("route === '/favoritos'"), 'la pestaña no se marca como activa');
});

test('Navbar tiene una pestaña de Búsqueda visual solo para premium', () => {
  const nav = fs.readFileSync(path.join(__dirname, 'src', 'components', 'Navbar.jsx'), 'utf8');
  assert(nav.includes('user?.premium === true'), 'la pestaña no está condicionada a premium');
  assert(nav.includes('href="#/busqueda-visual"'), 'falta el enlace a /busqueda-visual');
  assert(nav.includes("navigate('/busqueda-visual')"), 'el enlace no navega a /busqueda-visual');
  assert(nav.includes("route === '/busqueda-visual'"), 'la pestaña no se marca como activa');
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

test('api.js registra vistas para recomendaciones premium', () => {
  const api = fs.readFileSync(path.join(__dirname, 'src', 'api.js'), 'utf8');
  assert(api.includes('registrarVista('), 'falta registrarVista en api.js');
  assert(api.includes('/api/recomendaciones/vistas/${productoId}'), 'falta el endpoint de vistas');
  const detail = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'ProductDetail.jsx'), 'utf8');
  assert(detail.includes('api.registrarVista(id)'), 'el detalle no registra la vista');
});

test('api.js expone el historial de precios', () => {
  const api = fs.readFileSync(path.join(__dirname, 'src', 'api.js'), 'utf8');
  assert(api.includes('getHistorialPrecios'), 'falta getHistorialPrecios en api.js');
  assert(api.includes('/api/productos/${id}/historial-precios'), 'falta el endpoint de historial');
});

test('api.js expone la búsqueda visual premium con multipart', () => {
  const api = fs.readFileSync(path.join(__dirname, 'src', 'api.js'), 'utf8');
  assert(api.includes('buscarVisual('), 'falta buscarVisual en api.js');
  assert(api.includes('/api/premium/busqueda-visual'), 'falta el endpoint de búsqueda visual');
  assert(api.includes("form.append('imagen', file)"), 'no se envía la imagen como multipart');
});

test('App rutea la página de búsqueda visual', () => {
  const app = fs.readFileSync(path.join(__dirname, 'src', 'App.jsx'), 'utf8');
  assert(app.includes("from './pages/BusquedaVisual'"), 'falta importar BusquedaVisual');
  assert(app.includes("ruta === '/busqueda-visual'"), 'falta la ruta /busqueda-visual');
  assert(app.includes('<BusquedaVisual'), 'la ruta no renderiza la página');
});

test('la página de búsqueda visual sube una foto y muestra los productos premium', () => {
  const page = fs.readFileSync(
    path.join(__dirname, 'src', 'pages', 'BusquedaVisual.jsx'), 'utf8');
  assert(page.includes('user?.premium === true'), 'la página no está restringida a premium');
  assert(page.includes('type="file"'), 'falta el selector de imagen');
  assert(page.includes('name="imagen"'), 'el campo no se llama imagen');
  assert(page.includes('accept="image/jpeg,image/png,image/tiff"'), 'acepta tipos inválidos');
  assert(page.includes('api.buscarVisual(archivo, 8)'), 'no llama a la API de búsqueda visual');
  assert(page.includes('<ProductCard'), 'no muestra los productos encontrados');
});

test('el gráfico de precios solo se muestra a usuarios premium', () => {
  const detail = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'ProductDetail.jsx'), 'utf8');
  assert(detail.includes('user?.premium === true'), 'el gráfico no está condicionado a premium');
  assert(detail.includes('<PriceHistoryChart'), 'ProductDetail no renderiza el gráfico');
  const chart = fs.readFileSync(
    path.join(__dirname, 'src', 'components', 'PriceHistoryChart.jsx'), 'utf8');
  assert(chart.includes('api.getHistorialPrecios('), 'el gráfico no carga la serie');
  assert(chart.includes('<svg'), 'el gráfico no dibuja la serie');
});

test('el catálogo filtra por tienda, precio y género Niña', () => {
  const home = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'Home.jsx'), 'utf8');
  assert(home.includes('api.getTiendas()'), 'falta cargar las tiendas');
  for (const campo of ['tienda:', 'precioMin:', 'precioMax:']) {
    assert(home.includes(campo), `falta mandar ${campo} al backend`);
  }
  assert(home.includes('value="Niña"'), 'falta la opción Niña');
  const api = fs.readFileSync(path.join(__dirname, 'src', 'api.js'), 'utf8');
  assert(api.includes('/api/tiendas'), 'falta getTiendas en api.js');
});

test('Planes muestra el precio que define el backend (no uno fijo)', () => {
  const plans = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'Plans.jsx'), 'utf8');
  assert(plans.includes('api.getPlanPremium()'), 'Planes no pide el precio al backend');
  // El plan gratuito ($0) sí es fijo; el premium no.
  assert(!/\$[1-9][\d.]* \/ mes/.test(plans), 'Planes tiene un precio premium fijo en el código');
  const api = fs.readFileSync(path.join(__dirname, 'src', 'api.js'), 'utf8');
  assert(api.includes('/api/suscripciones/plan'), 'falta getPlanPremium en api.js');
});

test('el catálogo descarta respuestas de consultas viejas', () => {
  const home = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'Home.jsx'), 'utf8');
  assert(home.includes('consulta !== ultimaConsulta.current'), 'una respuesta vieja puede pisar a la nueva');
});

test('la tarjeta no inventa "Sin stock" cuando el producto no trae disponible', () => {
  const card = fs.readFileSync(path.join(__dirname, 'src', 'components', 'ProductCard.jsx'), 'utf8');
  assert(card.includes('producto.disponible != null'), 'ProductCard muestra stock aunque no venga el dato');
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

  // --- Landing: cada tienda abre el catálogo con su filtro ---

  const { filtrosDesdeQuery, rutaCatalogo } = await import(
    pathToFileURL(path.join(__dirname, 'src', 'query.js')).href
  );
  const tiendas = await import(pathToFileURL(path.join(__dirname, 'src', 'tiendas.js')).href);

  test('rutaCatalogo arma la ruta del catálogo con el filtro de tienda', () => {
    assert(rutaCatalogo({ tienda: 3 }) === '/catalogo?tienda=3', `salió "${rutaCatalogo({ tienda: 3 })}"`);
    assert(rutaCatalogo() === '/catalogo', 'sin filtros debería ser /catalogo');
    assert(rutaCatalogo({ tienda: '', otro: 'x' }) === '/catalogo', 'no descarta vacíos o desconocidos');
  });

  test('filtrosDesdeQuery lee solo los filtros conocidos del catálogo', () => {
    const f = filtrosDesdeQuery('tienda=2&genero=Mujer&pagina=4&texto=');
    assert(JSON.stringify(f) === JSON.stringify({ genero: 'Mujer', tienda: '2' }), `salió ${JSON.stringify(f)}`);
    const ida = filtrosDesdeQuery(rutaCatalogo({ tienda: 1, texto: 'air max' }).split('?')[1]);
    assert(ida.tienda === '1' && ida.texto === 'air max', 'rutaCatalogo y filtrosDesdeQuery no van y vuelven');
  });

  test('la landing es el inicio y el catálogo recibe los filtros de la URL', () => {
    const app = fs.readFileSync(path.join(__dirname, 'src', 'App.jsx'), 'utf8');
    assert(app.includes('<Landing'), 'App no muestra la landing');
    assert(app.includes("ruta === '/' ?"), 'la landing no está en la ruta /');
    assert(app.includes('filtrosIniciales={filtrosDesdeQuery(query)}'), 'el catálogo no recibe los filtros de la URL');
    const home = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'Home.jsx'), 'utf8');
    assert(home.includes('...filtrosIniciales'), 'Home no arranca con los filtros de la URL');
    const landing = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'Landing.jsx'), 'utf8');
    assert(landing.includes('rutaCatalogo({ tienda: tienda.id })'), 'las tiendas de la landing no llevan al catálogo filtrado');
  });

  test('la landing ordena las tiendas como Sportline, Newsport y Grid', () => {
    const orden = tiendas
      .ordenarTiendas([
        { id: 1, nombre: 'Grid' },
        { id: 2, nombre: 'Otra' },
        { id: 3, nombre: 'Sportline' },
        { id: 4, nombre: 'Newsport' }
      ])
      .map((t) => t.nombre);
    assert(orden.join(',') === 'Sportline,Newsport,Grid,Otra', `salió ${orden.join(',')}`);
    assert(tiendas.temaDeTienda({ nombre: 'Grid' }).fondo === '#ffd200', 'Grid no tiene su color');
    assert(tiendas.temaDeTienda({ nombre: 'Desconocida' }).fondo, 'falta el tema genérico');
  });

  test('elegirDestacados prioriza zapatillas con imagen y no repite', () => {
    const productos = [
      { id: 1, nombre: 'Remera básica', categoria: 'REMERA', imagenUrl: 'r.jpg' },
      { id: 2, nombre: 'Zapatillas sin foto', categoria: 'CALZADO', imagenUrl: null },
      { id: 3, nombre: 'Zapatillas Air', categoria: 'CALZADO', imagenUrl: 'z.jpg' },
      { id: 4, nombre: 'Ojotas', categoria: 'CALZADO', imagenUrl: 'o.jpg' }
    ];
    const ids = tiendas.elegirDestacados(productos).map((p) => p.id);
    assert(ids.join(',') === '3,1,4', `salió ${ids.join(',')}`);
  });

  test('marcasPrincipales junta mayúsculas y respeta siglas', () => {
    const marcas = tiendas.marcasPrincipales([
      { marca: 'ADIDAS' }, { marca: 'adidas' }, { marca: 'NBA' }, { marca: 'NBA' }, { marca: 'NBA' }, { marca: 'Nike' }
    ]);
    assert(marcas.join(',') === 'NBA,Adidas,Nike', `salió ${marcas.join(',')}`);
  });

  test('imagenVtex pide la imagen de VTEX en el tamaño indicado', () => {
    const url = 'https://sportline.vteximg.com.br/arquivos/ids/1996609/foto_1.jpg?v=1';
    assert(
      tiendas.imagenVtex(url, 600) === 'https://sportline.vteximg.com.br/arquivos/ids/1996609-600-600/foto_1.jpg?v=1',
      'no agrega el tamaño'
    );
    assert(
      tiendas.imagenVtex(tiendas.imagenVtex(url, 600), 300).includes('/1996609-300-300/'),
      'no reemplaza un tamaño anterior'
    );
    assert(tiendas.imagenVtex('https://otra.com/a.jpg', 600) === 'https://otra.com/a.jpg', 'cambia URLs que no son de VTEX');
  });

  // --- Catálogo: "Mostrar más productos" ---

  const { hayMasPaginas, unirProductos, TAMANO_PAGINA } = await import(
    pathToFileURL(path.join(__dirname, 'src', 'paginacion.js')).href
  );

  test('hayMasPaginas sabe si quedan productos por pedir', () => {
    assert(TAMANO_PAGINA === 20, 'el catálogo pide de a 20');
    assert(hayMasPaginas(0, 271), 'con 271 productos hay más de una página');
    assert(!hayMasPaginas(0, 20), 'con 20 productos alcanza la primera página');
    assert(hayMasPaginas(1, 41) && !hayMasPaginas(1, 40), 'cuenta mal las páginas siguientes');
    assert(!hayMasPaginas(0, 0), 'sin productos no hay más');
  });

  test('unirProductos suma la página nueva sin repetir productos', () => {
    const unidos = unirProductos([{ id: 1 }, { id: 2 }], [{ id: 2 }, { id: 3 }]);
    assert(unidos.map((p) => p.id).join(',') === '1,2,3', `salió ${unidos.map((p) => p.id).join(',')}`);
  });

  test('el catálogo tiene el botón "Mostrar más productos" y pide la página siguiente', () => {
    const home = fs.readFileSync(path.join(__dirname, 'src', 'pages', 'Home.jsx'), 'utf8');
    assert(home.includes('Mostrar más productos'), 'falta el botón');
    assert(home.includes('parametros(siguiente)'), 'no pide la página siguiente');
    assert(home.includes('unirProductos('), 'no suma los productos a los que ya estaban');
    assert(home.includes('hayMasPaginas(pagina, total)'), 'el botón no se oculta cuando no quedan productos');
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
