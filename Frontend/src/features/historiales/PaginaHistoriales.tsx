import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import type { ColumnDef } from '@tanstack/react-table'
import { ClipboardList, Plus, Trash2 } from 'lucide-react'
import {
  citasApi,
  historialesApi,
  mascotasApi,
  productosApi,
  recetaDetallesApi,
  recetasApi,
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
  Tarjeta,
  TextArea,
} from '@/shared/components/ui'
import { formatearFechaHora, indexarPor } from '@/shared/lib/utils'
import { limpiarDigitos } from '@/shared/lib/validacion'
import { filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import type { HistorialClinico, RecetaDetalle } from '@/shared/types/api'

/**
 * Consulta clínica.
 *
 * Encadena tres entidades que el backend expone por separado:
 *   cita -> historial_clinico (1:1) -> receta (1:1) -> receta_detalle (1:N)
 *
 * La pantalla las presenta como un solo flujo para que el veterinario no tenga
 * que navegar tres módulos distintos.
 */
export function PaginaHistoriales() {
  const [params, setParams] = useSearchParams()
  const idEmpresa = useSesion((s) => s.idEmpresa)
  const citaSeleccionada = params.get('cita')

  const historiales = historialesApi.useLista()
  const citas = citasApi.useLista()
  const mascotas = mascotasApi.useLista()
  const clientes = useClientesTodos()

  const porCita = useMemo(() => indexarPor(citas.data, 'idCita'), [citas.data])
  const porMascota = useMemo(() => indexarPor(mascotas.data, 'idMascota'), [mascotas.data])
  const porCliente = useMemo(() => indexarPor(clientes.data, 'idCliente'), [clientes.data])

  const datos = useMemo(
    () => filtrarPorEmpresa(historiales.data, idEmpresa),
    [historiales.data, idEmpresa],
  )

  const columnas = useMemo<ColumnDef<HistorialClinico, unknown>[]>(
    () => [
      {
        id: 'fecha',
        header: 'Fecha de consulta',
        accessorFn: (h) => formatearFechaHora(porCita.get(h.idCita)?.fechaHora),
      },
      {
        id: 'mascota',
        header: 'Mascota',
        accessorFn: (h) => {
          const cita = porCita.get(h.idCita)
          return cita ? (porMascota.get(cita.idMascota)?.nombre ?? '—') : '—'
        },
      },
      {
        id: 'propietario',
        header: 'Propietario',
        accessorFn: (h) => {
          const cita = porCita.get(h.idCita)
          const mascota = cita ? porMascota.get(cita.idMascota) : undefined
          return mascota ? (porCliente.get(mascota.idCliente)?.nombres ?? '—') : '—'
        },
      },
      {
        id: 'signos',
        header: 'Signos',
        accessorFn: (h) =>
          [
            h.pesoKg !== null ? `${h.pesoKg} kg` : null,
            h.temperaturaC !== null ? `${h.temperaturaC} °C` : null,
          ]
            .filter(Boolean)
            .join(' · ') || '—',
      },
      {
        accessorKey: 'diagnostico',
        header: 'Diagnóstico',
        cell: ({ getValue }) => (
          <span className="line-clamp-1 max-w-xs">{(getValue() as string) || '—'}</span>
        ),
      },
      {
        id: 'abrir',
        header: '',
        enableSorting: false,
        cell: ({ row }) => (
          <div className="text-right">
            <button
              onClick={() => setParams({ cita: row.original.idCita })}
              className="rounded-lg px-2.5 py-1 text-sm font-medium text-brand-700 hover:bg-brand-50"
            >
              Abrir
            </button>
          </div>
        ),
      },
    ],
    [porCita, porMascota, porCliente, setParams],
  )

  if (
    historiales.isLoading ||
    citas.isLoading ||
    mascotas.isLoading ||
    clientes.isLoading
  ) {
    return <Cargando />
  }

  return (
    <>
      <CabeceraPagina
        titulo="Historias clínicas"
        descripcion="Registro de consultas y recetas asociadas"
      />

      {historiales.isError && (
        <div className="mb-4">
          <MensajeError error={historiales.error} />
        </div>
      )}

      <TablaDatos datos={datos} columnas={columnas} buscarPlaceholder="Buscar consulta…" />

      {citaSeleccionada && (
        <ModalConsulta idCita={citaSeleccionada} onCerrar={() => setParams({})} />
      )}
    </>
  )
}

/* ------------------------------------------------------------------ consulta */

function ModalConsulta({ idCita, onCerrar }: { idCita: string; onCerrar: () => void }) {
  const idEmpresa = useSesion((s) => s.idEmpresa)

  const historiales = historialesApi.useLista()
  const citas = citasApi.useLista()
  const mascotas = mascotasApi.useLista()

  const crearHistorial = historialesApi.useCrear()
  const actualizarHistorial = historialesApi.useActualizar()

  const cita = (citas.data ?? []).find((c) => c.idCita === idCita)
  const mascota = cita ? (mascotas.data ?? []).find((m) => m.idMascota === cita.idMascota) : undefined
  const historial = (historiales.data ?? []).find((h) => h.idCita === idCita)

  const [form, setForm] = useState({
    pesoKg: historial?.pesoKg?.toString() ?? '',
    temperaturaC: historial?.temperaturaC?.toString() ?? '',
    anamnesis: historial?.anamnesis ?? '',
    diagnostico: historial?.diagnostico ?? '',
  })

  // Sincroniza el formulario cuando los datos cargan después del primer render
  useEffect(() => {
    if (historial) {
      setForm({
        pesoKg: historial.pesoKg?.toString() ?? '',
        temperaturaC: historial.temperaturaC?.toString() ?? '',
        anamnesis: historial.anamnesis ?? '',
        diagnostico: historial.diagnostico ?? '',
      })
    }
  // Solo re-sincroniza cuando cambia el historial (no en cada edición del form)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [historial?.idHistorial])

  function guardar() {
    if (pesoInvalido || temperaturaInvalida) return
    const payload = {
      idEmpresa: idEmpresa ?? undefined,
      idCita,
      pesoKg: form.pesoKg !== '' ? Number.parseFloat(form.pesoKg) : null,
      temperaturaC: form.temperaturaC !== '' ? Number.parseFloat(form.temperaturaC) : null,
      anamnesis: form.anamnesis || null,
      diagnostico: form.diagnostico || null,
    }

    if (historial) {
      actualizarHistorial.mutate(
        { id: historial.idHistorial, datos: { ...historial, ...payload } },
        { onSuccess: onCerrar },
      )
    } else {
      crearHistorial.mutate(payload as Partial<HistorialClinico>, { onSuccess: onCerrar })
    }
  }

  const guardando = crearHistorial.isPending || actualizarHistorial.isPending
  const error = crearHistorial.error ?? actualizarHistorial.error

  const pesoNumero = form.pesoKg === '' ? null : Number.parseFloat(form.pesoKg)
  const pesoInvalido =
    pesoNumero !== null && (Number.isNaN(pesoNumero) || pesoNumero < 0 || pesoNumero > 300)
  const temperaturaNumero =
    form.temperaturaC === '' ? null : Number.parseFloat(form.temperaturaC)
  const temperaturaInvalida =
    temperaturaNumero !== null &&
    (Number.isNaN(temperaturaNumero) || temperaturaNumero < 0 || temperaturaNumero > 60)

  return (
    <Modal
      abierto
      titulo={`Consulta · ${mascota?.nombre ?? 'Mascota'}`}
      descripcion={cita ? formatearFechaHora(cita.fechaHora) : undefined}
      onCerrar={onCerrar}
      ancho="max-w-4xl"
    >
      <div className="space-y-5">
        {error && <MensajeError error={error} />}

        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <Campo
            etiqueta="Peso (kg)"
            error={pesoInvalido ? 'Ingresa un peso entre 0 y 300 kg' : undefined}
          >
            <Input
              type="number"
              step="0.01"
              min={0}
              max={300}
              value={form.pesoKg}
              onChange={(e) => setForm({ ...form, pesoKg: e.target.value })}
            />
          </Campo>
          <Campo
            etiqueta="Temperatura (°C)"
            error={temperaturaInvalida ? 'Ingresa una temperatura entre 0 y 60 °C' : undefined}
          >
            <Input
              type="number"
              step="0.1"
              min={0}
              max={60}
              value={form.temperaturaC}
              onChange={(e) => setForm({ ...form, temperaturaC: e.target.value })}
            />
          </Campo>
        </div>

        <Campo etiqueta="Anamnesis" ayuda="Motivo de consulta y antecedentes referidos">
          <TextArea
            rows={3}
            value={form.anamnesis}
            onChange={(e) => setForm({ ...form, anamnesis: e.target.value })}
          />
        </Campo>

        <Campo etiqueta="Diagnóstico">
          <TextArea
            rows={3}
            value={form.diagnostico}
            onChange={(e) => setForm({ ...form, diagnostico: e.target.value })}
          />
        </Campo>

        <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
          <Boton variante="secundario" onClick={onCerrar}>
            Cerrar
          </Boton>
          <Boton cargando={guardando} disabled={pesoInvalido || temperaturaInvalida} onClick={guardar}>
            {historial ? 'Guardar consulta' : 'Registrar consulta'}
          </Boton>
        </div>

        {historial ? (
          <SeccionReceta idHistorial={historial.idHistorial} />
        ) : (
          <p className="rounded-lg bg-slate-50 px-4 py-3 text-sm text-slate-600">
            Registra la consulta para poder emitir una receta.
          </p>
        )}
      </div>
    </Modal>
  )
}

/* -------------------------------------------------------------------- receta */

function SeccionReceta({ idHistorial }: { idHistorial: string }) {
  const idEmpresa = useSesion((s) => s.idEmpresa)

  const recetas = recetasApi.useLista()
  const detalles = recetaDetallesApi.useLista()
  const productos = productosApi.useLista()

  const crearReceta = recetasApi.useCrear()
  const crearDetalle = recetaDetallesApi.useCrear()
  const eliminarDetalle = recetaDetallesApi.useEliminar()
  const actualizarReceta = recetasApi.useActualizar()

  const receta = (recetas.data ?? []).find((r) => r.idHistorial === idHistorial)
  const lineas = (detalles.data ?? []).filter((d) => d.idReceta === receta?.idReceta)
  const porProducto = useMemo(() => indexarPor(productos.data, 'idProducto'), [productos.data])

  const [nuevo, setNuevo] = useState({
    idProducto: '',
    dosis: '',
    frecuencia: '',
    duracionDias: '',
  })

  const productosEmpresa = filtrarPorEmpresa(productos.data, idEmpresa)

  const [indicaciones, setIndicaciones] = useState('')

  // La receta se emite vacía y sus indicaciones se completan desde la consulta.
  useEffect(() => {
    setIndicaciones(receta?.indicacionesGenerales ?? '')
  }, [receta?.idReceta, receta?.indicacionesGenerales])

  function guardarIndicaciones() {
    if (!receta) return
    const texto = indicaciones.trim()
    actualizarReceta.mutate({
      id: receta.idReceta,
      datos: { ...receta, indicacionesGenerales: texto === '' ? null : texto },
    })
  }

  function agregarLinea() {
    if (!receta || !nuevo.idProducto) return
    crearDetalle.mutate(
      {
        idReceta: receta.idReceta,
        idProducto: nuevo.idProducto,
        dosis: nuevo.dosis,
        frecuencia: nuevo.frecuencia,
        duracionDias: Number.parseInt(nuevo.duracionDias || '1', 10),
      } as Partial<RecetaDetalle>,
      {
        onSuccess: () => setNuevo({ idProducto: '', dosis: '', frecuencia: '', duracionDias: '' }),
      },
    )
  }

  // La tabla de líneas traduce idProducto -> nombre y depende de las recetas:
  // se espera a que carguen para no mostrar celdas vacías un instante.
  if (recetas.isLoading || detalles.isLoading || productos.isLoading) {
    return <Cargando />
  }

  if (!receta) {
    return (
      <Tarjeta titulo="Receta">
        <div className="flex items-center justify-between gap-4">
          <p className="text-sm text-slate-600">Esta consulta aún no tiene receta emitida.</p>
          <Boton
            variante="secundario"
            cargando={crearReceta.isPending}
            onClick={() =>
              crearReceta.mutate({
                idEmpresa: idEmpresa ?? undefined,
                idHistorial,
                indicacionesGenerales: null,
              })
            }
          >
            <ClipboardList className="h-4 w-4" />
            Emitir receta
          </Boton>
        </div>
        {crearReceta.error && (
          <div className="mt-3">
            <MensajeError error={crearReceta.error} />
          </div>
        )}
      </Tarjeta>
    )
  }

  return (
    <Tarjeta titulo="Receta" acciones={<Badge tono="info">{lineas.length} medicamentos</Badge>}>
      <div className="space-y-3">
        {lineas.map((linea) => (
          <div
            key={linea.idDetalleReceta}
            className="flex items-start justify-between gap-3 rounded-lg border border-slate-200 px-3.5 py-2.5"
          >
            <div className="min-w-0">
              <p className="text-sm font-medium text-slate-900">
                {porProducto.get(linea.idProducto)?.nombre ?? 'Producto'}
              </p>
              <p className="text-xs text-slate-500">
                {linea.dosis} · {linea.frecuencia} · {linea.duracionDias} días
              </p>
            </div>
            <button
              onClick={() => eliminarDetalle.mutate(linea.idDetalleReceta)}
              className="rounded-lg p-1.5 text-slate-400 hover:bg-red-50 hover:text-red-600"
              aria-label="Quitar medicamento"
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        ))}

        {lineas.length === 0 && (
          <p className="py-2 text-sm text-slate-500">Aún no se han añadido medicamentos.</p>
        )}

        {crearDetalle.error && <MensajeError error={crearDetalle.error} />}

        <div className="grid grid-cols-1 items-end gap-2 border-t border-slate-200 pt-3 sm:grid-cols-5">
          <div className="sm:col-span-2">
            <Campo etiqueta="Producto">
              <Select
                value={nuevo.idProducto}
                onChange={(e) => setNuevo({ ...nuevo, idProducto: e.target.value })}
              >
                <option value="">Seleccionar</option>
                {productosEmpresa.map((p) => (
                  <option key={p.idProducto} value={p.idProducto}>
                    {p.nombre}
                  </option>
                ))}
              </Select>
            </Campo>
          </div>
          <Campo etiqueta="Dosis">
            <Input
              value={nuevo.dosis}
              placeholder="1 tableta"
              onChange={(e) => setNuevo({ ...nuevo, dosis: e.target.value })}
            />
          </Campo>
          <Campo etiqueta="Frecuencia">
            <Input
              value={nuevo.frecuencia}
              placeholder="cada 8 h"
              onChange={(e) => setNuevo({ ...nuevo, frecuencia: e.target.value })}
            />
          </Campo>
          <Campo etiqueta="Días">
            <Input
              type="number"
              min={1}
              value={nuevo.duracionDias}
              onChange={(e) => setNuevo({ ...nuevo, duracionDias: limpiarDigitos(e.target.value) })}
            />
          </Campo>
        </div>

        <div className="flex justify-end">
          <Boton
            variante="secundario"
            cargando={crearDetalle.isPending}
            disabled={!nuevo.idProducto || !nuevo.dosis || !nuevo.frecuencia}
            onClick={agregarLinea}
          >
            <Plus className="h-4 w-4" />
            Añadir medicamento
          </Boton>
        </div>

        <div className="border-t border-slate-200 pt-3">
          <Campo
            etiqueta="Indicaciones generales"
            ayuda="Recomendaciones para el propietario (opcional)"
          >
            <TextArea
              rows={2}
              value={indicaciones}
              placeholder="Ej. administrar con alimento, mantener en reposo…"
              onChange={(e) => setIndicaciones(e.target.value)}
            />
          </Campo>

          {actualizarReceta.error && (
            <div className="mt-2">
              <MensajeError error={actualizarReceta.error} />
            </div>
          )}

          <div className="mt-2 flex justify-end">
            <Boton
              variante="secundario"
              cargando={actualizarReceta.isPending}
              disabled={indicaciones.trim() === (receta.indicacionesGenerales ?? '').trim()}
              onClick={guardarIndicaciones}
            >
              Guardar indicaciones
            </Boton>
          </div>
        </div>
      </div>
    </Tarjeta>
  )
}
