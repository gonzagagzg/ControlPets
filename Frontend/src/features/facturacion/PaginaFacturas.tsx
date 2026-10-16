import { useMemo, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { ColumnDef } from '@tanstack/react-table'
import { Info, Plus, Receipt, Trash2 } from 'lucide-react'
import { api } from '@/shared/api/client'
import {
  facturaDetallesApi,
  facturasApi,
  productosApi,
  sriIvaApi,
  usuariosApi,
} from '@/shared/api/recursos'
import { useClientesTodos } from '@/features/clientes/api'
import { TablaDatos } from '@/shared/components/TablaDatos'
import {
  Badge,
  Boton,
  CabeceraPagina,
  Campo,
  Cargando,
  Input,
  MensajeError,
  Modal,
  Select,
} from '@/shared/components/ui'
import { formatearFechaHora, formatearMoneda, indexarPor } from '@/shared/lib/utils'
import { limpiarDigitos } from '@/shared/lib/validacion'
import { useEsSuperUsuario } from '@/shared/session/sesion'
import type { Factura, FacturaEmitirRequest, Producto } from '@/shared/types/api'

interface LineaCarrito {
  producto: Producto
  cantidad: number
}

/**
 * Facturación.
 *
 * La emisión usa el endpoint transaccional del backend
 * (POST /api/facturas/emitir -> FacturacionTransaccionalService), que crea
 * la cabecera y todas las líneas de detalle en una sola transacción JDBC:
 * ya no hay riesgo de una factura con detalle incompleto por un fallo a
 * mitad del carrito. idEmpresa/idUsuario los resuelve el backend a partir
 * del token, no se envían desde el cliente.
 */
export function PaginaFacturas() {
  const [modalAbierto, setModalAbierto] = useState(false)
  const [facturaVista, setFacturaVista] = useState<Factura | null>(null)
  const esSuper = useEsSuperUsuario()

  const facturas = facturasApi.useLista()
  const clientes = useClientesTodos()
  const usuarios = usuariosApi.useLista()

  const porCliente = useMemo(() => indexarPor(clientes.data, 'idCliente'), [clientes.data])
  const porUsuario = useMemo(() => indexarPor(usuarios.data, 'idUsuario'), [usuarios.data])

  // El backend ya filtra por empresa activa a partir del token (ver
  // FacturaServlet), así que no hace falta re-filtrar aquí.
  const datos = useMemo(
    () => [...(facturas.data ?? [])].sort((a, b) => b.fecha.localeCompare(a.fecha)),
    [facturas.data],
  )

  const columnas = useMemo<ColumnDef<Factura, unknown>[]>(
    () => [
      { id: 'fecha', header: 'Fecha', accessorFn: (f) => formatearFechaHora(f.fecha) },
      {
        id: 'cliente',
        header: 'Cliente',
        accessorFn: (f) => porCliente.get(f.idCliente)?.nombres ?? '—',
      },
      {
        id: 'emisor',
        header: 'Emitida por',
        accessorFn: (f) => porUsuario.get(f.idUsuario)?.nombres ?? '—',
      },
      {
        id: 'total',
        header: 'Total',
        accessorFn: (f) => f.total,
        cell: ({ row }) => (
          <span className="font-medium">{formatearMoneda(row.original.total)}</span>
        ),
      },
      {
        accessorKey: 'estado',
        header: 'Estado',
        cell: ({ getValue }) => {
          const estado = getValue() as string
          return <Badge tono={estado === 'Anulada' ? 'peligro' : 'exito'}>{estado}</Badge>
        },
      },
      {
        id: 'ver',
        header: '',
        enableSorting: false,
        cell: ({ row }) => (
          <div className="text-right">
            <button
              onClick={() => setFacturaVista(row.original)}
              className="rounded-lg px-2.5 py-1 text-sm font-medium text-brand-700 hover:bg-brand-50"
            >
              Ver detalle
            </button>
          </div>
        ),
      },
    ],
    [porCliente, porUsuario],
  )

  if (facturas.isLoading || clientes.isLoading || usuarios.isLoading) return <Cargando />

  return (
    <>
      <CabeceraPagina
        titulo="Facturación"
        descripcion="Comprobantes emitidos"
        acciones={
          esSuper ? undefined : (
            <Boton onClick={() => setModalAbierto(true)}>
              <Plus className="h-4 w-4" />
              Nueva factura
            </Boton>
          )
        }
      />

      {esSuper && (
        <div className="mb-4 flex gap-2 rounded-lg border border-acento-200 bg-acento-50 p-3 text-xs text-acento-700">
          <Info className="mt-0.5 h-3.5 w-3.5 shrink-0" />
          <p>
            Como SuperUsuario puedes consultar las facturas de todas las veterinarias, pero
            emitir una factura requiere iniciar sesión con un usuario que pertenezca a una
            veterinaria específica.
          </p>
        </div>
      )}

      {facturas.isError && (
        <div className="mb-4">
          <MensajeError error={facturas.error} />
        </div>
      )}

      <TablaDatos datos={datos} columnas={columnas} buscarPlaceholder="Buscar factura…" />

      {modalAbierto && <ModalEmision onCerrar={() => setModalAbierto(false)} />}
      {facturaVista && (
        <ModalDetalle factura={facturaVista} onCerrar={() => setFacturaVista(null)} />
      )}
    </>
  )
}

/* ------------------------------------------------------------------ emisión */

function useEmitirFactura() {
  const qc = useQueryClient()
  return useMutation<Factura, Error, FacturaEmitirRequest>({
    mutationFn: (body) => api.post<Factura>('/api/facturas/emitir', body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['facturas'] })
      qc.invalidateQueries({ queryKey: ['factura-detalles'] })
      qc.invalidateQueries({ queryKey: ['productos'] })
      qc.invalidateQueries({ queryKey: ['movimientos-inventario'] })
    },
  })
}

