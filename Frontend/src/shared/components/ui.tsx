import { forwardRef, useState, type ReactNode } from 'react'
import { Eye, EyeOff, Inbox, Loader2, X } from 'lucide-react'
import { LogoIcono } from '@/shared/components/Logo'
import { cn } from '@/shared/lib/utils'

/* --------------------------------------------------------------- Button */

type VarianteBoton = 'primario' | 'secundario' | 'peligro' | 'fantasma'

const estilosBoton: Record<VarianteBoton, string> = {
  primario:
    'bg-brand-600 text-white hover:bg-brand-700 hover:shadow-marca focus-visible:ring-brand-500 shadow-sm',
  secundario:
    'bg-white text-slate-700 border border-slate-200 hover:bg-brand-50 hover:border-brand-200 hover:text-brand-800 focus-visible:ring-brand-400 shadow-sm',
  peligro: 'bg-red-600 text-white hover:bg-red-700 focus-visible:ring-red-500 shadow-sm',
  fantasma: 'text-slate-600 hover:bg-slate-100 hover:text-slate-800 focus-visible:ring-slate-400',
}

interface PropsBoton extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: VarianteBoton
  cargando?: boolean
}

export function Boton({
  variante = 'primario',
  cargando,
  className,
  children,
  disabled,
  ...props
}: PropsBoton) {
  return (
    <button
      className={cn(
        'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg px-3.5 py-2 text-sm font-medium transition-all duration-200 active:scale-[0.98]',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2',
        'disabled:pointer-events-none disabled:opacity-50 disabled:active:scale-100',
        estilosBoton[variante],
        className,
      )}
      disabled={disabled || cargando}
      {...props}
    >
      {cargando && <Loader2 className="h-4 w-4 animate-spin" />}
      {children}
    </button>
  )
}

/* ------------------------------------------------------- Campos de formulario */

const claseControl =
  'w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 ' +
  'placeholder:text-slate-400 hover:border-slate-300 focus:border-brand-500 focus:outline-none focus:ring-2 ' +
  'focus:ring-brand-500/15 disabled:bg-slate-50 disabled:text-slate-400 shadow-sm transition-colors'

export const Input = forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input ref={ref} className={cn(claseControl, className)} {...props} />
  ),
)
Input.displayName = 'Input'

export const Select = forwardRef<HTMLSelectElement, React.SelectHTMLAttributes<HTMLSelectElement>>(
  ({ className, ...props }, ref) => (
    <select ref={ref} className={cn(claseControl, 'pr-8', className)} {...props} />
  ),
)
Select.displayName = 'Select'

/** Input de contraseña con ícono de ojo para mostrar/ocultar el valor. */
export const InputContrasena = forwardRef<
  HTMLInputElement,
  React.InputHTMLAttributes<HTMLInputElement>
