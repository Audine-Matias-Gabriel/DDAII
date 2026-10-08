import { Navigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '@/hooks/useAuth'

export type ProtectedRouteProps = {
  children: ReactNode
}

/** Si no hay sesión, manda al login. */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { usuario } = useAuth()

  if (!usuario) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}
