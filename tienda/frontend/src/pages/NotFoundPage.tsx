import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/Button/Button'
import styles from './NotFoundPage.module.css'

export function NotFoundPage() {
  const navigate = useNavigate()

  return (
    <div className={styles.pagina}>
      <h1 className={styles.codigo}>404</h1>
      <p className={styles.mensaje}>No encontramos la página que buscabas.</p>
      <Button onClick={() => navigate('/')}>Volver al inicio</Button>
    </div>
  )
}
