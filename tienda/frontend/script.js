const API = 'http://localhost:8080';
let usuarioId = null;

const $ = (sel) => document.querySelector(sel);

function mostrar(seccion) {
  document.querySelectorAll('.seccion').forEach(s => s.classList.remove('activa'));
  $(`#${seccion}`).classList.add('activa');
  if (seccion === 'catalogo') cargarCatalogo();
}

function msg(id, texto, tipo = 'err') {
  const el = $(id);
  el.textContent = texto;
  el.className = 'mensaje ' + tipo;
}

async function login() {
  const nombre = $('#userNombre').value.trim();
  const email = $('#userEmail').value.trim();
  try {
    const r = await fetch(`${API}/api/usuarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre, email })
    });
    if (!r.ok) throw new Error('No se pudo crear el usuario');
    const u = await r.json();
    usuarioId = u.id;
    msg('msgLogin', `Usuario "${u.nombre}" creado (id=${u.id}). Se usará el header X-Usuario-Id.`, 'ok');
  } catch (e) {
    // Endpoint de usuarios puede no existir en esta etapa; usamos id simulado para la demo.
    usuarioId = Date.now();
    msg('msgLogin', `Usuario simulado id=${usuarioId} (header X-Usuario-Id).`, 'ok');
  }
}

async function cargarCatalogo() {
  const texto = $('#filtroTexto').value;
  const categoria = $('#filtroCategoria').value;
  const genero = $('#filtroGenero').value;
  const params = new URLSearchParams();
  if (texto) params.set('texto', texto);
  if (categoria) params.set('categoria', categoria);
  if (genero) params.set('genero', genero);
  params.set('pagina', 0);
  params.set('tamano', 20);

  try {
    const r = await fetch(`${API}/api/productos?${params}`, {
      headers: usuarioId ? { 'X-Usuario-Id': String(usuarioId) } : {}
    });
    if (!r.ok) throw new Error('Backend no responde');
    const pagina = await r.json();
    renderProductos(pagina.contenido || []);
  } catch (e) {
    $('#listaProductos').innerHTML = '<div class="mensaje err">No se pudo cargar el catálogo. ¿Está levantado el backend?</div>';
  }
}

function renderProductos(productos) {
  const cont = $('#listaProductos');
  if (!productos || productos.length === 0) {
    cont.innerHTML = '<div class="mensaje">No se encontraron productos.</div>';
    return;
  }
  cont.innerHTML = productos.map(p => `
    <div class="card">
      <img src="${p.imagenUrl || ''}" alt="${p.nombre}">
      <div class="card-body">
        <h3>${p.nombre}</h3>
        <p class="precio">$${p.precio}</p>
        <p class="stock">${p.disponible ? 'Disponible' : 'Sin stock'} · Talles: ${(p.tallesDisponibles||[]).join(', ')}</p>
        <p>${p.descripcion || ''}</p>
        <button onclick="verDetalle(${p.id})">Ver en tienda</button>
      </div>
    </div>
  `).join('');
}

async function verDetalle(id) {
  try {
    const r = await fetch(`${API}/api/productos/${id}`, {
      headers: usuarioId ? { 'X-Usuario-Id': String(usuarioId) } : {}
    });
    const p = await r.json();
    $('#detalleContenido').innerHTML = `
      <h3>${p.nombre}</h3>
      <p>${p.descripcion || ''}</p>
      <p class="precio">$${p.precio}</p>
      <p>${p.disponible ? 'Disponible / Sin stock' : ''} · ${p.marca || ''}</p>
      <p><strong>Talles:</strong> ${(p.tallesDisponibles||[]).join(', ')}</p>
      <p><strong>Tienda:</strong> ${p.tienda ? p.tienda.nombre : ''}</p>
      <a href="${p.urlOriginal || '#'}" target="_blank" class="boton">Ver en tienda oficial</a>
      <h3>Te puede gustar</h3>
      <div id="recomendaciones-${id}">Cargando recomendaciones...</div>
      <h3>Prendas similares en otras tiendas</h3>
      <div id="similares-${id}">Cargando similares...</div>
    `;
    mostrar('detalle');
    cargarRecomendaciones(id);
    cargarSimilares(id);
  } catch (e) {
    $('#detalleContenido').innerHTML = '<div class="mensaje err">No se pudo cargar el detalle.</div>';
    mostrar('detalle');
  }
}

async function cargarRecomendaciones(productoId) {
  const cont = $(`#recomendaciones-${productoId}`);
  try {
    const r = await fetch(`${API}/api/recomendaciones?productoId=${productoId}&limite=4`, {
      headers: usuarioId ? { 'X-Usuario-Id': String(usuarioId) } : {}
    });
    const recs = await r.json();
    cont.innerHTML = (recs || []).map(p => `
      <div style="border:1px solid #e2e8f0;padding:0.5rem;border-radius:0.5rem;margin-bottom:0.5rem;display:flex;align-items:center;gap:0.5rem;">
        <img src="${p.imagenUrl || ''}" width="40" height="40" style="object-fit:cover;border-radius:0.3rem;">
        <div><div style="font-size:0.85rem;font-weight:600;">${p.nombre}</div><div style="font-size:0.8rem;color:#0ea5e9;">$${p.precio}</div></div>
      </div>
    `).join('') || '<span style="font-size:0.85rem;color:#64748b;">Sin recomendaciones.</span>';
  } catch (e) {
    cont.textContent = 'No se pudieron cargar las recomendaciones.';
  }
}

async function cargarSimilares(productoId) {
  const cont = $(`#similares-${productoId}`);
  try {
    const r = await fetch(`${API}/api/productos/${productoId}/similares?limite=4`);
    const similares = await r.json();
    cont.innerHTML = (similares || []).map(p => `
      <div style="border:1px solid #e2e8f0;padding:0.5rem;border-radius:0.5rem;margin-bottom:0.5rem;display:flex;align-items:center;gap:0.5rem;">
        <img src="${p.imagenUrl || ''}" width="40" height="40" style="object-fit:cover;border-radius:0.3rem;">
        <div>
          <div style="font-size:0.85rem;font-weight:600;">${p.nombre}</div>
          <div style="font-size:0.8rem;color:#64748b;">${p.tienda ? p.tienda.nombre : ''} · $${p.precio}</div>
          <a href="${p.urlOriginal || '#'}" target="_blank" style="font-size:0.8rem;">Ver en tienda</a>
        </div>
      </div>
    `).join('') || '<span style="font-size:0.85rem;color:#64748b;">Sin similares en otras tiendas.</span>';
  } catch (e) {
    cont.textContent = 'No se pudieron cargar los similares.';
  }
}

async function irPremium() {
  if (!usuarioId) {
    msg('msgPlanes', 'Primero iniciá sesión.', 'err');
    return;
  }
  try {
    const r = await fetch(`${API}/api/suscripciones`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Usuario-Id': String(usuarioId)
      },
      body: JSON.stringify({ usuarioId })
    });
    const data = await r.json();
    if (data.urlPago) {
      window.location.href = data.urlPago;
    } else {
      msg('msgPlanes', 'No se pudo obtener la URL de pago.', 'err');
    }
  } catch (e) {
    msg('msgPlanes', 'Error al crear el pago.', 'err');
  }
}

// Si llegamos por /pago/exito o /pago/error, mostrar la sección correspondiente.
const hash = window.location.hash.replace('#/', '');
if (hash === 'pago-exito' || hash === 'pago-error') {
  mostrar(hash);
}

// Cargar catálogo al inicio
cargarCatalogo();
