import { useMemo, useState } from 'react'
import { Check } from 'lucide-react'
import { permisosApi, rolesApi } from '@/shared/api/recursos'
import {
  useAsignarPermiso,
  useRevocarPermiso,
  useRolPermisos,
} from './api/rolPermisos'
import {
  CabeceraPagina,
  Cargando,
  MensajeError,
  Select,
  Campo,
  Tarjeta,
} from '@/shared/components/ui'
import { cn } from '@/shared/lib/utils'

/**
 * Matriz de roles y permisos.
 *
 * Agrupa los permisos por módulo y permite marcar o desmarcar
 * cada acción para el rol seleccionado.
 *
 * Los permisos asignados se utilizan para definir las acciones
 * disponibles para cada rol dentro del sistema.
 */
export function PaginaAccesos() {
  const roles = rolesApi.useLista()
  const permisos = permisosApi.useLista()
  const asignaciones = useRolPermisos()

  const asignar = useAsignarPermiso()
  const revocar = useRevocarPermiso()

  const [rolSel, setRolSel] = useState<number | ''>('')

  const permisosPorModulo = useMemo(() => {
    const mapa = new Map<string, typeof permisos.data>()

    for (const p of permisos.data ?? []) {
      const lista = mapa.get(p.modulo) ?? []

      mapa.set(p.modulo, [
        ...lista,
        p,
      ])
    }

    return [...mapa.entries()].sort(
        ([a], [b]) => a.localeCompare(b),
    )
  }, [permisos.data])

  const asignadosDelRol = useMemo(() => {
    if (rolSel === '') {
      return new Set<number>()
    }

    return new Set(
        (asignaciones.data ?? [])
            .filter(
                (a) => a.idRol === rolSel,
            )
            .map(
                (a) => a.idPermiso,
            ),
    )
  }, [
    asignaciones.data,
    rolSel,
  ])

  if (
      roles.isLoading ||
      permisos.isLoading ||
      asignaciones.isLoading
  ) {
    return <Cargando />
  }

  const error =
      roles.error ??
      permisos.error ??
      asignaciones.error ??
      asignar.error ??
      revocar.error

  function alternar(idPermiso: number) {
    if (rolSel === '') {
      return
    }

    const payload = {
      idRol: rolSel,
      idPermiso,
    }

    if (asignadosDelRol.has(idPermiso)) {
      revocar.mutate(payload)
    } else {
      asignar.mutate(payload)
    }
  }

  return (
      <>
        <CabeceraPagina
            titulo="Roles y permisos"
            descripcion="Define qué acciones puede realizar cada rol"
        />

        {error && (
            <div className="mb-4">
              <MensajeError error={error} />
            </div>
        )}

        <div className="mb-6 max-w-sm">
          <Campo etiqueta="Rol">
            <Select
                value={rolSel}
                onChange={(e) =>
                    setRolSel(
                        e.target.value
                            ? Number(e.target.value)
                            : '',
                    )
                }
            >
              <option value="">
              Seleccionar rol
              </option>

              {(roles.data ?? [])
                  .filter((r) => r.nombre !== 'Administrador Global' && r.nombre !== 'SuperUsuario')
                  .map((r) => (

                  <option
                      key={r.idRol}
                      value={r.idRol}
                  >
                    {r.nombre}
                  </option>
              ))}
            </Select>
          </Campo>
        </div>

        {rolSel === '' ? (
            <p className="rounded-xl border border-slate-200 bg-white px-5 py-10 text-center text-sm text-slate-500">
              Selecciona un rol para administrar sus permisos.
            </p>
        ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
              {permisosPorModulo.map(
                  ([modulo, lista]) => (
                      <Tarjeta
                          key={modulo}
                          titulo={modulo}
                      >
                        <div className="space-y-1">
                          {(lista ?? []).map((p) => {
                            const activo =
                                asignadosDelRol.has(
                                    p.idPermiso,
                                )

                            return (
                                <button
                                    key={p.idPermiso}
                                    type="button"
                                    onClick={() =>
                                        alternar(
                                            p.idPermiso,
                                        )
                                    }
                                    disabled={
                                        asignar.isPending ||
                                        revocar.isPending
                                    }
                                    className={cn(
                                        'flex w-full items-center justify-between rounded-lg px-3 py-2 text-sm transition-colors',
                                        activo
                                            ? 'bg-brand-50 font-medium text-brand-800'
                                            : 'text-slate-600 hover:bg-slate-50',
                                        (
                                            asignar.isPending ||
                                            revocar.isPending
                                        ) &&
                                        'cursor-not-allowed opacity-60',
                                    )}
                                >
                        <span>
                          {p.accion}
                        </span>

                                  <span
                                      className={cn(
                                          'flex h-4 w-4 items-center justify-center rounded border',
                                          activo
                                              ? 'border-brand-600 bg-brand-600 text-white'
                                              : 'border-slate-300 bg-white',
                                      )}
                                  >
                          {activo && (
                              <Check className="h-3 w-3" />
                          )}
                        </span>
                                </button>
                            )
                          })}
                        </div>
                      </Tarjeta>
                  ),
              )}

              {permisosPorModulo.length === 0 && (
                  <p className="text-sm text-slate-500">
                    No hay permisos registrados.
                  </p>
              )}
            </div>
        )}
      </>
  )
}