import { useQuery } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type { Cliente, PaginaResponse } from '@/shared/types/api'

/**
 * GET /api/clientes?pagina=&tamanio= — paginación real de servidor
 * (ClienteServlet/ClienteService.listarPaginado), a diferencia del resto
 * de módulos que aún se paginan en cliente vía TablaDatos.
 *
 * La clave de query empieza con 'clientes' a propósito: así queda dentro
 * del `invalidateQueries({queryKey:['clientes']})` que ya disparan las
 * mutaciones crear/actualizar/eliminar de `clientesApi` (crearHooksCrud).
 */
export function useClientesPaginado(pagina: number, tamanio: number) {
  return useQuery<PaginaResponse<Cliente>>({
    queryKey: ['clientes', 'paginado', pagina, tamanio],
    queryFn: () => api.get<PaginaResponse<Cliente>>('/api/clientes', { pagina, tamanio }),
    placeholderData: (anterior) => anterior,
  })
}

/**
 * Para pantallas que solo necesitan "todos los clientes" para un lookup o
 * un <select> (Dashboard, Facturación) — NO para el listado principal de
 * Clientes, que usa `useClientesPaginado` con controles de página reales.
 *
 * Limitación conocida: el backend limita `tamanio` a 100
 * (ClienteService.validarPaginacion), así que con más de 100 clientes esta
 * vista no los mostrará todos. Mientras no exista un endpoint "listar todos"
 * dedicado, es el máximo que se puede pedir en una sola llamada.
 */
export function useClientesTodos() {
  const pagina = useClientesPaginado(0, 100)
  return { ...pagina, data: pagina.data?.contenido }
}
