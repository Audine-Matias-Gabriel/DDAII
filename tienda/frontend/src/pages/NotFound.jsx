export default function NotFound({ navigate }) {
  return (
    <section className="page not-found">
      <h1>404</h1>
      <p>Página no encontrada.</p>
      <button className="btn btn-primary" onClick={() => navigate('/')}>
        Ir al catálogo
      </button>
    </section>
  )
}
