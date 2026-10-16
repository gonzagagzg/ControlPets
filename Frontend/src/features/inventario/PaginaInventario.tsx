import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { Badge, Cargando, Tarjeta } from '@/shared/components/ui'
import { formatearFechaHora, indexarPor } from '@/shared/lib/utils'
import { movimientosApi, productosApi } from '@/shared/api/recursos'
import { filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import { TIPOS_MOVIMIENTO, type MovimientoInventario } from '@/shared/types/api'

const TONO_TIPO: Record<string, 'exito' | 'peligro' | 'info' | 'neutro'> = {
  Ingreso: 'exito',
  Egreso: 'peligro',
  Venta: 'info',
  Ajuste: 'neutro',
}

export function PaginaInventario() {
  const idEmpresa = useSesion((s) => s.idEmpresa)

  const productos = productosApi.useLista()
  const porProducto = useMemo(() => indexarPor(productos.data, 'idProducto'), [productos.data])

  const productosEmpresa = useMemo(
    () => filtrarPorEmpresa(productos.data, idEmpresa),
    [productos.data, idEmpresa],
  )

  const bajoMinimo = productosEmpresa.filter((p) => p.stockActual <= p.stockMinimo)

  const columnas = useMemo<ColumnDef<MovimientoInventario, unknown>[]>(
    () => [
      { id: 'fecha', header: 'Fecha', accessorFn: (m) => formatearFechaHora(m.fecha) },
      {
        id: 'producto',
        header: 'Producto',
        accessorFn: (m) => porProducto.get(m.idProducto)?.nombre ?? '—',
      },
      {
        accessorKey: 'tipo',
        header: 'Tipo',
        cell: ({ getValue }) => {
          const tipo = getValue() as string
          return <Badge tono={TONO_TIPO[tipo] ?? 'neutro'}>{tipo}</Badge>
        },
      },
      { accessorKey: 'cantidad', header: 'Cantidad' },
    ],
    [porProducto],
  )

  // Los productos alimentan tanto el aviso de stock como la columna "Producto":
  // si no están, la tabla saldría con celdas vacías.
  if (productos.isLoading) return <Cargando />

  return (
    <>
      {bajoMinimo.length > 0 && (
        <div className="mb-6">
          <Tarjeta
            titulo="Productos bajo el stock mínimo"
            acciones={<Badge tono="alerta">{bajoMinimo.length}</Badge>}
          >
            <ul className="grid grid-cols-1 gap-2 sm:grid-cols-2 lg:grid-cols-3">
              {bajoMinimo.slice(0, 9).map((p) => (
                <li
                  key={p.idProducto}
                  className="flex items-center justify-between rounded-lg bg-amber-50 px-3 py-2 text-sm"
                >
                  <span className="truncate text-amber-900">{p.nombre}</span>
                  <span className="ml-2 shrink-0 font-medium text-amber-800">
                    {p.stockActual}/{p.stockMinimo}
                  </span>
                </li>
              ))}
            </ul>
          </Tarjeta>
        </div>
      )}

      <PaginaCrud<MovimientoInventario>
        titulo="Movimientos de inventario"
        singular="movimiento"
        descripcion="Kardex de ingresos, egresos, ventas y ajustes"
        api={movimientosApi}
        campoId="idMovimiento"
        multiEmpresa
        columnas={columnas}
        campos={[
          {
            nombre: 'idProducto',
            etiqueta: 'Producto',
            tipo: 'select',
            requerido: true,
            soloCreacion: true,
            opciones: productosEmpresa.map((p) => ({
              valor: p.idProducto,
              etiqueta: `${p.nombre} (stock: ${p.stockActual})`,
            })),
          },
          {
            nombre: 'tipo',
            etiqueta: 'Tipo de movimiento',
            tipo: 'select',
            requerido: true,
            opciones: TIPOS_MOVIMIENTO.map((t) => ({ valor: t, etiqueta: t })),
          },
          { nombre: 'cantidad', etiqueta: 'Cantidad', tipo: 'numero', requerido: true, min: 1 },
          {
            nombre: 'fecha',
            etiqueta: 'Fecha y hora',
            tipo: 'fechaHora',
            requerido: true,
          },
        ]}
      />
    </>
  )
}
