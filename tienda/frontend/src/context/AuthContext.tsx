import { createContext, useCallback, useMemo, useState } from 'react'
import type { ReactNode } from 'react'

export type AuthContextValue = {
  usuario: string | null
  entrar: (nombre: string) => void
  salir: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

// Prototipo: la sesión vive en memoria y no se verifica nada contra ningún lado.
// Si hay que recordarla al recargar, es un useEffect con localStorage.
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<string | null>(null)

  const entrar = useCallback((nombre: string) => setUsuario(nombre), [])
  const salir = useCallback(() => setUsuario(null), [])

  const valor = useMemo(() => ({ usuario, entrar, salir }), [usuario, entrar, salir])

  return <AuthContext value={valor}>{children}</AuthContext>
}
