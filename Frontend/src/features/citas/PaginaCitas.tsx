import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { addDays, format, isSameDay, parseISO, startOfDay, startOfWeek } from 'date-fns'
import { es } from 'date-fns/locale'
import { CalendarDays, ChevronLeft, ChevronRight, Plus, Stethoscope } from 'lucide-react'
import {
  citasApi,
  historialesApi,
  mascotasApi,
  usuariosApi,
  veterinariosApi,
} from '@/shared/api/recursos'
import { useClientesTodos } from '@/features/clientes/api'
import {
  Badge,
  Boton,
  CabeceraPagina,

  Cargando,
  MensajeError,
  Modal,
  Select,
  SinDatos,
} from '@/shared/components/ui'
import { FormularioCrud } from '@/shared/components/crud/FormularioCrud'
import { cn, indexarPor } from '@/shared/lib/utils'
import { filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import { ESTADOS_CITA, type Cita, type EstadoCita } from '@/shared/types/api'

const TONO_ESTADO: Record<string, 'alerta' | 'exito' | 'peligro' | 'neutro'> = {
  Pendiente: 'alerta',
  Atendida: 'exito',
  Cancelada: 'peligro',
}

/**
 * Agenda semanal.
 *
 * Es la pantalla que más se aparta del CRUD genérico porque el valor está en
 * ver la carga del día, no en una tabla: agrupa por día, permite cambiar el
 * estado en un clic y enlaza a la consulta clínica de cada cita.
 */
export function PaginaCitas() {
  const navigate = useNavigate()
  const idEmpresa = useSesion((s) => s.idEmpresa)

  const [inicioSemana, setInicioSemana] = useState(() =>
    startOfWeek(new Date(), { weekStartsOn: 1 }),
  )
  const [modalAbierto, setModalAbierto] = useState(false)
  const [citaEditada, setCitaEditada] = useState<Cita | null>(null)
  const [filtroVeterinario, setFiltroVeterinario] = useState('')

  const citas = citasApi.useLista()
  const mascotas = mascotasApi.useLista()
  const clientes = useClientesTodos()
  const veterinarios = veterinariosApi.useLista()
  const usuarios = usuariosApi.useLista()
  const historiales = historialesApi.useLista()

  const crear = citasApi.useCrear()
  const actualizar = citasApi.useActualizar()

  const porMascota = useMemo(() => indexarPor(mascotas.data, 'idMascota'), [mascotas.data])
  const porCliente = useMemo(() => indexarPor(clientes.data, 'idCliente'), [clientes.data])
  const porUsuario = useMemo(() => indexarPor(usuarios.data, 'idUsuario'), [usuarios.data])
  const historialPorCita = useMemo(
    () => indexarPor(historiales.data, 'idCita'),
    [historiales.data],
  )

  const veterinariosEmpresa = useMemo(
    () => filtrarPorEmpresa(veterinarios.data, idEmpresa),
    [veterinarios.data, idEmpresa],
  )

  const nombreVeterinario = (idVeterinario: string) => {
    const vet = veterinariosEmpresa.find((v) => v.idVeterinario === idVeterinario)
    if (!vet) return '—'
    return porUsuario.get(vet.idUsuario)?.nombres ?? vet.especialidad ?? '—'
  }

  const dias = useMemo(
    () => Array.from({ length: 7 }, (_, i) => addDays(inicioSemana, i)),
    [inicioSemana],
  )

  const citasSemana = useMemo(() => {
    const propias = filtrarPorEmpresa(citas.data, idEmpresa)
    const filtradas = filtroVeterinario
      ? propias.filter((c) => c.idVeterinario === filtroVeterinario)
      : propias

    return dias.map((dia) => ({
      dia,
      citas: filtradas
        .filter((c) => {
          try {
            return isSameDay(parseISO(c.fechaHora), dia)
          } catch {
            return false
          }
        })
        .sort((a, b) => a.fechaHora.localeCompare(b.fechaHora)),
    }))
  }, [citas.data, idEmpresa, dias, filtroVeterinario])

  function cambiarEstado(cita: Cita, estado: EstadoCita) {
    actualizar.mutate({ id: cita.idCita, datos: { ...cita, estado } })
  }

  if (
    citas.isLoading ||
    mascotas.isLoading ||
    clientes.isLoading ||
    veterinarios.isLoading ||
    usuarios.isLoading ||
    historiales.isLoading
  ) {
    return <Cargando />
  }

  const mascotasEmpresa = filtrarPorEmpresa(mascotas.data, idEmpresa)

  return (
    <>
      <CabeceraPagina
        titulo="Agenda de citas"
        descripcion="Programación semanal por veterinario"
        acciones={
          <Boton
            onClick={() => {
              setCitaEditada(null)
              crear.reset()
              setModalAbierto(true)
            }}
          >
            <Plus className="h-4 w-4" />
            Nueva cita
          </Boton>
        }
      />

      {citas.isError && (
        <div className="mb-4">
          <MensajeError error={citas.error} />
        </div>
      )}

      <div className="mb-4 flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3">
        <div className="flex items-center gap-1">
          <button
            onClick={() => setInicioSemana(addDays(inicioSemana, -7))}
            className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100"
            aria-label="Semana anterior"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
          <button
            onClick={() => setInicioSemana(startOfWeek(new Date(), { weekStartsOn: 1 }))}
            className="rounded-lg px-2.5 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-100"
          >
            Hoy
          </button>
          <button
            onClick={() => setInicioSemana(addDays(inicioSemana, 7))}
            className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100"
            aria-label="Semana siguiente"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>

        <span className="flex items-center gap-2 text-sm font-medium text-slate-700">
          <CalendarDays className="h-4 w-4 text-slate-400" />
          {format(inicioSemana, "d 'de' MMMM", { locale: es })} –{' '}
          {format(addDays(inicioSemana, 6), "d 'de' MMMM yyyy", { locale: es })}
        </span>

        <div className="ml-auto w-56">
          <Select
            value={filtroVeterinario}
            onChange={(e) => setFiltroVeterinario(e.target.value)}
            aria-label="Filtrar por veterinario"
          >
            <option value="">Todos los veterinarios</option>
            {veterinariosEmpresa.map((v) => (
              <option key={v.idVeterinario} value={v.idVeterinario}>
                {porUsuario.get(v.idUsuario)?.nombres ?? v.idVeterinario}
              </option>
            ))}
          </Select>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
        {citasSemana.map(({ dia, citas: delDia }) => {
          const esHoy = isSameDay(dia, startOfDay(new Date()))
          return (
            <div
              key={dia.toISOString()}
              className={cn(
                'rounded-xl border bg-white',
                esHoy ? 'border-brand-400 ring-1 ring-brand-200' : 'border-slate-200',
              )}
            >
              <div
                className={cn(
                  'flex items-baseline justify-between rounded-t-xl border-b px-3.5 py-2',
                  esHoy ? 'border-brand-200 bg-brand-50' : 'border-slate-200 bg-slate-50',
                )}
              >
                <span
                  className={cn(
                    'text-sm font-semibold capitalize',
                    esHoy ? 'text-brand-800' : 'text-slate-700',
                  )}
                >
                  {format(dia, 'EEEE d', { locale: es })}
                </span>
                <span className="text-xs text-slate-500">{delDia.length}</span>
              </div>

              <div className="space-y-2 p-2.5">
                {delDia.length === 0 && (
                  <p className="py-4 text-center text-xs text-slate-400">Sin citas</p>
                )}

                {delDia.map((cita) => {
                  const mascota = porMascota.get(cita.idMascota)
                  const cliente = mascota ? porCliente.get(mascota.idCliente) : undefined
                  const tieneHistorial = historialPorCita.has(cita.idCita)

                  return (
                    <article
                      key={cita.idCita}
                      className="rounded-lg border border-slate-200 p-2.5 transition-colors hover:border-brand-300"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-sm font-semibold text-slate-900">
                          {format(parseISO(cita.fechaHora), 'HH:mm')}
                        </span>
                        <Badge tono={TONO_ESTADO[cita.estado] ?? 'neutro'}>{cita.estado}</Badge>
                      </div>

                      <p className="mt-1 truncate text-sm text-slate-800">
                        {mascota?.nombre ?? 'Mascota desconocida'}
                      </p>
                      <p className="truncate text-xs text-slate-500">{cliente?.nombres ?? '—'}</p>
                      <p className="truncate text-xs text-slate-400">
                        {nombreVeterinario(cita.idVeterinario)}
                      </p>

                      <div className="mt-2 flex flex-wrap gap-1.5">
                        <button
                          onClick={() => navigate(`/app/historiales?cita=${cita.idCita}`)}
                          className="inline-flex items-center gap-1 rounded-md bg-brand-50 px-2 py-1 text-xs font-medium text-brand-700 hover:bg-brand-100"
                        >
                          <Stethoscope className="h-3 w-3" />
                          {tieneHistorial ? 'Ver consulta' : 'Atender'}
                        </button>

                        {cita.estado === 'Pendiente' && (
                          <>
                            <button
                              onClick={() => cambiarEstado(cita, 'Atendida')}
                              className="rounded-md px-2 py-1 text-xs text-emerald-700 hover:bg-emerald-50"
                            >
                              Marcar atendida
                            </button>
                            <button
                              onClick={() => cambiarEstado(cita, 'Cancelada')}
                              className="rounded-md px-2 py-1 text-xs text-red-600 hover:bg-red-50"
                            >
                              Cancelar
                            </button>
                          </>
                        )}

                        <button
                          onClick={() => {
                            setCitaEditada(cita)
                            actualizar.reset()
                            setModalAbierto(true)
                          }}
                          className="rounded-md px-2 py-1 text-xs text-slate-600 hover:bg-slate-100"
                        >
                          Editar
                        </button>
                      </div>
                    </article>
                  )
                })}
              </div>
            </div>
          )
        })}
      </div>

      {citasSemana.every((d) => d.citas.length === 0) && (
        <div className="mt-4 rounded-xl border border-slate-200 bg-white">
          <SinDatos mensaje="No hay citas programadas en esta semana" />
        </div>
      )}

      <Modal
        abierto={modalAbierto}
        titulo={citaEditada ? 'Editar cita' : 'Nueva cita'}
        onCerrar={() => setModalAbierto(false)}
        ancho="max-w-2xl"
      >
        <FormularioCrud<Cita>
          edicion={Boolean(citaEditada)}
          valoresIniciales={citaEditada ?? { estado: 'Pendiente' }}
          enviando={crear.isPending || actualizar.isPending}
          error={crear.error ?? actualizar.error}
          onCancelar={() => setModalAbierto(false)}
          onEnviar={(datos) => {
            if (citaEditada) {
              actualizar.mutate(
                { id: citaEditada.idCita, datos: { ...citaEditada, ...datos } },
                { onSuccess: () => setModalAbierto(false) },
              )
            } else {
              crear.mutate(
                { ...datos, idEmpresa: idEmpresa ?? undefined } as Partial<Cita>,
                { onSuccess: () => setModalAbierto(false) },
              )
            }
          }}
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
              nombre: 'idVeterinario',
              etiqueta: 'Veterinario',
              tipo: 'select',
              requerido: true,
              opciones: veterinariosEmpresa.map((v) => ({
                valor: v.idVeterinario,
                etiqueta: porUsuario.get(v.idUsuario)?.nombres ?? v.idVeterinario,
              })),
            },
            {
              nombre: 'fechaHora',
              etiqueta: 'Fecha y hora',
              tipo: 'fechaHora',
              requerido: true,
              minFecha: 'hoy',
            },
            {
              nombre: 'estado',
              etiqueta: 'Estado',
              tipo: 'select',
              requerido: true,
              opciones: ESTADOS_CITA.map((e) => ({ valor: e, etiqueta: e })),
            },
          ]}
        />
      </Modal>
    </>
  )
}
