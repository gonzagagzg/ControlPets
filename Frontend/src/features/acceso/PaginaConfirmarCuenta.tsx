import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { AlertTriangle, CheckCircle2, Loader2, MailCheck } from 'lucide-react'
import { api } from '@/shared/api/client'
import { MarcaControlPets } from '@/shared/components/Logo'
import { Boton, MensajeError } from '@/shared/components/ui'

/**
 * Confirma el correo de una cuenta recién creada al registrar una veterinaria.
 *
 * El enlace enviado por correo apunta aquí con el token:
 * /confirmar-cuenta?token=... Al abrir la página se consume el token
 * (POST /api/auth/confirmar-cuenta) y, si todo va bien, se muestra el
 * resultado con los estilos del aplicativo y se redirige al login.
 *
 * La confirmación se hace con el cliente HTTP directo y estado local (no con
 * una mutación de React Query) a propósito: el token es de un solo uso y se
 * consume una única vez desde un efecto de montaje. Apoyarse en el estado de
 * una mutación hacía que, con el doble montaje de StrictMode en desarrollo, la
 * pantalla se quedara en "Confirmando tu cuenta…" aunque el servidor ya hubiera
 * activado la cuenta.
 */
const SEGUNDOS_REDIRECCION = 10

type EstadoConfirmacion = 'confirmando' | 'confirmada' | 'error'

export function PaginaConfirmarCuenta() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const token = params.get('token') ?? ''

  const [estado, setEstado] = useState<EstadoConfirmacion>('confirmando')
  const [error, setError] = useState<unknown>(null)
  const [segundos, setSegundos] = useState(SEGUNDOS_REDIRECCION)

  // El token es de un solo uso: se consume exactamente una vez, aunque
  // StrictMode monte el efecto dos veces en desarrollo. El ref sobrevive al
  // ciclo montar/desmontar que simula StrictMode.
  const enviado = useRef(false)

  useEffect(() => {
    if (enviado.current) return
    enviado.current = true

    if (!token) {
      setEstado('error')
      return
    }

    api
      .post<void>('/api/auth/confirmar-cuenta', { token })
      .then(() => setEstado('confirmada'))
      .catch((causa: unknown) => {
        setError(causa)
        setEstado('error')
      })
  }, [token])

  // Cuenta regresiva hacia el login una vez confirmada la cuenta.
  useEffect(() => {
    if (estado !== 'confirmada') return
    const id = window.setInterval(
      () => setSegundos((s) => (s <= 1 ? 0 : s - 1)),
      1000,
    )
    return () => window.clearInterval(id)
  }, [estado])

  useEffect(() => {
    if (estado === 'confirmada' && segundos === 0) {
      navigate('/acceso', { replace: true })
    }
  }, [estado, segundos, navigate])

  const confirmada = estado === 'confirmada'

  return (
    <div className="flex min-h-screen items-center justify-center bg-brand-50 p-8">
      <div className="w-full max-w-sm">
        <Link to="/" className="mb-8 flex items-center">
          <MarcaControlPets tamano="md" variante="oscuro" />
        </Link>

        <h2 className="text-2xl font-bold tracking-tight text-slate-900">Confirmación de cuenta</h2>
        <p className="mt-1 text-sm text-slate-500">
          Estamos verificando el correo con el que se registró tu veterinaria.
        </p>

        {!token ? (
          <div className="mt-6 flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
            <span>El enlace no incluye un token de confirmación válido.</span>
          </div>
        ) : confirmada ? (
          <div className="mt-6 flex items-start gap-2 rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
            <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0" />
            <span>
              ¡Cuenta confirmada! Ya puedes iniciar sesión.
              <br />
              Te llevaremos al inicio de sesión en{' '}
              <span className="font-semibold">{segundos}</span>{' '}
              {segundos === 1 ? 'segundo' : 'segundos'}.
            </span>
          </div>
        ) : estado === 'error' ? (
          <div className="mt-6">
            <div className="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
              <span>
                No pudimos confirmar la cuenta. El enlace puede haber expirado o ya
                fue utilizado.
              </span>
            </div>
            {error ? (
              <div className="mt-3">
                <MensajeError error={error} />
              </div>
            ) : null}
          </div>
        ) : (
          <div className="mt-6 flex items-center gap-2 rounded-lg border border-slate-200 bg-white p-4 text-sm text-slate-600">
            <Loader2 className="h-4 w-4 shrink-0 animate-spin text-acento-600" />
            <span>Confirmando tu cuenta…</span>
          </div>
        )}

        {confirmada ? (
          <Boton className="mt-6 w-full" onClick={() => navigate('/acceso', { replace: true })}>
            Ir a iniciar sesión
          </Boton>
        ) : (
          <Link
            to="/acceso"
            className="mt-6 flex items-center justify-center gap-1 text-center text-sm text-acento-600 hover:text-acento-700 hover:underline"
          >
            <MailCheck className="h-4 w-4" />
            Volver a iniciar sesión
          </Link>
        )}

        {token && estado === 'error' && (
          <p className="mt-4 text-center text-xs text-slate-400">
            Puedes solicitar un nuevo enlace desde la pantalla de inicio de sesión.
          </p>
        )}
      </div>
    </div>
  )
}