>(({ className, ...props }, ref) => {
  const [mostrar, setMostrar] = useState(false)
  return (
    <div className="relative">
      <Input ref={ref} type={mostrar ? 'text' : 'password'} className={cn('pr-10', className)} {...props} />
      <button
        type="button"
        onClick={() => setMostrar((v) => !v)}
        className="absolute inset-y-0 right-0 flex items-center px-3 text-slate-400 hover:text-slate-600"
        tabIndex={-1}
        aria-label={mostrar ? 'Ocultar contraseña' : 'Mostrar contraseña'}
      >
        {mostrar ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
      </button>
    </div>
  )
})
InputContrasena.displayName = 'InputContrasena'

export const TextArea = forwardRef<
  HTMLTextAreaElement,
  React.TextareaHTMLAttributes<HTMLTextAreaElement>
>(({ className, ...props }, ref) => (
  <textarea ref={ref} rows={3} className={cn(claseControl, 'resize-y', className)} {...props} />
))
TextArea.displayName = 'TextArea'

export function Campo({
  etiqueta,
  error,
  requerido,
  ayuda,
  children,
}: {
  etiqueta: string
  error?: string
  requerido?: boolean
  ayuda?: string
  children: ReactNode
}) {
  return (
    <label className="block space-y-1.5">
      <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">
        {etiqueta}
        {requerido && <span className="ml-0.5 text-red-500">*</span>}
      </span>
      {children}
      {ayuda && !error && <span className="block text-xs text-slate-400">{ayuda}</span>}
      {error && <span className="block text-xs font-medium text-red-600">{error}</span>}
    </label>
  )
}

/* ---------------------------------------------------------------- Badge */

type TonoBadge = 'neutro' | 'exito' | 'alerta' | 'peligro' | 'info'

const estilosBadge: Record<TonoBadge, string> = {
  neutro: 'bg-slate-100 text-slate-600 ring-1 ring-slate-200',
  exito: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200',
  alerta: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200',
  peligro: 'bg-red-50 text-red-700 ring-1 ring-red-200',
  info: 'bg-acento-50 text-acento-700 ring-1 ring-acento-200',
}

export function Badge({ tono = 'neutro', children }: { tono?: TonoBadge; children: ReactNode }) {
  return (
    <span
      className={cn(
        'inline-flex items-center whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-medium',
        estilosBadge[tono],
      )}
    >
      {children}
    </span>
  )
}

/* ---------------------------------------------------------------- Modal */

export function Modal({
  abierto,
  titulo,
  descripcion,
  onCerrar,
  children,
  ancho = 'max-w-lg',
}: {
  abierto: boolean
  titulo: string
  descripcion?: string
  onCerrar: () => void
  children: ReactNode
  ancho?: string
}) {
  if (!abierto) return null

  return (
    <div className="animate-aparecer fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-brand-950/60 p-3 backdrop-blur-sm sm:p-8">
      <div
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
        className={cn(
          'animate-escala w-full rounded-xl bg-white shadow-2xl ring-1 ring-slate-200',
          'my-auto max-h-[calc(100dvh-1.5rem)] overflow-y-auto sm:max-h-[calc(100dvh-4rem)]',
          ancho,
        )}
      >
        <div className="sticky top-0 z-10 flex items-start justify-between gap-3 border-b border-slate-100 bg-white/95 px-5 py-4 backdrop-blur sm:px-6">
          <div className="min-w-0">
            <h2 className="text-base font-semibold text-slate-900">{titulo}</h2>
            {descripcion && <p className="mt-0.5 text-sm text-slate-500">{descripcion}</p>}
          </div>
          <button
            onClick={onCerrar}
            aria-label="Cerrar"
            className="shrink-0 rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
        <div className="px-5 py-5 sm:px-6">{children}</div>
      </div>
    </div>
  )
}

/* --------------------------------------------------------- Estados vacíos */

export function Cargando({ texto = 'Cargando…' }: { texto?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-sm text-slate-400">
      <span className="relative flex h-12 w-12 items-center justify-center">
        <span className="absolute inset-0 animate-ping rounded-full bg-brand-200/50" />
        <span className="relative flex h-12 w-12 items-center justify-center rounded-full bg-brand-50 ring-1 ring-brand-100">
          <LogoIcono alt="" className="h-6 w-6 animate-pulso-suave" />
        </span>
      </span>
      {texto}
    </div>
  )
}

export function MensajeError({ error }: { error: unknown }) {
  const mensaje =
    error instanceof Error && error.message
      ? error.message
      : 'Ocurrió un error inesperado. Intenta nuevamente.'
  return (
    <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {mensaje}
    </div>
  )
}

export function SinDatos({ mensaje = 'No hay registros para mostrar' }: { mensaje?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 px-4 py-16 text-center text-sm text-slate-400">
      <Inbox className="h-7 w-7 text-slate-300" />
      {mensaje}
    </div>
  )
}

/* -------------------------------------------------------------- Cabecera */

export function CabeceraPagina({
  titulo,
  descripcion,
  acciones,
}: {
  titulo: string
  descripcion?: string
  acciones?: ReactNode
}) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:mb-7 sm:flex-row sm:flex-wrap sm:items-end sm:justify-between">
      <div className="min-w-0">
        <h1 className="text-xl font-bold tracking-tight text-slate-900 sm:text-2xl">{titulo}</h1>
        {descripcion && <p className="mt-1 text-sm text-slate-500">{descripcion}</p>}
      </div>
      {acciones && <div className="flex flex-wrap items-center gap-2">{acciones}</div>}
    </div>
  )
}

export function Tarjeta({
  titulo,
  acciones,
  children,
  className,
}: {
  titulo?: string
  acciones?: ReactNode
  children: ReactNode
  className?: string
}) {
  return (
    <section
      className={cn(
        'overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm',
        className,
      )}
    >
      {(titulo || acciones) && (
        <header className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 bg-brand-50/60 px-4 py-3 sm:px-5 sm:py-3.5">
          {titulo && (
            <h2 className="text-xs font-semibold uppercase tracking-wide text-brand-700">
              {titulo}
            </h2>
          )}
          {acciones}
        </header>
      )}
      <div className="p-4 sm:p-5">{children}</div>
    </section>
  )
}
