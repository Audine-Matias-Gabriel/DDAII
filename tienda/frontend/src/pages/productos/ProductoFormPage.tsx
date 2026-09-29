import { useNavigate } from 'react-router-dom'
import { ProductoForm } from './components/ProductoForm'
import styles from './ProductoFormPage.module.css'

export function ProductoFormPage() {
  const navigate = useNavigate()

  return (
    <div className={styles.pagina}>
      <h1 className={styles.titulo}>Publicar producto</h1>
      <p className={styles.subtitulo}>Completá los datos del artículo.</p>

      <ProductoForm onCancel={() => navigate('/')} />
    </div>
  )
}
