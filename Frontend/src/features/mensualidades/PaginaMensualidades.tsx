import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { Badge, CabeceraPagina, Cargando, MensajeError } from '@/shared/components/ui'
import { TablaDatos } from '@/shared/components/TablaDatos'
import { mensualidadesApi } from '@/shared/api/recursos'
import { formatearFecha, formatearMoneda } from '@/shared/lib/utils'
import type { MensualidadEmpresa } from '@/shared/types/api'

const TONO_ESTADO: Record<string, 'exito' | 'alerta' | 'peligro'> = {
  PAGADA: 'exito',
  PENDIENTE: 'alerta',
  VENCIDA: 'peligro',
}

/**
 * Pagos/mensualidades de la aplicación para el Administrador Local.
 * Solo lectura: GET /api/mensualidades ya resuelve la empresa desde el JWT,
 * así que no hay selector de veterinaria ni acciones de alta/edición
 * (reservadas a SuperUsuario, ver MENSUALIDADES/CREAR-EDITAR-ELIMINAR).
 */
export function PaginaMensualidades() {
  const mensualidades = mensualidadesApi.useMisMensualidades()

  const columnas = useMemo<ColumnDef<MensualidadEmpresa, unknown>[]>(
    () => [
      { accessorKey: 'periodo', header: 'Periodo' },
      {
        accessorKey: 'valor',
        header: 'Valor',
        cell: ({ getValue }) => formatearMoneda(getValue() as number),
      },
      {
        accessorKey: 'fechaVencimiento',
        header: 'Vencimiento',
        cell: ({ getValue }) => formatearFecha(getValue() as string),
      },
      {
        accessorKey: 'fechaPago',
        header: 'Fecha de pago',
        cell: ({ getValue }) => formatearFecha(getValue() as string | null),
      },
      {
        accessorKey: 'estado',
        header: 'Estado',
        cell: ({ getValue }) => {
          const estado = String(getValue())
          return <Badge tono={TONO_ESTADO[estado] ?? 'neutro'}>{estado}</Badge>
        },
      },
      {
        accessorKey: 'observacion',
        header: 'Observación',
        cell: ({ getValue }) => (getValue() as string | null) || '—',
      },
    ],
    [],
  )

  return (
    <div>
      <CabeceraPagina
        titulo="Mensualidades"
        descripcion="Pagos de la aplicación correspondientes a tu veterinaria."
      />

      {mensualidades.isLoading ? (
        <Cargando />
      ) : mensualidades.error ? (
        <MensajeError error={mensualidades.error} />
      ) : (
        <TablaDatos
          datos={mensualidades.data ?? []}
          columnas={columnas}
          buscarPlaceholder="Buscar por periodo…"
        />
      )}
    </div>
  )
}
