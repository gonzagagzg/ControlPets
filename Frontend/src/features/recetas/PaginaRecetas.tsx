import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import type { ColumnDef } from '@tanstack/react-table'
import {
  citasApi,
  historialesApi,
  mascotasApi,
  recetaDetallesApi,
  recetasApi,
} from '@/shared/api/recursos'
import { TablaDatos } from '@/shared/components/TablaDatos'
import { Badge, CabeceraPagina, Cargando, MensajeError } from '@/shared/components/ui'
import { formatearFechaHora, indexarPor } from '@/shared/lib/utils'
import type { Receta } from '@/shared/types/api'

/**
 * Listado de recetas en solo lectura: la edición ocurre dentro de la consulta
 * clínica, que es donde tiene sentido clínico crearlas.
 */
export function PaginaRecetas() {
  const navigate = useNavigate()

  const recetas = recetasApi.useLista()
  const detalles = recetaDetallesApi.useLista()
  const historiales = historialesApi.useLista()
  const citas = citasApi.useLista()
  const mascotas = mascotasApi.useLista()

  const porHistorial = useMemo(() => indexarPor(historiales.data, 'idHistorial'), [historiales.data])
  const porCita = useMemo(() => indexarPor(citas.data, 'idCita'), [citas.data])
  const porMascota = useMemo(() => indexarPor(mascotas.data, 'idMascota'), [mascotas.data])

  const conteoLineas = useMemo(() => {
    const mapa = new Map<string, number>()
    for (const d of detalles.data ?? []) mapa.set(d.idReceta, (mapa.get(d.idReceta) ?? 0) + 1)
    return mapa
  }, [detalles.data])

  // El backend ya filtra por empresa activa a partir del token (ver
  // RecetaServlet), así que no hace falta re-filtrar aquí.
  const datos = recetas.data ?? []

  const citaDeReceta = (r: Receta) => {
    const historial = porHistorial.get(r.idHistorial)
    return historial ? porCita.get(historial.idCita) : undefined
  }

  const columnas = useMemo<ColumnDef<Receta, unknown>[]>(
    () => [
      {
        id: 'fecha',
        header: 'Fecha',
        accessorFn: (r) => formatearFechaHora(citaDeReceta(r)?.fechaHora),
      },
      {
        id: 'mascota',
        header: 'Mascota',
        accessorFn: (r) => {
          const cita = citaDeReceta(r)
          return cita ? (porMascota.get(cita.idMascota)?.nombre ?? '—') : '—'
        },
      },
      {
        id: 'medicamentos',
        header: 'Medicamentos',
        accessorFn: (r) => conteoLineas.get(r.idReceta) ?? 0,
        cell: ({ row }) => <Badge tono="info">{conteoLineas.get(row.original.idReceta) ?? 0}</Badge>,
      },
      {
        accessorKey: 'indicacionesGenerales',
        header: 'Indicaciones',
        cell: ({ getValue }) => (
          <span className="line-clamp-1 max-w-sm">{(getValue() as string) || '—'}</span>
        ),
      },
      {
        id: 'abrir',
        header: '',
        enableSorting: false,
        cell: ({ row }) => {
          const cita = citaDeReceta(row.original)
          return (
            <div className="text-right">
              <button
                disabled={!cita}
                onClick={() => cita && navigate(`/app/historiales?cita=${cita.idCita}`)}
                className="rounded-lg px-2.5 py-1 text-sm font-medium text-brand-700 hover:bg-brand-50 disabled:opacity-40"
              >
                Ver consulta
              </button>
            </div>
          )
        },
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [porHistorial, porCita, porMascota, conteoLineas, navigate],
  )

  if (
    recetas.isLoading ||
    detalles.isLoading ||
    historiales.isLoading ||
    citas.isLoading ||
    mascotas.isLoading
  ) {
    return <Cargando />
  }

  return (
    <>
      <CabeceraPagina
        titulo="Recetas"
        descripcion="Prescripciones emitidas desde las consultas clínicas"
      />
      {recetas.isError && (
        <div className="mb-4">
          <MensajeError error={recetas.error} />
        </div>
      )}
      <TablaDatos datos={datos} columnas={columnas} buscarPlaceholder="Buscar receta…" />
    </>
  )
}
