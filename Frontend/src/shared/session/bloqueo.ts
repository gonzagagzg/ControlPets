import { create } from 'zustand'

/**
 * Estado global para el bloqueo por veterinaria (empresa) desactivada.
 *
 * El AuthFilter del backend responde 403 en TODA petición cuando la
 * empresa del usuario está inactiva (falta de pago / arriendo), no solo
 * en el login. El interceptor HTTP (shared/api/client.ts) detecta ese
 * caso puntual y activa este store; `BloqueoEmpresa` (app/layout) lo
 * escucha y cubre toda la pantalla con un aviso, sin cerrar la sesión.
 */
interface EstadoBloqueo {
  bloqueada: boolean
  mensaje: string | null
  bloquear: (mensaje: string) => void
  desbloquear: () => void
}

export const useBloqueoEmpresa = create<EstadoBloqueo>((set) => ({
  bloqueada: false,
  mensaje: null,
  bloquear: (mensaje) => set({ bloqueada: true, mensaje }),
  desbloquear: () => set({ bloqueada: false, mensaje: null }),
}))
