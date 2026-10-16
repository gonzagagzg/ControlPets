import { useQuery } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type { EstadisticasPlataforma } from '@/shared/types/api'

/**
 * GET /api/estadisticas — panel estadístico global de la plataforma.
 * Reservado al SuperUsuario (EstadisticasServlet.exigirSuperUsuario); para
 * cualquier otro rol el backend responde 403, así que la consulta solo se
 * habilita cuando el consumidor lo indica.
 */
export function useEstadisticasPlataforma(habilitado = true) {
  return useQuery<EstadisticasPlataforma>({
    queryKey: ['estadisticas', 'plataforma'],
    queryFn: () => api.get<EstadisticasPlataforma>('/api/estadisticas'),
    enabled: habilitado,
  })
}
