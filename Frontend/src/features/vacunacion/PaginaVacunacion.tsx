import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { formatearFecha, indexarPor } from '@/shared/lib/utils'
import { mascotaVacunasApi, mascotasApi, vacunasApi } from '@/shared/api/recursos'
import { useClientesTodos } from '@/features/clientes/api'
import type { MascotaVacuna } from '@/shared/types/api'

export function PaginaVacunacion() {
  const mascotas = mascotasApi.useLista()
  const clientes = useClientesTodos()
  const vacunas = vacunasApi.useLista()

  const porMascota = useMemo(() => indexarPor(mascotas.data, 'idMascota'), [mascotas.data])
  const porCliente = useMemo(() => indexarPor(clientes.data, 'idCliente'), [clientes.data])
  const porVacuna = useMemo(() => indexarPor(vacunas.data, 'idVacuna'), [vacunas.data])

  const columnas = useMemo<ColumnDef<MascotaVacuna, unknown>[]>(
    () => [
      {
        id: 'mascota',
        header: 'Mascota',
        accessorFn: (v) => porMascota.get(v.idMascota)?.nombre ?? '—',
      },
      {
        id: 'propietario',
        header: 'Propietario',
        accessorFn: (v) => {
          const mascota = porMascota.get(v.idMascota)
          return mascota ? (porCliente.get(mascota.idCliente)?.nombres ?? '—') : '—'
        },
      },
      {
        id: 'vacuna',
        header: 'Vacuna',
        accessorFn: (v) => porVacuna.get(String(v.idVacuna))?.nombre ?? '—',
      },
      {
        id: 'aplicacion',
        header: 'Fecha de aplicación',
        accessorFn: (v) => formatearFecha(v.fechaAplicacion),
      },
    ],
    [porMascota, porCliente, porVacuna],
  )

  // El backend ya filtra por empresa activa a partir del token (ver
  // MascotaServlet), así que no hace falta re-filtrar aquí.
  const mascotasEmpresa = mascotas.data ?? []

  return (
    <PaginaCrud<MascotaVacuna>
      titulo="Vacunación"
      singular="registro de vacunación"
      descripcion="Vacunas aplicadas por mascota"
      api={mascotaVacunasApi}
      campoId="idMascotaVacuna"
      multiEmpresa
      cargando={mascotas.isLoading || clientes.isLoading || vacunas.isLoading}
      columnas={columnas}
      campos={[
        {
          nombre: 'idMascota',
          etiqueta: 'Mascota',
          tipo: 'select',
          requerido: true,
          opciones: mascotasEmpresa.map((m) => ({
            valor: m.idMascota,
            etiqueta: `${m.nombre} — ${porCliente.get(m.idCliente)?.nombres ?? '?'}`,
          })),
        },
        {
          nombre: 'idVacuna',
          etiqueta: 'Vacuna',
          tipo: 'select',
          requerido: true,
          opcionesNumericas: true,
          opciones: (vacunas.data ?? []).map((v) => ({ valor: v.idVacuna, etiqueta: v.nombre })),
        },
        {
          nombre: 'fechaAplicacion',
          etiqueta: 'Fecha de aplicación',
          tipo: 'fecha',
          requerido: true,
        },
      ]}
    />
  )
}
