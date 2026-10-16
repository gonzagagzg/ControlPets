import { useMemo } from 'react'
import { useRolPermisos } from '@/features/admin/api/rolPermisos'
import { permisosApi } from '@/shared/api/recursos'
import { esSuperUsuario, useSesion } from '@/shared/session/sesion'

/**
 * Permisos efectivos del usuario autenticado.
 *
 * El login (LoginResponse) y el JWT NO incluyen la lista de permisos —
 * el backend solo embebe idRol/rol y evalúa (modulo,accion) contra
 * rol_permiso en cada petición (ver Autorizacion.exigir en el backend).
 * Por eso el frontend arma su propia vista de "qué puedo ver" cruzando
 * /api/permisos y /api/rol-permisos y filtrando por el idRol de sesión.
 *
 * Esto SOLO sirve para decidir qué mostrar/ocultar en la UI (mejor
 * experiencia). El backend re-valida cada operación de forma
 * independiente y es la única fuente de verdad de seguridad.
 */
export function usePermisos() {
  const idRol = useSesion((s) => s.idRol)
  const rol = useSesion((s) => s.rol)
  const esSuper = esSuperUsuario(rol)

  const { data: permisos, isLoading: cargandoPermisos } = permisosApi.useLista()
  const { data: rolPermisos, isLoading: cargandoAsignaciones } = useRolPermisos()

  const modulosAcciones = useMemo(() => {
    if (!permisos || !rolPermisos || idRol == null) return new Set<string>()
    const idsPermisoDelRol = new Set(
      rolPermisos.filter((rp) => rp.idRol === idRol).map((rp) => rp.idPermiso),
    )
    return new Set(
      permisos
        .filter((p) => idsPermisoDelRol.has(p.idPermiso))
        .map((p) => clave(p.modulo, p.accion)),
    )
  }, [permisos, rolPermisos, idRol])

  const modulosVisibles = useMemo(() => {
    if (!permisos || !rolPermisos || idRol == null) return new Set<string>()
    const idsPermisoDelRol = new Set(
      rolPermisos.filter((rp) => rp.idRol === idRol).map((rp) => rp.idPermiso),
    )
    return new Set(
      permisos.filter((p) => idsPermisoDelRol.has(p.idPermiso)).map((p) => p.modulo.toUpperCase()),
    )
  }, [permisos, rolPermisos, idRol])

  return {
    cargando: cargandoPermisos || cargandoAsignaciones,
    esSuperUsuario: esSuper,
    /** SuperUsuario nunca pasa por esta comprobación en el backend: siempre "puede". */
    puede: (modulo: string, accion: string) =>
      esSuper || modulosAcciones.has(clave(modulo, accion)),
    tieneAccesoAModulo: (modulo: string) => esSuper || modulosVisibles.has(modulo.toUpperCase()),
  }
}

function clave(modulo: string, accion: string) {
  return `${modulo.toUpperCase()}::${accion.toUpperCase()}`
}
