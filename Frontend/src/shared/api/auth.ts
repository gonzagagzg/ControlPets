import { useMutation } from '@tanstack/react-query'
import { api } from './client'
import type {
  ConfirmarCuentaRequest,
  LoginRequest,
  LoginResponse,
  NotificarBloqueoRequest,
  RecuperarPasswordRequest,
  RestablecerPasswordRequest,
} from '@/shared/types/api'

/** POST /api/auth/login — único endpoint público, sin Authorization. */
export function useLogin() {
  return useMutation<LoginResponse, Error, LoginRequest>({
    mutationFn: (credenciales) => api.post<LoginResponse>('/api/auth/login', credenciales),
  })
}

/** POST /api/auth/notificar-bloqueo — público, el usuario bloqueado no tiene token. */
export function useNotificarBloqueo() {
  return useMutation<void, Error, NotificarBloqueoRequest>({
    mutationFn: (body) => api.post<void>('/api/auth/notificar-bloqueo', body),
  })
}

/** POST /api/auth/recuperar-password — envía el correo con el token de recuperación. */
export function useRecuperarPassword() {
  return useMutation<void, Error, RecuperarPasswordRequest>({
    mutationFn: (datos) => api.post<void>('/api/auth/recuperar-password', datos),
  })
}

/** POST /api/auth/restablecer-password — consume el token y fija la nueva clave. */
export function useRestablecerPassword() {
  return useMutation<void, Error, RestablecerPasswordRequest>({
    mutationFn: (datos) => api.post<void>('/api/auth/restablecer-password', datos),
  })
}

/** POST /api/auth/confirmar-cuenta — público, consume el token de confirmación de correo. */
export function useConfirmarCuenta() {
  return useMutation<void, Error, ConfirmarCuentaRequest>({
    mutationFn: (datos) => api.post<void>('/api/auth/confirmar-cuenta', datos),
  })
}
