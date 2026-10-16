import { forwardRef, useEffect, useMemo } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { Boton, Campo, Input, InputContrasena, MensajeError, Select, TextArea } from '../ui'
import {
  aInputDateTime,
  desdeInputDateTime,
  hoyLocalISO,
  mananaLocalISO,
} from '@/shared/lib/utils'
import { FORMATOS, limpiarDecimal } from '@/shared/lib/validacion'
import type { CampoFormulario, TipoCampo } from './tipos'

/** Convierte el valor del registro al formato que espera el input del campo. */
function aValorInput(tipo: TipoCampo, bruto: unknown): unknown {
  if (tipo === 'fechaHora') return aInputDateTime(bruto as string)

  // El valor decimal se guarda "crudo" (sin ceros de relleno) y el input lo
  // muestra siempre con 2 cifras (ej. 45 -> "45.00", 38.5 -> "38.50").
  if (tipo === 'decimal') {
    if (bruto === null || bruto === undefined || bruto === '') return ''
    const numero = Number(bruto)
    return Number.isNaN(numero) ? String(bruto) : String(numero)
  }

  return bruto ?? ''
}

/** Muestra un decimal "crudo" (ej. "45", "45.", "45.5") con 2 cifras. */
function mostrarDecimal(valor: string): string {
  if (valor === '') return ''
  const [entero, fraccion = ''] = valor.split('.')
  if (!valor.includes('.')) return `${entero}.00`
  return `${entero || '0'}.${fraccion.padEnd(2, '0')}`
}

/**
 * Input de precio que se ve siempre con 2 decimales mientras se escribe.
 *
 * Los dígitos llenan primero la parte entera (escribes "45" y se ve "45.00")
 * y, al pulsar "." o ",", pasan a los centavos (escribes ".5" y se ve
 * "45.50"). Por dentro se guarda el valor sin el relleno ("45" / "45.5"), que
 * es lo que termina en el formulario.
 */
const InputDecimal = forwardRef<
  HTMLInputElement,
  {
    value: string
    onChange: (valor: string) => void
    onBlur?: React.FocusEventHandler<HTMLInputElement>
    name?: string
    disabled?: boolean
    placeholder?: string
  }
>(function InputDecimal({ value, onChange, onBlur, ...props }, ref) {
  const manejarTecla = (e: React.KeyboardEvent<HTMLInputElement>) => {
    // Deja pasar atajos (copiar/pegar/seleccionar) y teclas de navegación.
    if (e.ctrlKey || e.metaKey || e.altKey) return

    const input = e.currentTarget
    const seleccionTotal =
      input.selectionStart === 0 && input.selectionEnd === input.value.length
    const tecla = e.key

    if (tecla === 'Backspace') {
      e.preventDefault()
      onChange(seleccionTotal ? '' : value.slice(0, -1))
      return
    }

    if (tecla === 'Delete') {
      e.preventDefault()
      onChange('')
      return
    }

    if (tecla === '.' || tecla === ',') {
      e.preventDefault()
      if (!value.includes('.')) onChange(`${value || '0'}.`)
      return
    }

    if (tecla.length === 1) {
      // Bloquea cualquier otro carácter imprimible (letras, espacios, etc.).
      if (!/[0-9]/.test(tecla)) {
        e.preventDefault()
        return
      }

      e.preventDefault()
      const base = seleccionTotal ? '' : value

      if (base.includes('.')) {
        const [, fraccion = ''] = base.split('.')
        if (fraccion.length >= 2) return
      } else if (base.length >= 9) {
        return
      }

      onChange(`${base}${tecla}`)
    }
  }

  return (
    <Input
      ref={ref}
      type="text"
      inputMode="decimal"
      value={mostrarDecimal(value)}
      onKeyDown={manejarTecla}
      // Cubre el pegado y el autocompletado, que no pasan por onKeyDown.
      onChange={(e) => onChange(limpiarDecimal(e.target.value))}
      onBlur={onBlur}
      {...props}
    />
  )
})

/**
 * Formulario generado a partir de la configuración de campos.
 *
 * Se apoya en la validación nativa de React Hook Form (required/min/max/
 * maxLength) en lugar de un esquema Zod por entidad: los servlets ya validan
 * en servidor y devuelven mensajes en español, así que aquí solo se cubre el
 * feedback inmediato y la conversión de tipos correcta (number vs string).
 */
