import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import {
  differenceInCalendarDays,
  endOfMonth,
  isSameDay,
  isWithinInterval,
  parseISO,
  startOfMonth,
} from 'date-fns'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { AlertTriangle, CalendarClock, CalendarX, PawPrint, TrendingUp, Users } from 'lucide-react'
import {
  citasApi,
  facturasApi,
  mascotasApi,
  productosApi,
  usuariosApi,
  veterinariosApi,
} from '@/shared/api/recursos'
import { useClientesTodos } from '@/features/clientes/api'
import { Badge, CabeceraPagina, Cargando, Tarjeta } from '@/shared/components/ui'
import { cn, formatearFecha, formatearMoneda, indexarPor } from '@/shared/lib/utils'
import { filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import { usePermisos } from '@/shared/session/permisos'

/** Días de anticipación con los que se avisa de un producto por caducar. */
const DIAS_AVISO_CADUCIDAD = 30

export function PaginaPanel() {
  const { idEmpresa, nombreUsuario } = useSesion()
  const permisos = usePermisos()

  const citas = citasApi.useLista()
  const clientes = useClientesTodos()
  const mascotas = mascotasApi.useLista()
  const productos = productosApi.useLista()
  const facturas = facturasApi.useLista()
  // Solo se cargan si el rol tiene acceso: roles como Farmacéutico no tienen
  // permiso VETERINARIOS/USUARIOS y el backend respondería 403 en cada visita.
  const veterinarios = veterinariosApi.useLista({
    enabled: permisos.puede('VETERINARIOS', 'LISTAR'),
  })
  const usuarios = usuariosApi.useLista({
    enabled: permisos.puede('USUARIOS', 'LISTAR'),
  })

  const porMascota = useMemo(() => indexarPor(mascotas.data, 'idMascota'), [mascotas.data])
  const porUsuario = useMemo(() => indexarPor(usuarios.data, 'idUsuario'), [usuarios.data])

  const metricas = useMemo(() => {
    const hoy = new Date()
    const citasEmpresa = filtrarPorEmpresa(citas.data, idEmpresa)
    const facturasEmpresa = filtrarPorEmpresa(facturas.data, idEmpresa)
    const productosEmpresa = filtrarPorEmpresa(productos.data, idEmpresa)

    const citasHoy = citasEmpresa
      .filter((c) => {
        try {
          return isSameDay(parseISO(c.fechaHora), hoy)
        } catch {
          return false
        }
      })
      .sort((a, b) => a.fechaHora.localeCompare(b.fechaHora))

    const intervaloMes = { start: startOfMonth(hoy), end: endOfMonth(hoy) }
    const ventasMes = facturasEmpresa
      .filter((f) => f.estado !== 'Anulada')
      .filter((f) => {
        try {
          return isWithinInterval(parseISO(f.fecha), intervaloMes)
        } catch {
          return false
        }
      })
      .reduce((suma, f) => suma + Number(f.total), 0)

    // Productos por caducar (incluye los ya vencidos, que son más urgentes).
    // `dias` es negativo si ya venció, 0 si vence hoy. Se ordenan del más
    // urgente al menos.
    const porCaducar = productosEmpresa
      .filter((p) => p.fechaCaducidad)
      .map((p) => ({
        producto: p,
        dias: differenceInCalendarDays(parseISO(p.fechaCaducidad as string), hoy),
      }))
      .filter(({ dias }) => dias <= DIAS_AVISO_CADUCIDAD)
      .sort((a, b) => a.dias - b.dias)

    return {
      citasHoy,
      pendientesHoy: citasHoy.filter((c) => c.estado === 'Pendiente').length,
      clientes: filtrarPorEmpresa(clientes.data, idEmpresa).length,
      mascotas: filtrarPorEmpresa(mascotas.data, idEmpresa).length,
      ventasMes,
      alertasStock: productosEmpresa.filter((p) => p.stockActual <= p.stockMinimo),
      porCaducar,
    }
  }, [citas.data, clientes.data, mascotas.data, productos.data, facturas.data, idEmpresa])

  const nombreVeterinario = (idVeterinario: string) => {
    const vet = (veterinarios.data ?? []).find((v) => v.idVeterinario === idVeterinario)
    return vet ? (porUsuario.get(vet.idUsuario)?.nombres ?? '—') : '—'
  }

  if (
    citas.isLoading ||
    clientes.isLoading ||
    mascotas.isLoading ||
    productos.isLoading ||
    facturas.isLoading ||
    usuarios.isLoading
  ) {
    return <Cargando />
  }

  return (
    <>
      <CabeceraPagina
        titulo={`Hola, ${nombreUsuario ?? ''}`}
        descripcion={format(new Date(), "EEEE d 'de' MMMM 'de' yyyy", { locale: es })}
      />

      <div className="grid-escalonado mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Metrica
          Icono={CalendarClock}
          etiqueta="Citas hoy"
          valor={String(metricas.citasHoy.length)}
          detalle={`${metricas.pendientesHoy} pendientes`}
          a="/app/citas"
          acento="border-l-brand-500"
          iconoBg="bg-brand-50"
          iconoColor="text-brand-700"
        />
        <Metrica
          Icono={Users}
          etiqueta="Clientes"
          valor={String(metricas.clientes)}
          detalle="registrados"
          a="/app/clientes"
          acento="border-l-acento-500"
          iconoBg="bg-acento-50"
          iconoColor="text-acento-700"
        />
        <Metrica
          Icono={PawPrint}
          etiqueta="Pacientes"
          valor={String(metricas.mascotas)}
          detalle="en seguimiento"
          a="/app/mascotas"
          acento="border-l-brand-800"
          iconoBg="bg-brand-100"
          iconoColor="text-brand-800"
        />
        <Metrica
          Icono={TrendingUp}
          etiqueta="Facturación del mes"
          valor={formatearMoneda(metricas.ventasMes)}
          detalle="facturas no anuladas"
          a="/app/facturas"
          acento="border-l-acento-800"
          iconoBg="bg-acento-100"
          iconoColor="text-acento-800"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Tarjeta
            titulo="Agenda de hoy"
            acciones={
              <Link to="/app/citas" className="text-sm font-medium text-acento-600 hover:text-acento-700 hover:underline">
                Ver semana
              </Link>
            }
          >
            {metricas.citasHoy.length === 0 ? (
              <p className="py-6 text-center text-sm text-slate-500">
                No hay citas programadas para hoy.
              </p>
            ) : (
              <ul className="divide-y divide-slate-100">
                {metricas.citasHoy.map((cita) => (
                  <li key={cita.idCita} className="flex items-center gap-4 py-2.5">
                    <span className="w-12 shrink-0 text-sm font-semibold text-slate-900">
                      {format(parseISO(cita.fechaHora), 'HH:mm')}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm text-slate-800">
                        {porMascota.get(cita.idMascota)?.nombre ?? 'Mascota'}
                      </p>
                      <p className="truncate text-xs text-slate-500">
                        {nombreVeterinario(cita.idVeterinario)}
                      </p>
                    </div>
                    <Badge
                      tono={
                        cita.estado === 'Atendida'
                          ? 'exito'
                          : cita.estado === 'Cancelada'
                            ? 'peligro'
                            : 'alerta'
                      }
                    >
                      {cita.estado}
                    </Badge>
                  </li>
                ))}
              </ul>
            )}
          </Tarjeta>
        </div>

        <Tarjeta
          titulo="Productos cerca de agotarse"
          acciones={
            metricas.alertasStock.length > 0 ? (
              <Badge tono="alerta">{metricas.alertasStock.length}</Badge>
            ) : undefined
          }
        >
          {metricas.alertasStock.length === 0 ? (
            <p className="py-6 text-center text-sm text-slate-500">
              Todo el inventario está sobre el mínimo.
            </p>
          ) : (
            <ul className="space-y-2">
              {metricas.alertasStock.slice(0, 6).map((p) => (
                <li key={p.idProducto} className="flex items-start gap-2.5">
                  <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-amber-500" />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm text-slate-800">{p.nombre}</p>
                    <p className="text-xs text-slate-500">
                      {p.stockActual} en stock · mínimo {p.stockMinimo}
                    </p>
                  </div>
                </li>
              ))}
              {metricas.alertasStock.length > 6 && (
                <li>
                  <Link to="/app/inventario" className="text-sm text-acento-600 hover:text-acento-700 hover:underline">
                    Ver los {metricas.alertasStock.length} productos
                  </Link>
                </li>
              )}
            </ul>
          )}
        </Tarjeta>
      </div>

      <div className="mt-4">
        <Tarjeta
          titulo="Productos que están cerca de caducar"
          acciones={
            metricas.porCaducar.length > 0 ? (
              <Badge tono={metricas.porCaducar.some((p) => p.dias < 0) ? 'peligro' : 'alerta'}>
                {metricas.porCaducar.length}
              </Badge>
            ) : undefined
          }
        >
          {metricas.porCaducar.length === 0 ? (
            <p className="py-6 text-center text-sm text-slate-500">
              Ningún producto caduca en los próximos {DIAS_AVISO_CADUCIDAD} días.
            </p>
          ) : (
            <>
              <ul className="grid grid-cols-1 gap-2 sm:grid-cols-2 xl:grid-cols-3">
                {metricas.porCaducar.slice(0, 6).map(({ producto, dias }) => (
                  <li
                    key={producto.idProducto}
                    className={cn(
                      'flex items-center gap-3 rounded-lg px-3 py-2',
                      dias < 0 ? 'bg-red-50' : 'bg-amber-50',
                    )}
                  >
                    <CalendarX
                      className={cn(
                        'h-4 w-4 shrink-0',
                        dias < 0 ? 'text-red-500' : 'text-amber-500',
                      )}
                    />
                    <div className="min-w-0 flex-1">
                      <p
                        className={cn(
                          'truncate text-sm',
                          dias < 0 ? 'text-red-900' : 'text-amber-900',
                        )}
                      >
                        {producto.nombre}
                      </p>
                      <p className={cn('text-xs', dias < 0 ? 'text-red-700' : 'text-amber-700')}>
                        {formatearFecha(producto.fechaCaducidad)}
                      </p>
                    </div>
                    <Badge tono={dias < 0 ? 'peligro' : 'alerta'}>
                      {dias < 0
                        ? `Vencido hace ${Math.abs(dias)} d`
                        : dias === 0
                          ? 'Caduca hoy'
                          : `En ${dias} d`}
                    </Badge>
                  </li>
                ))}
              </ul>
              {metricas.porCaducar.length > 6 && (
                <div className="mt-3">
                  <Link
                    to="/app/productos"
                    className="text-sm text-acento-600 hover:text-acento-700 hover:underline"
                  >
                    Ver los {metricas.porCaducar.length} productos
                  </Link>
                </div>
              )}
            </>
          )}
        </Tarjeta>
      </div>
    </>
  )
}

function Metrica({
  Icono,
  etiqueta,
  valor,
  detalle,
  a,
  acento,
  iconoBg,
  iconoColor,
}: {
  Icono: React.ComponentType<{ className?: string }>
  etiqueta: string
  valor: string
  detalle: string
  a: string
  acento: string
  iconoBg: string
  iconoColor: string
}) {
  return (
    <Link
      to={a}
      className={cn(
        'group relative overflow-hidden rounded-xl border border-slate-200 border-l-4 bg-white p-5 shadow-sm transition hover:shadow-md',
        acento,
      )}
    >
      <div className={cn('mb-4 flex h-10 w-10 items-center justify-center rounded-lg', iconoBg)}>
        <Icono className={cn('h-5 w-5', iconoColor)} />
      </div>
      <p className="text-2xl font-bold tracking-tight text-slate-900">{valor}</p>
      <p className="mt-0.5 text-sm font-medium text-slate-700">{etiqueta}</p>
      <p className="mt-0.5 text-xs text-slate-400">{detalle}</p>
    </Link>
  )
}
