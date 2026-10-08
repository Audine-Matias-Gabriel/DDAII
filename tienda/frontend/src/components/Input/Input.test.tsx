import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Input } from './Input'

describe('Input', () => {
  it('asocia el label con el input', () => {
    render(<Input label="Email" />)

    expect(screen.getByLabelText('Email')).toBeInTheDocument()
  })

  it('llama onChange al tipear', async () => {
    const onChange = vi.fn()
    const user = userEvent.setup()
    render(<Input label="Email" value="" onChange={onChange} />)

    await user.type(screen.getByLabelText('Email'), 'a')

    expect(onChange).toHaveBeenCalled()
  })

  it('marca el error y lo referencia con aria-describedby', () => {
    render(<Input label="Email" error="Campo requerido" />)

    const input = screen.getByLabelText('Email')
    const error = screen.getByText('Campo requerido')

    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(input).toHaveAttribute('aria-describedby', error.id)
  })

  it('sin error no marca aria-invalid ni describedby', () => {
    render(<Input label="Email" />)

    const input = screen.getByLabelText('Email')

    expect(input).toHaveAttribute('aria-invalid', 'false')
    expect(input).not.toHaveAttribute('aria-describedby')
  })
})
