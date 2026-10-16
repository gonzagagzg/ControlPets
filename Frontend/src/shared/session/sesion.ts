import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Uuid } from '@/shared/types/api'
import { useBloqueoEmpresa } from '@/shared/session/bloqueo'

/**
 * Sesión de autenticación.
 *
 * Respaldada por el JWT que devuelve POST /api/auth/login (ver AuthServlet /
 * AuthFilter en el backend). El token es la única fuente de verdad para el
 * backend: `idEmpresa` e `idRol` viajan como claims firmados, así que aunque
 * el usuario edite este store desde devtools no gana ningún permiso — el
 * AuthFilter vuelve a resolver todo desde el token en cada petición.
 */

const ES_SUPER_USUARIO = (rol: string | null) => rol?.toLowerCase() === 'superusuario'

interface EstadoSesion {
  token: string | null
  idEmpresa: Uuid | null
  idUsuario: Uuid | null
  idRol: number | null
  rol: string | null
  nombreUsuario: string | null
  establecer: (datos: {
    token: string
    idEmpresa: Uuid | null
    idUsuario: Uuid
    idRol: number
    rol: string
    nombreUsuario: string
  }) => void
  limpiar: () => void
}

export const useSesion = create<EstadoSesion>()(
  persist(
    (set) => ({
      token: null,
      idEmpresa: null,
      idUsuario: null,
      idRol: null,
      rol: null,
      nombreUsuario: null,
      establecer: (datos) => {
        useBloqueoEmpresa.getState().desbloquear()
        set(datos)
      },
      limpiar: () =>
        set({
          token: null,
          idEmpresa: null,
          idUsuario: null,
          idRol: null,
          rol: null,
          nombreUsuario: null,
        }),
    }),
    {
      name: 'vet-sesion',
      // v2: el store pre-JWT persistía {idEmpresa,idUsuario,...} sin `token`.
      // Sin este bump, zustand rehidrata esos campos viejos y deja un estado
      // inconsistente (idUsuario con valor pero token null) que provoca un
      // loop de redirección infinito entre /acceso y /app.
      version: 2,
      migrate: () => ({
        token: null,
        idEmpresa: null,
        idUsuario: null,
        idRol: null,
        rol: null,
        nombreUsuario: null,
      }),
    },
  ),
)

/** Lee el token fuera de un componente React (para el interceptor HTTP). */
export function obtenerToken(): string | null {
  return useSesion.getState().token
}

/** Limpia la sesión fuera de un componente React (para el interceptor HTTP en 401). */
export function limpiarSesion(): void {
  useSesion.getState().limpiar()
}

export function useEsSuperUsuario(): boolean {
  const rol = useSesion((s) => s.rol)
  return ES_SUPER_USUARIO(rol)
}

export function esSuperUsuario(rol: string | null): boolean {
  return ES_SUPER_USUARIO(rol)
}

/** Empresa activa garantizada; usar solo dentro de rutas protegidas no-SuperUsuario. */
export function useEmpresaActiva(): Uuid {
  const idEmpresa = useSesion((s) => s.idEmpresa)
  if (!idEmpresa) throw new Error('No hay empresa activa en la sesión')
  return idEmpresa
}

/**
 * Filtra por empresa activa en el cliente.
 *
 * El backend ya filtra por tenant a partir del token (ver `idEmpresa` /
 * `superUsuario` en cada Service), pero se conserva este helper para listas
 * cargadas por un SuperUsuario que sí ve todas las empresas y necesita
 * acotar la vista a una sola.
 */
export function filtrarPorEmpresa<T extends { idEmpresa?: Uuid }>(
  registros: T[] | undefined,
  idEmpresa: Uuid | null,
): T[] {
  if (!registros) return []
  if (!idEmpresa) return registros
  return registros.filter((r) => !r.idEmpresa || r.idEmpresa === idEmpresa)
}
