import { useContext } from 'react'
import type { CarritoContextValue } from '@/context/CarritoContext'
import { CarritoContext } from '@/context/CarritoContext'

export function useCarrito(): CarritoContextValue {
  const contexto = useContext(CarritoContext)
  if (!contexto) {
    throw new Error('useCarrito debe usarse dentro de un CarritoProvider')
  }
  return contexto
}
