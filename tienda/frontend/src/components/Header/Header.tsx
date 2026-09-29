import { Link, NavLink } from 'react-router-dom'
import { Button } from '@/components/Button/Button'
import { CarritoMenu } from '@/components/CarritoMenu/CarritoMenu'
import styles from './Header.module.css'

export type HeaderProps = {
  usuario?: string | null
  onLogout: () => void
}

export function Header({ usuario, onLogout }: HeaderProps) {
  return (
    <header className={styles.header}>
      <Link to="/" className={styles.logo}>
        Tienda<span className={styles.logoAcento}>de Ropa</span>
      </Link>

      <nav className={styles.nav}>
        <NavLink to="/" end className={styles.enlace}>
          Shop
        </NavLink>
        <NavLink to="/productos/nuevo" className={styles.enlace}>
          Publicar
        </NavLink>
      </nav>

      <div className={styles.acciones}>
        <CarritoMenu />

        {usuario && <span className={styles.saludo}>Hola, {usuario}</span>}

        <Button variante="secundario" tamano="sm" onClick={onLogout}>
          Salir
        </Button>
      </div>
    </header>
  )
}
