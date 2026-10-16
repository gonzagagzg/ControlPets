import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type { RolPermiso } from '@/shared/types/api'

/**
 * `rol_permiso` es la única tabla con llave compuesta, y su servlet
 * (RolPermisoServlet) la recibe por query string en vez de por path:
 *
 *   GET|PUT|DELETE /api/rol-permisos?idRol=1&idPermiso=4
 *
 * Por eso no puede usar `crearHooksCrud`, que asume `/{id}` en la ruta.
 */

const RUTA = '/api/rol-permisos'
const CLAVE = ['rol-permisos'] as const

export function useRolPermisos() {
  return useQuery<RolPermiso[]>({
    queryKey: CLAVE,
    queryFn: () => api.get<RolPermiso[]>(RUTA),
  })
}

export function useAsignarPermiso() {
  const qc = useQueryClient()
  return useMutation<RolPermiso, Error, RolPermiso>({
    mutationFn: (asignacion) => api.post<RolPermiso>(RUTA, asignacion),
    onSuccess: () => qc.invalidateQueries({ queryKey: CLAVE }),
  })
}

export function useRevocarPermiso() {
  const qc = useQueryClient()
  return useMutation<void, Error, RolPermiso>({
    mutationFn: ({ idRol, idPermiso }) => api.delete(RUTA, { idRol, idPermiso }),
    onSuccess: () => qc.invalidateQueries({ queryKey: CLAVE }),
  })
}
