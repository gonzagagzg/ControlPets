import { ShieldAlert } from 'lucide-react'
import { Boton } from '@/shared/components/ui'
import { useBloqueoEmpresa } from '@/shared/session/bloqueo'
import { limpiarSesion } from '@/shared/session/sesion'

/**
 * Pantalla de bloqueo total cuando el backend responde 403 "veterinaria
 * desactivada" (falta de pago/arriendo). Se activa desde el interceptor
 * HTTP (shared/api/client.ts) para cualquier petición, no solo el login.
 */
export function BloqueoEmpresa() {
  const { bloqueada, mensaje } = useBloqueoEmpresa()

  if (!bloqueada) return null

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-brand-950/95 p-6 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-xl bg-white p-8 text-center shadow-2xl">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-red-100 text-red-600">
          <ShieldAlert className="h-6 w-6" />
        </div>
        <h2 className="mt-4 text-lg font-bold text-slate-900">Veterinaria desactivada</h2>
        <p className="mt-2 text-sm leading-relaxed text-slate-500">
          {mensaje ?? 'Esta cuenta se encuentra desactivada. Contacte al administrador del sistema.'}
        </p>
        <Boton
          variante="secundario"
          className="mt-6 w-full"
          onClick={() => {
            limpiarSesion()
            window.location.assign('/acceso')
          }}
        >
          Volver al inicio de sesión
        </Boton>
      </div>
    </div>
  )
}
