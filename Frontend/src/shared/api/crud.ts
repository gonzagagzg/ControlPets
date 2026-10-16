import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseQueryOptions,
} from '@tanstack/react-query'
import { api } from './client'

/**
 * Fábrica de hooks CRUD.
 *
 * Los 22 servlets del backend son estructuralmente idénticos:
 *
 *   GET    /api/<recurso>        -> listado
 *   GET    /api/<recurso>/{id}   -> uno
 *   POST   /api/<recurso>        -> crea (201)
 *   PUT    /api/<recurso>/{id}   -> actualiza (200)
 *   DELETE /api/<recurso>/{id}   -> elimina (204)
 *
 * En vez de escribir esa capa 22 veces, se genera una vez y cada módulo
 * la instancia con su tipo y su nombre de llave primaria.
 */

export type Id = string | number

export interface CrudHooks<T> {
  claves: {
    todos: readonly unknown[]
    lista: () => readonly unknown[]
    detalle: (id: Id) => readonly unknown[]
  }
  useLista: (opciones?: Partial<UseQueryOptions<T[]>>) => ReturnType<typeof useQuery<T[]>>
  useDetalle: (id: Id | undefined) => ReturnType<typeof useQuery<T>>
  useCrear: () => ReturnType<typeof useMutation<T, Error, Partial<T>>>
  useActualizar: () => ReturnType<typeof useMutation<T, Error, { id: Id; datos: Partial<T> }>>
  useEliminar: () => ReturnType<typeof useMutation<void, Error, Id>>
}

export function crearHooksCrud<T>(recurso: string, campoId: keyof T): CrudHooks<T> {
  const claves = {
    todos: [recurso] as const,
    lista: () => [recurso, 'lista'] as const,
    detalle: (id: Id) => [recurso, 'detalle', String(id)] as const,
  }

  const ruta = `/api/${recurso}`

  /** Toda mutación invalida el recurso completo: listado y detalles. */
  function useInvalidar() {
    const qc = useQueryClient()
    return () => qc.invalidateQueries({ queryKey: claves.todos })
  }

  return {
    claves,

    useLista: (opciones) =>
      useQuery<T[]>({
        queryKey: claves.lista(),
        queryFn: () => api.get<T[]>(ruta),
        ...opciones,
      }),

    useDetalle: (id) =>
      useQuery<T>({
        queryKey: claves.detalle(id ?? ''),
        queryFn: () => api.get<T>(`${ruta}/${id}`),
        enabled: id !== undefined && id !== null && id !== '',
      }),

    useCrear: () => {
      const invalidar = useInvalidar()
      return useMutation<T, Error, Partial<T>>({
        mutationFn: (datos) => api.post<T>(ruta, datos),
        onSuccess: invalidar,
      })
    },

    useActualizar: () => {
      const invalidar = useInvalidar()
      return useMutation<T, Error, { id: Id; datos: Partial<T> }>({
        mutationFn: ({ id, datos }) => api.put<T>(`${ruta}/${id}`, { ...datos, [campoId]: id }),
        onSuccess: invalidar,
      })
    },

    useEliminar: () => {
      const invalidar = useInvalidar()
      return useMutation<void, Error, Id>({
        mutationFn: (id) => api.delete(`${ruta}/${id}`),
        onSuccess: invalidar,
      })
    },
  }
}

/** Extrae el valor de la llave primaria de un registro. */
export function obtenerId<T>(registro: T, campoId: keyof T): Id {
  return registro[campoId] as unknown as Id
}
