import { useState } from 'react'
import type { Genero, Producto } from '@/types/Producto'
import {
  primerGenero,
  stockDe,
  tallesDe,
  generosDisponibles,
} from '@/lib/variantes'

/**
 * Estado de la variante (género + talle) que el usuario está eligiendo.
 *
 * El género arranca en el declarado por el producto (si tiene stock) o en el
 * primero disponible. El talle queda vacío salvo que el género tenga uno solo,
 * así el botón de agregar queda deshabilitado hasta que el usuario lo elija.
 */
export function useVariante(producto: Producto | null) {
  const [generoElegido, setGeneroElegido] = useState<Genero | ''>('')
  const [talleElegido, setTalleElegido] = useState('')

  const generos = producto ? generosDisponibles(producto) : []
  const genero = producto
    ? generoElegido && generos.includes(generoElegido)
      ? generoElegido
      : primerGenero(producto)
    : ''
  const talles = producto && genero ? tallesDe(producto, genero) : []
  const talle =
    talleElegido || (talles.length === 1 ? (talles[0] ?? '') : '')
  const stock = producto ? stockDe(producto, genero, talle) : 0

  const elegirGenero = (elegido: Genero) => {
    setGeneroElegido(elegido)
    setTalleElegido('')
  }

  return {
    generos,
    talles,
    genero,
    talle,
    setGenero: elegirGenero,
    setTalle: setTalleElegido,
    stock,
    listo: Boolean(producto && genero && talle && stock > 0),
  }
}
