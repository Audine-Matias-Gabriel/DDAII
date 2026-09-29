import { CATEGORIAS, TALLES } from '@/types/Producto'
import { TIENDAS } from '@/data'
import { Button } from '@/components/Button/Button'
import { Input } from '@/components/Input/Input'
import { Select } from '@/components/Select/Select'
import { Textarea } from '@/components/Textarea/Textarea'
import styles from './ProductoForm.module.css'

export type ProductoFormProps = {
  onCancel: () => void
}

export function ProductoForm({ onCancel }: ProductoFormProps) {
  return (
    <form
      className={styles.formulario}
      // Prototipo: el alta no guarda nada. Acá va el submit real.
      onSubmit={(e) => e.preventDefault()}
    >
      <div className={styles.fila}>
        <Input label="Nombre" placeholder="Campera de jean" />
        <Input label="Precio" type="number" placeholder="0" />
      </div>

      <Textarea label="Descripción" rows={4} placeholder="Contá de qué material es, medidas, etc." />

      <div className={styles.fila}>
        <Input label="Stock" type="number" placeholder="0" />
        <Select
          label="Categoría"
          placeholder="Elegí una categoría"
          opciones={CATEGORIAS.map((c) => ({ value: c, label: c }))}
        />
      </div>

      <div className={styles.fila}>
        <Select
          label="Tienda"
          placeholder="Elegí una tienda"
          opciones={TIENDAS.map((t) => ({ value: t.id, label: t.nombre }))}
        />
        <Select
          label="Estado"
          opciones={[
            { value: 'NUEVO', label: 'Nuevo' },
            { value: 'USADO', label: 'Usado' },
          ]}
        />
      </div>

      <fieldset className={styles.talles}>
        <legend className={styles.tallesLeyenda}>Talles</legend>
        <div className={styles.tallesFila}>
          {TALLES.map((talle) => (
            <label key={talle} className={styles.talle}>
              <input type="checkbox" value={talle} />
              {talle}
            </label>
          ))}
        </div>
      </fieldset>

      <Input label="URL de imagen (opcional)" type="url" placeholder="https://..." />

      <div className={styles.acciones}>
        <Button type="submit">Publicar</Button>
        <Button type="button" variante="secundario" onClick={onCancel}>
          Cancelar
        </Button>
      </div>
    </form>
  )
}
