import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { Button } from '@/components/Button/Button'
import { Input } from '@/components/Input/Input'
import styles from './LoginPage.module.css'

export function LoginPage() {
  const { entrar } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')

  // La contraseña se muestra para que el formulario se vea completo,
  // pero no se lee ni se valida contra nada.
  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    const nombre = email.split('@')[0] || 'invitado'
    entrar(nombre)
    navigate('/', { replace: true })
  }

  return (
    <div className={styles.pagina}>
      <form className={styles.formulario} onSubmit={handleSubmit}>
        <h1 className={styles.titulo}>Tienda de Ropa</h1>
        <p className={styles.subtitulo}>Ingresá para ver el catálogo.</p>

        <Input
          label="Email"
          type="email"
          name="email"
          autoComplete="email"
          placeholder="demo@tienda.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <Input
          label="Contraseña"
          type="password"
          name="password"
          autoComplete="current-password"
          placeholder="••••••••"
        />

        <Button type="submit" className={styles.boton}>
          Ingresar
        </Button>
      </form>
    </div>
  )
}
