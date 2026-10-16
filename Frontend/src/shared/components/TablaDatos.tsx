import { useMemo, useState, type ReactNode } from 'react'
import { ArrowDown, ArrowUp, ChevronLeft, ChevronRight, Search } from 'lucide-react'
import {
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
  type ColumnDef,
} from '@tanstack/react-table'
import { cn } from '@/shared/lib/utils'
import { Input } from './ui'
import { SinDatos } from './ui'

/**
 * Tabla reutilizable con búsqueda global, orden y paginación en cliente.
 *
 * La paginación es de cliente porque los servlets devuelven el listado
 * completo sin soporte de `limit`/`offset`. Si el backend agrega paginación,
 * solo cambia este componente.
 */
export function TablaDatos<T>({
  datos,
  columnas,
  buscarPlaceholder = 'Buscar…',
  accionesCabecera,
  tamanioPagina = 10,
  claseFila,
}: {
  datos: T[]
  columnas: ColumnDef<T, unknown>[]
  buscarPlaceholder?: string
  accionesCabecera?: ReactNode
  tamanioPagina?: number
  /** Clase de fondo extra por fila (p. ej. resaltar por notificación). */
  claseFila?: (registro: T) => string | undefined
}) {
  const [filtro, setFiltro] = useState('')

  const columnasMemo = useMemo(() => columnas, [columnas])

  const tabla = useReactTable({
    data: datos,
    columns: columnasMemo,
    state: { globalFilter: filtro },
    onGlobalFilterChange: setFiltro,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    initialState: { pagination: { pageSize: tamanioPagina } },
  })

  const filas = tabla.getRowModel().rows

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-4 py-3">
        <div className="relative w-full sm:max-w-xs">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
            placeholder={buscarPlaceholder}
            className="pl-9"
          />
        </div>
        {accionesCabecera}
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left">
            {tabla.getHeaderGroups().map((grupo) => (
              <tr key={grupo.id}>
                {grupo.headers.map((header) => {
                  const ordenable = header.column.getCanSort()
                  const orden = header.column.getIsSorted()
                  return (
                    <th
                      key={header.id}
                      className="whitespace-nowrap px-4 py-2.5 text-xs font-semibold uppercase tracking-wide text-slate-500"
                    >
                      {header.isPlaceholder ? null : (
                        <button
                          type="button"
                          disabled={!ordenable}
                          onClick={header.column.getToggleSortingHandler()}
                          className="inline-flex items-center gap-1 disabled:cursor-default"
                        >
                          {flexRender(header.column.columnDef.header, header.getContext())}
                          {orden === 'asc' && <ArrowUp className="h-3 w-3 animate-aparecer" />}
                          {orden === 'desc' && <ArrowDown className="h-3 w-3 animate-aparecer" />}
                        </button>
                      )}
                    </th>
                  )
                })}
              </tr>
            ))}
          </thead>
          <tbody className="divide-y divide-slate-100">
            {filas.map((fila, indice) => (
              <tr
                key={fila.id}
                className={cn('animate-aparecer hover:bg-slate-50/70', claseFila?.(fila.original))}
                style={{ animationDelay: `${Math.min(indice, 12) * 18}ms` }}
              >
                {fila.getVisibleCells().map((celda) => (
                  <td key={celda.id} className="px-4 py-2.5 text-slate-700">
                    {flexRender(celda.column.columnDef.cell, celda.getContext())}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>

        {filas.length === 0 && (
          <SinDatos mensaje={filtro ? 'Ningún registro coincide con la búsqueda' : undefined} />
        )}
      </div>

      {tabla.getPageCount() > 1 && (
        <div className="flex flex-wrap items-center justify-between gap-2 border-t border-slate-200 px-4 py-2.5 text-sm text-slate-600">
          <span>
            Página {tabla.getState().pagination.pageIndex + 1} de {tabla.getPageCount()} ·{' '}
            {datos.length} registros
          </span>
          <div className="flex gap-1">
            <button
              onClick={() => tabla.previousPage()}
              disabled={!tabla.getCanPreviousPage()}
              className="rounded-lg p-1.5 transition-colors hover:bg-slate-100 disabled:opacity-40"
              aria-label="Página anterior"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <button
              onClick={() => tabla.nextPage()}
              disabled={!tabla.getCanNextPage()}
              className="rounded-lg p-1.5 transition-colors hover:bg-slate-100 disabled:opacity-40"
              aria-label="Página siguiente"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
