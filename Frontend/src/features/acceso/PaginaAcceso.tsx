import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { LockKeyhole, ShieldAlert } from 'lucide-react'
import { useLogin, useNotificarBloqueo } from '@/shared/api/auth'
import { ApiError } from '@/shared/api/client'
import { MarcaControlPets } from '@/shared/components/Logo'
import { Boton, Campo, Input, InputContrasena, Modal } from '@/shared/components/ui'
import { useSesion } from '@/shared/session/sesion'

/**
 * Login real contra POST /api/auth/login.
 *
 * El backend valida usuario/contraseña, empresa y usuario activos, y
 * devuelve un JWT con idEmpresa/idRol/rol embebidos — ya no se elige la
 * empresa a mano: el token la trae resuelta (ver AuthServlet/AuthFilter).
 */
export function PaginaAcceso() {
  const navigate = useNavigate()
  const { token, establecer } = useSesion()
  const login = useLogin()
  const notificar = useNotificarBloqueo()

  const [usuario, setUsuario] = useState('')
  const [clave, setClave] = useState('')
  const [bloqueo, setBloqueo] = useState<'pago' | 'tecnico' | null>(null)
  const [notificado, setNotificado] = useState(false)

  if (token) return <Navigate to="/app" replace />

  function enviar(e: FormEvent) {
    e.preventDefault()
    if (!usuario.trim() || !clave) return

    login.mutate(
      { usuario: usuario.trim(), clave },
      {
        onSuccess: (r) => {
          establecer({
            token: r.token,
            idEmpresa: r.idEmpresa,
            idUsuario: r.idUsuario,
            idRol: r.idRol,
            rol: r.rol,
            nombreUsuario: r.nombres,
          })
          navigate('/app', { replace: true })
        },
        onError: (err) => {
          if (err instanceof ApiError) {
            if (/falta de pago/.test(err.message)) {
              setNotificado(false)
              setBloqueo('pago')
            } else if (/motivo técnico/.test(err.message)) {
              setNotificado(false)
              setBloqueo('tecnico')
            }
          }
        },
      },
    )
  }

  function notificarAlAdministrador() {
    notificar.mutate(
      { usuario: usuario.trim() },
      { onSuccess: () => setNotificado(true) },
    )
  }

  const error = login.error
  const mensajeError =
    error instanceof ApiError
      ? error.message
      : error
        ? 'No se pudo iniciar sesión. Intente nuevamente.'
        : null

  return (
    <div className="flex min-h-screen">
      {/* Panel izquierdo: identidad clínica */}
      <div className="hidden flex-col justify-between bg-gradient-to-br from-brand-700 via-brand-800 to-brand-950 p-12 lg:flex lg:w-5/12">
        <Link to="/" className="flex items-center" aria-label="ControlPets">
          <MarcaControlPets tamano="lg" />
        </Link>

        <div>
          <p className="text-4xl font-bold leading-snug text-white">
            Gestión clínica<br />
            <span className="text-acento-300">de precisión.</span>
          </p>
          <p className="mt-4 text-base leading-relaxed text-brand-300">
            Agenda, historiales, vacunación e inventario en un solo sistema diseñado para el equipo veterinario.
          </p>
        </div>

        <div className="flex gap-6 text-xs text-brand-300">
          <span>Clínica · Comercial · Administración</span>
        </div>
      </div>

      <div className="relative flex flex-1 flex-col items-center justify-center bg-brand-50 p-5 sm:p-8">
        {/* Burbujas decorativas del panel de acceso */}
        <div aria-hidden="true" className="pointer-events-none absolute inset-x-0 top-0 -z-10 overflow-hidden">
          <span className="absolute left-8 top-16 h-36 w-36 animate-flotar rounded-full bg-acento-300/15 blur-3xl" />
          <span
            className="absolute right-10 top-32 h-24 w-24 animate-flotar rounded-full bg-brand-200/20 blur-2xl"
            style={{ animationDelay: '2s' }}
          />
        </div>

        <div className="w-full max-w-sm animate-escala">
          {/* Logo móvil */}
          <Link to="/" className="mb-8 flex items-center lg:hidden">
            <MarcaControlPets tamano="md" variante="oscuro" />
          </Link>

          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Iniciar sesión</h2>
          <p className="mt-1 text-sm text-slate-500">Ingrese sus credenciales de acceso.</p>

          <form className="mt-6 space-y-4" onSubmit={enviar}>
            <Campo etiqueta="Usuario" requerido>
              <Input
                autoFocus
                autoComplete="username"
                value={usuario}
                onChange={(e) => setUsuario(e.target.value)}
                maxLength={50}
                placeholder="usuario"
              />
            </Campo>

            <Campo etiqueta="Contraseña" requerido>
              <InputContrasena
                autoComplete="current-password"
                value={clave}
                onChange={(e) => setClave(e.target.value)}
                placeholder="••••••••"
              />
            </Campo>

            <div className="flex justify-end">
              <Link to="/recuperar-password" className="text-xs font-medium text-acento-600 hover:text-acento-700 hover:underline">
                ¿Olvidaste tu contraseña?
              </Link>
            </div>

            {!bloqueo && mensajeError && (
              <div className="flex gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                <LockKeyhole className="mt-0.5 h-3.5 w-3.5 shrink-0" />
                <p>{mensajeError}</p>
              </div>
            )}

            <Boton
              type="submit"
              className="mt-2 w-full"
              disabled={!usuario.trim() || !clave || login.isPending}
            >
              {login.isPending ? 'Ingresando…' : 'Entrar al sistema'}
            </Boton>
          </form>
        </div>
      </div>

      <Modal
        abierto={bloqueo !== null}
        titulo="Usuario bloqueado"
        onCerrar={() => setBloqueo(null)}
      >
        <div className="text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-red-100 text-red-600">
            <ShieldAlert className="h-6 w-6" />
          </div>
          <p className="mt-4 text-sm leading-relaxed text-slate-600">
            {bloqueo === 'pago'
              ? 'Su cuenta se encuentra bloqueada por falta de pago. Para reactivarla, notifique al administrador.'
              : 'Su cuenta se encuentra bloqueada por motivo técnico. Para reactivarla, notifique al administrador.'}
          </p>
          {notificado && (
            <p className="mt-3 rounded-lg bg-emerald-50 p-3 text-xs font-medium text-emerald-700 ring-1 ring-emerald-200">
              Notificación enviada al administrador
            </p>
          )}
          {notificar.error && (
            <p className="mt-3 rounded-lg bg-red-50 p-3 text-xs font-medium text-red-700 ring-1 ring-red-200">
              No se pudo enviar la notificación. Intente nuevamente.
            </p>
          )}
        </div>

        <div className="mt-6 flex flex-col gap-2">
          {bloqueo === 'tecnico' ? (
            <Boton
              cargando={notificar.isPending}
              disabled={notificado}
              onClick={notificarAlAdministrador}
            >
              Notificar al Administrador
            </Boton>
          ) : (
            <Boton
              cargando={notificar.isPending}
              disabled={notificado}
              onClick={notificarAlAdministrador}
            >
              Pago Realizado - Notificar al Administrador
            </Boton>
          )}
          <Boton variante="secundario" onClick={() => setBloqueo(null)}>
            Salir
          </Boton>
        </div>
      </Modal>
    </div>
  )
}
