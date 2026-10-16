import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { CheckCircle2 } from 'lucide-react'
import { useRestablecerPassword } from '@/shared/api/auth'
import { MarcaControlPets } from '@/shared/components/Logo'
import { Boton, Campo, InputContrasena, MensajeError } from '@/shared/components/ui'

/**
 * Consume el token de recuperación y fija la nueva contraseña
 * (POST /api/auth/restablecer-password). El token llega por query string
 * desde el enlace enviado por correo: /restablecer-password?token=...
 */
export function PaginaRestablecerPassword() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const token = params.get('token') ?? ''
  const restablecer = useRestablecerPassword()

  const [nuevaClave, setNuevaClave] = useState('')
  const [confirmar, setConfirmar] = useState('')

  const claveCorta = nuevaClave.length > 0 && nuevaClave.length < 6
  const noCoincide = confirmar.length > 0 && confirmar !== nuevaClave

  function enviar(e: FormEvent) {
    e.preventDefault()
    if (!token || !nuevaClave || claveCorta || noCoincide) return
    restablecer.mutate({ token, nuevaClave })
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-brand-50 p-8">
      <div className="w-full max-w-sm">
        <Link to="/" className="mb-8 flex items-center">
          <MarcaControlPets tamano="md" variante="oscuro" />
        </Link>

        <h2 className="text-2xl font-bold tracking-tight text-slate-900">Nueva contraseña</h2>
        <p className="mt-1 text-sm text-slate-500">Ingresa tu nueva contraseña de acceso.</p>

        {!token ? (
          <div className="mt-6 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            El enlace no incluye un token de recuperación válido. Solicita uno nuevo.
          </div>
        ) : restablecer.isSuccess ? (
          <div className="mt-6 flex gap-2 rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
            <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0" />
            <p>Contraseña actualizada correctamente. Ya puedes iniciar sesión.</p>
          </div>
        ) : (
          <form className="mt-6 space-y-4" onSubmit={enviar}>
            <Campo
              etiqueta="Nueva contraseña"
              requerido
              ayuda="Mínimo 6 caracteres"
              error={claveCorta ? 'La contraseña debe tener al menos 6 caracteres' : undefined}
            >
              <InputContrasena
                autoFocus
                autoComplete="new-password"
                value={nuevaClave}
                onChange={(e) => setNuevaClave(e.target.value)}
              />
            </Campo>
            <Campo
              etiqueta="Confirmar contraseña"
              requerido
              error={noCoincide ? 'Las contraseñas no coinciden' : undefined}
            >
              <InputContrasena
                autoComplete="new-password"
                value={confirmar}
                onChange={(e) => setConfirmar(e.target.value)}
              />
            </Campo>

            {restablecer.error && <MensajeError error={restablecer.error} />}

            <Boton
              type="submit"
              className="w-full"
              disabled={!nuevaClave || claveCorta || noCoincide || restablecer.isPending}
              cargando={restablecer.isPending}
            >
              Restablecer contraseña
            </Boton>
          </form>
        )}

        {restablecer.isSuccess ? (
          <Boton className="mt-6 w-full" onClick={() => navigate('/acceso')}>
            Ir a iniciar sesión
          </Boton>
        ) : (
          <Link to="/acceso" className="mt-6 block text-center text-sm text-acento-600 hover:text-acento-700 hover:underline">
            Volver a iniciar sesión
          </Link>
        )}
      </div>
    </div>
  )
}
