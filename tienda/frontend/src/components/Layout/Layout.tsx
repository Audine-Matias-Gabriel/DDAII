import { Outlet } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { Header } from '@/components/Header/Header'
import styles from './Layout.module.css'

/** Contenedor de las páginas privadas: barra superior + contenido. */
export function Layout() {
  const { usuario, salir } = useAuth()

  return (
    <div className={styles.layout}>
      <Header usuario={usuario} onLogout={salir} />
      <main className={styles.contenido}>
        <Outlet />
      </main>
    </div>
  )
}