function ModalEmision({ onCerrar }: { onCerrar: () => void }) {
  const clientes = useClientesTodos()
  const productos = productosApi.useLista()
  const ivas = sriIvaApi.useLista()

  const emitirFactura = useEmitirFactura()

  const [idCliente, setIdCliente] = useState('')
  const [carrito, setCarrito] = useState<LineaCarrito[]>([])
  const [productoSel, setProductoSel] = useState('')
  const [cantidad, setCantidad] = useState('1')

  const porIva = useMemo(() => indexarPor(ivas.data, 'idIva'), [ivas.data])
  // El backend ya filtra por empresa activa a partir del token (ver
  // ClienteServlet / ProductoServlet), así que no hace falta re-filtrar aquí.
  const clientesEmpresa = clientes.data ?? []
  const productosEmpresa = productos.data ?? []

  /** El subtotal por línea excluye IVA; el impuesto se calcula por tarifa. */
  const totales = useMemo(() => {
    let subtotal = 0
    let impuesto = 0

    for (const linea of carrito) {
      const base = linea.producto.precioUnitario * linea.cantidad
      const porcentaje = Number(porIva.get(String(linea.producto.idIva))?.porcentaje ?? 0)
      subtotal += base
      impuesto += base * (porcentaje / 100)
    }

    return {
      subtotal,
      impuesto,
      total: subtotal + impuesto,
    }
  }, [carrito, porIva])

  function agregar() {
    const producto = productosEmpresa.find((p) => p.idProducto === productoSel)
    if (!producto) return
    const cant = Number.parseInt(cantidad || '1', 10)
    if (cant < 1) return

    setCarrito((actual) => {
      const existente = actual.find((l) => l.producto.idProducto === producto.idProducto)
      if (existente) {
        return actual.map((l) =>
          l.producto.idProducto === producto.idProducto
            ? { ...l, cantidad: l.cantidad + cant }
            : l,
        )
      }
      return [...actual, { producto, cantidad: cant }]
    })

    setProductoSel('')
    setCantidad('1')
  }

  function emitir() {
    if (!idCliente || carrito.length === 0) return

    emitirFactura.mutate(
      {
        idCliente,
        detalles: carrito.map((linea) => ({
          idProducto: linea.producto.idProducto,
          idIva: linea.producto.idIva,
          cantidad: linea.cantidad,
          precioUnitario: linea.producto.precioUnitario,
          subtotal: Number((linea.producto.precioUnitario * linea.cantidad).toFixed(2)),
        })),
      },
      { onSuccess: onCerrar },
    )
  }

  return (
    <Modal abierto titulo="Emitir factura" onCerrar={onCerrar} ancho="max-w-3xl">
      <div className="space-y-5">
        {emitirFactura.isError && <MensajeError error={emitirFactura.error} />}

        <Campo etiqueta="Cliente" requerido>
          <Select value={idCliente} onChange={(e) => setIdCliente(e.target.value)}>
            <option value="">Seleccionar</option>
            {clientesEmpresa.map((c) => (
              <option key={c.idCliente} value={c.idCliente}>
                {c.nombres} — {c.identificacion}
              </option>
            ))}
          </Select>
        </Campo>

        <div className="grid grid-cols-1 items-end gap-2 sm:grid-cols-[1fr_100px_auto]">
          <Campo etiqueta="Producto">
            <Select value={productoSel} onChange={(e) => setProductoSel(e.target.value)}>
              <option value="">Seleccionar</option>
              {productosEmpresa.map((p) => (
                <option key={p.idProducto} value={p.idProducto}>
                  {p.nombre} · {formatearMoneda(p.precioUnitario)} (stock {p.stockActual})
                </option>
              ))}
            </Select>
          </Campo>
          <Campo etiqueta="Cantidad">
            <Input
              type="number"
              min={1}
              value={cantidad}
              onChange={(e) => setCantidad(limpiarDigitos(e.target.value))}
            />
          </Campo>
          <Boton variante="secundario" disabled={!productoSel} onClick={agregar}>
            <Plus className="h-4 w-4" />
            Añadir
          </Boton>
        </div>

        <div className="overflow-hidden rounded-xl border border-slate-200">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-2">Producto</th>
                <th className="px-4 py-2">Cant.</th>
                <th className="px-4 py-2">P. unit.</th>
                <th className="px-4 py-2">IVA</th>
                <th className="px-4 py-2 text-right">Subtotal</th>
                <th className="w-10" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {carrito.map((linea) => (
                <tr key={linea.producto.idProducto}>
                  <td className="px-4 py-2">{linea.producto.nombre}</td>
                  <td className="px-4 py-2">{linea.cantidad}</td>
                  <td className="px-4 py-2">{formatearMoneda(linea.producto.precioUnitario)}</td>
                  <td className="px-4 py-2">
                    {Number(porIva.get(String(linea.producto.idIva))?.porcentaje ?? 0).toFixed(0)} %
                  </td>
                  <td className="px-4 py-2 text-right">
                    {formatearMoneda(linea.producto.precioUnitario * linea.cantidad)}
                  </td>
                  <td className="px-2">
                    <button
                      onClick={() =>
                        setCarrito((a) =>
                          a.filter((l) => l.producto.idProducto !== linea.producto.idProducto),
                        )
                      }
                      className="rounded-lg p-1.5 text-slate-400 hover:bg-red-50 hover:text-red-600"
                      aria-label="Quitar línea"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </td>
                </tr>
              ))}
              {carrito.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-6 text-center text-slate-500">
                    Añade productos a la factura
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="ml-auto w-full max-w-xs space-y-1.5 text-sm">
          <div className="flex justify-between text-slate-600">
            <span>Subtotal</span>
            <span>{formatearMoneda(totales.subtotal)}</span>
          </div>
          <div className="flex justify-between text-slate-600">
            <span>IVA</span>
            <span>{formatearMoneda(totales.impuesto)}</span>
          </div>
          <div className="flex justify-between border-t border-slate-200 pt-1.5 text-base font-semibold text-slate-900">
            <span>Total</span>
            <span>{formatearMoneda(totales.total)}</span>
          </div>
        </div>

        <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
          <Boton variante="secundario" onClick={onCerrar}>
            Cancelar
          </Boton>
          <Boton
            cargando={emitirFactura.isPending}
            disabled={!idCliente || carrito.length === 0}
            onClick={emitir}
          >
            <Receipt className="h-4 w-4" />
            Emitir factura
          </Boton>
        </div>
      </div>
    </Modal>
  )
}

/* ------------------------------------------------------------------ detalle */

function ModalDetalle({ factura, onCerrar }: { factura: Factura; onCerrar: () => void }) {
  const detalles = facturaDetallesApi.useLista()
  const productos = productosApi.useLista()
  const clientes = useClientesTodos()
  const actualizar = facturasApi.useActualizar()

  const porProducto = useMemo(() => indexarPor(productos.data, 'idProducto'), [productos.data])
  const cliente = (clientes.data ?? []).find((c) => c.idCliente === factura.idCliente)
  const lineas = (detalles.data ?? []).filter((d) => d.idFactura === factura.idFactura)

  // Las líneas y el nombre del producto vienen de otras consultas: sin esto la
  // columna "Producto" aparecería vacía hasta que terminaran de cargar.
  if (detalles.isLoading || productos.isLoading || clientes.isLoading) {
    return (
      <Modal abierto titulo="Detalle de factura" onCerrar={onCerrar} ancho="max-w-2xl">
        <Cargando />
      </Modal>
    )
  }

  return (
    <Modal
      abierto
      titulo="Detalle de factura"
      descripcion={`${cliente?.nombres ?? '—'} · ${formatearFechaHora(factura.fecha)}`}
      onCerrar={onCerrar}
      ancho="max-w-2xl"
    >
      <div className="space-y-4">
        <div className="overflow-hidden rounded-xl border border-slate-200">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-2">Producto</th>
                <th className="px-4 py-2">Cant.</th>
                <th className="px-4 py-2">P. unit.</th>
                <th className="px-4 py-2 text-right">Subtotal</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {lineas.map((d) => (
                <tr key={d.idDetalle}>
                  <td className="px-4 py-2">{porProducto.get(d.idProducto)?.nombre ?? '—'}</td>
                  <td className="px-4 py-2">{d.cantidad}</td>
                  <td className="px-4 py-2">{formatearMoneda(d.precioUnitario)}</td>
                  <td className="px-4 py-2 text-right">{formatearMoneda(d.subtotal)}</td>
                </tr>
              ))}
              {lineas.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-4 py-6 text-center text-slate-500">
                    Esta factura no tiene líneas registradas
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="flex items-center justify-between">
          <Badge tono={factura.estado === 'Anulada' ? 'peligro' : 'exito'}>{factura.estado}</Badge>
          <span className="text-lg font-semibold text-slate-900">
            {formatearMoneda(factura.total)}
          </span>
        </div>

        {actualizar.error && <MensajeError error={actualizar.error} />}

        <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
          <Boton variante="secundario" onClick={onCerrar}>
            Cerrar
          </Boton>
          {factura.estado !== 'Anulada' && (
            <Boton
              variante="peligro"
              cargando={actualizar.isPending}
              onClick={() =>
                actualizar.mutate(
                  { id: factura.idFactura, datos: { ...factura, estado: 'Anulada' } },
                  { onSuccess: onCerrar },
                )
              }
            >
              Anular factura
            </Boton>
          )}
        </div>
      </div>
    </Modal>
  )
}