export function FormularioCrud<T extends object>({
  campos,
  valoresIniciales,
  edicion,
  enviando,
  error,
  onEnviar,
  onCancelar,
}: {
  campos: CampoFormulario<T>[]
  valoresIniciales?: Partial<T>
  edicion: boolean
  enviando: boolean
  error: unknown
  onEnviar: (datos: Partial<T>) => void
  onCancelar: () => void
}) {
  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors },
  } = useForm<Record<string, unknown>>()

  const hoyISO = new Date().toISOString().slice(0, 10)
  // Fechas de hoy y mañana en la hora del usuario (no UTC), para los mínimos.
  const hoyLocal = hoyLocalISO()
  const mananaLocal = mananaLocalISO()

  // Firma estable del registro inicial (por contenido, no por identidad).
  // `campos` y `valoresIniciales` pueden recrearse en cada render del padre; si
  // el efecto de rehidratación dependiera de su identidad, cualquier re-render
  // ajeno al formulario —por ejemplo el refetch de React Query que dispara el
  // selector nativo de fecha/hora al abrirse— reiniciaría el formulario y
  // borraría lo que el usuario ya había escrito en los demás campos.
  const claveInicial = useMemo(
    () => JSON.stringify(valoresIniciales ?? null),
    [valoresIniciales],
  )

  // Al abrir el modal (crear o editar) se rehidrata el formulario.
  useEffect(() => {
    const valores: Record<string, unknown> = {}
    for (const campo of campos) {
      valores[campo.nombre] = aValorInput(campo.tipo, valoresIniciales?.[campo.nombre])
    }
    reset(valores)
    // Se depende de `claveInicial` (contenido) y no de la identidad de
    // `valoresIniciales`/`campos` para no reiniciar el formulario en cada
    // re-render del padre.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [claveInicial, reset])

  function alEnviar(valores: Record<string, unknown>) {
    const payload: Record<string, unknown> = {}

    for (const campo of campos) {
      if (edicion && campo.soloCreacion) continue

      const valor = valores[campo.nombre]

      // Campo opcional vacío -> null explícito (Gson lo acepta y la BD también).
      if (valor === '' || valor === undefined) {
        payload[campo.nombre] = campo.requerido ? '' : null
        continue
      }

      switch (campo.tipo) {
        case 'numero':
          payload[campo.nombre] = Number.parseInt(String(valor), 10)
          break
        case 'decimal':
          payload[campo.nombre] = Number.parseFloat(String(valor))
          break
        case 'select':
          payload[campo.nombre] = campo.opcionesNumericas
            ? Number.parseInt(String(valor), 10)
            : valor
          break
        case 'booleano':
          payload[campo.nombre] = valor === true || valor === 'true'
          break
        case 'fechaHora':
          payload[campo.nombre] = desdeInputDateTime(String(valor))
          break
        default:
          payload[campo.nombre] = valor
      }
    }

    onEnviar(payload as Partial<T>)
  }

  return (
    <form onSubmit={handleSubmit(alEnviar)} className="space-y-4">
      {Boolean(error) && <MensajeError error={error} />}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {campos.map((campo) => {
          const bloqueado = edicion && campo.soloCreacion
          const mensajeError = errors[campo.nombre]?.message as string | undefined
          // Los campos decimales usan el formato decimal por defecto (filtra a
          // 2 cifras y valida el número) salvo que el campo defina otro.
          const formato = campo.formato
            ? FORMATOS[campo.formato]
            : campo.tipo === 'decimal'
              ? FORMATOS.decimal
              : undefined
          const limiteCaracteres = campo.maxLength ?? formato?.maxLength

          // Valor original del campo en el formato del input, para el mínimo al
          // editar (no se puede mover la fecha a una anterior a la del registro).
          const brutoOriginal = valoresIniciales?.[campo.nombre]
          const fechaOriginal =
            campo.tipo === 'fechaHora'
              ? aInputDateTime(brutoOriginal as string | undefined)
              : ((brutoOriginal as string | undefined) ?? '')

          // `minFecha`: al crear el mínimo es hoy o mañana; al editar, la fecha
          // original del registro. Así no se agendan citas en días pasados ni se
          // pone una caducidad de hoy o anterior, sin bloquear el guardado de
          // registros cuya fecha ya pasó.
          const comoInput = (fecha: string) =>
            campo.tipo === 'fechaHora' ? `${fecha}T00:00` : fecha
          const minimoFecha =
            campo.minFecha === undefined
              ? undefined
              : edicion
                ? fechaOriginal || undefined
                : comoInput(campo.minFecha === 'manana' ? mananaLocal : hoyLocal)

          const reglas = {
            required: campo.requerido && !bloqueado ? 'Este campo es obligatorio' : false,
            maxLength: limiteCaracteres
              ? { value: limiteCaracteres, message: `Máximo ${limiteCaracteres} caracteres` }
              : undefined,
            minLength: formato?.minLength
              ? {
                  value: formato.minLength,
                  message: `Debe tener al menos ${formato.minLength} caracteres`,
                }
              : undefined,
            pattern: formato
              ? { value: formato.patron, message: formato.mensaje }
              : undefined,
            min:
              campo.min !== undefined
                ? { value: campo.min, message: `El valor mínimo es ${campo.min}` }
                : minimoFecha
                  ? {
                      value: minimoFecha,
                      message: edicion
                        ? 'La fecha no puede ser anterior a la original'
                        : campo.minFecha === 'manana'
                          ? 'La fecha debe ser posterior a hoy'
                          : 'La fecha no puede ser anterior a hoy',
                    }
                  : undefined,
            max:
              campo.max !== undefined
                ? { value: campo.max, message: `El valor máximo es ${campo.max}` }
                : campo.maxHoy
                  ? { value: hoyISO, message: 'La fecha no puede ser futura' }
                  : undefined,
          }

          // Los campos decimales usan un input controlado (`Controller`), así que
          // no se registran como no controlados para no duplicar el registro.
          const registro =
            campo.tipo === 'decimal'
              ? null
              : campo.tipo === 'booleano'
                ? register(campo.nombre)
                : register(campo.nombre, reglas)

          // Bloquea los caracteres inválidos mientras se escribe (p. ej. letras
          // en un teléfono o números en un nombre) antes de que RHF los lea.
          const manejarCambio = (e: React.ChangeEvent<HTMLInputElement>) => {
            if (formato?.filtrar) {
              e.target.value = formato.filtrar(e.target.value)
            }
            void registro?.onChange(e)
          }

          return (
            <div
              key={campo.nombre}
              className={campo.anchoCompleto || campo.tipo === 'textarea' ? 'sm:col-span-2' : ''}
            >
              <Campo
                etiqueta={campo.etiqueta}
                requerido={campo.requerido && !bloqueado}
                error={mensajeError}
                ayuda={bloqueado ? 'No editable después de la creación' : campo.ayuda}
              >
                {campo.tipo === 'select' ? (
                  <Select disabled={bloqueado} {...registro}>
                    <option value="">Seleccionar</option>
                    {campo.opciones?.map((o) => (
                      <option key={o.valor} value={o.valor}>
                        {o.etiqueta}
                      </option>
                    ))}
                  </Select>
                ) : campo.tipo === 'textarea' ? (
                  <TextArea
                    disabled={bloqueado}
                    placeholder={campo.placeholder}
                    {...registro}
                  />
                ) : campo.tipo === 'booleano' ? (
                  <Select disabled={bloqueado} {...registro}>
                    <option value="true">Sí</option>
                    <option value="false">No</option>
                  </Select>
                ) : campo.tipo === 'contrasena' ? (
                  <InputContrasena
                    disabled={bloqueado}
                    placeholder={campo.placeholder}
                    {...registro}
                  />
                ) : campo.tipo === 'decimal' ? (
                  <Controller
                    name={campo.nombre}
                    control={control}
                    rules={reglas}
                    render={({ field }) => (
                      <InputDecimal
                        ref={field.ref}
                        name={field.name}
                        value={String(field.value ?? '')}
                        onChange={field.onChange}
                        onBlur={field.onBlur}
                        disabled={bloqueado}
                        placeholder={campo.placeholder}
                      />
                    )}
                  />
                ) : (
                  <Input
                    type={
                      campo.tipo === 'fecha'
                        ? 'date'
                        : campo.tipo === 'fechaHora'
                          ? 'datetime-local'
                          : campo.tipo === 'numero'
                            ? 'number'
                            : 'text'
                    }
                    min={minimoFecha}
                    max={campo.tipo === 'fecha' && campo.maxHoy ? hoyISO : undefined}
                    inputMode={formato?.inputMode}
                    maxLength={limiteCaracteres}
                    disabled={bloqueado}
                    placeholder={campo.placeholder}
                    {...registro}
                    onChange={manejarCambio}
                  />
                )}
              </Campo>
            </div>
          )
        })}
      </div>

      <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
        <Boton type="button" variante="secundario" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" cargando={enviando}>
          {edicion ? 'Guardar cambios' : 'Crear'}
        </Boton>
      </div>
    </form>
  )
}
