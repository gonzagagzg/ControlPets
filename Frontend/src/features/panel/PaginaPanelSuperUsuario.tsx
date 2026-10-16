import { format, parseISO } from 'date-fns'
import { es } from 'date-fns/locale'
import {
  Building2,
  CalendarDays,
  Dog,
  Package,
  RefreshCw,
  Stethoscope,
  TrendingUp,
  UserSquare,
  Users,
} from 'lucide-react'
import {
  Badge,
  Boton,
  CabeceraPagina,
  Cargando,
  MensajeError,
  SinDatos,
  Tarjeta,
} from '@/shared/components/ui'
import { cn, formatearFechaHora, formatearMoneda } from '@/shared/lib/utils'
import { useSesion } from '@/shared/session/sesion'
import type { EstadisticaConteo, EstadisticaPuntoSerie } from '@/shared/types/api'
import { useEstadisticasPlataforma } from './api'
import {
  formatearCompacto,
  formatearMonedaCompacta,
  GraficoArea,
  GraficoBarras,
  GraficoBarrasHorizontales,
  GraficoDonut,
  type PuntoGrafico,
} from './graficos'

/** 'YYYY-MM' -> 'Oct 25' (mes abreviado y año, en español). */
function etiquetaMes(periodo: string) {
  try {
    const texto = format(parseISO(`${periodo}-01`), 'MMM yy', { locale: es })
    return texto.charAt(0).toUpperCase() + texto.slice(1)
  } catch {
    return periodo
  }
}

function puntosSerie(
  serie: EstadisticaPuntoSerie[],
  campo: 'facturado' | 'citas' | 'facturas' | 'movimientos',
): PuntoGrafico[] {
  return serie.map((punto) => ({ etiqueta: etiquetaMes(punto.periodo), valor: punto[campo] }))
}

function puntosConteo(conteos: EstadisticaConteo[]): PuntoGrafico[] {
  return conteos.map((conteo) => ({ etiqueta: conteo.etiqueta, valor: conteo.valor }))
}

export function PaginaPanelSuperUsuario() {
  const { nombreUsuario } = useSesion()
  const consulta = useEstadisticasPlataforma()

  if (consulta.isLoading) {
    return <Cargando texto="Calculando estadísticas de la plataforma…" />
  }

  if (consulta.error) {
    return <MensajeError error={consulta.error} />
  }

  const panel = consulta.data
  if (!panel) return null

  const { totales } = panel
  const facturacion = puntosSerie(panel.serieMensual, 'facturado')
  const citasMes = puntosSerie(panel.serieMensual, 'citas')

  return (
    <>
      <CabeceraPagina
        titulo="Panel de la plataforma"
        descripcion={`Hola, ${nombreUsuario ?? ''} · Datos consolidados de todas las veterinarias`}
        acciones={
          <>
            <Badge tono="info">SuperUsuario</Badge>
            <Boton
              variante="secundario"
              cargando={consulta.isFetching}
              onClick={() => consulta.refetch()}
            >
              <RefreshCw className="h-4 w-4" />
              Actualizar
            </Boton>
          </>
        }
      />

      {/* ------------------------------------------- Indicadores globales */}
      <div className="grid-escalonado mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Kpi
          Icono={Building2}
          etiqueta="Veterinarias"
          valor={String(totales.empresas)}
          detalle={`${totales.empresasActivas} activas · ${totales.empresasInactivas} inactivas`}
          acento="border-l-brand-500"
          iconoBg="bg-brand-50"
          iconoColor="text-brand-700"
        />
        <Kpi
          Icono={UserSquare}
          etiqueta="Usuarios"
          valor={String(totales.usuarios)}
          detalle={`${totales.usuariosPendientes} sin confirmar`}
          acento="border-l-acento-500"
          iconoBg="bg-acento-50"
          iconoColor="text-acento-700"
        />
        <Kpi
          Icono={Users}
          etiqueta="Clientes"
          valor={String(totales.clientes)}
          detalle="registrados en la plataforma"
          acento="border-l-brand-800"
          iconoBg="bg-brand-100"
          iconoColor="text-brand-800"
        />
        <Kpi
          Icono={Dog}
          etiqueta="Mascotas"
          valor={String(totales.mascotas)}
          detalle="pacientes en seguimiento"
          acento="border-l-acento-800"
          iconoBg="bg-acento-100"
          iconoColor="text-acento-800"
        />
        <Kpi
          Icono={CalendarDays}
          etiqueta="Citas"
          valor={String(totales.citas)}
          detalle={`${totales.citasPendientes} pendientes`}
          acento="border-l-brand-500"
          iconoBg="bg-brand-50"
          iconoColor="text-brand-700"
        />
        <Kpi
          Icono={TrendingUp}
          etiqueta="Facturación total"
          valor={formatearMoneda(totales.totalFacturado)}
          detalle={`Ticket promedio ${formatearMoneda(totales.ticketPromedio)}`}
          acento="border-l-acento-500"
          iconoBg="bg-acento-50"
          iconoColor="text-acento-700"
        />
        <Kpi
          Icono={Package}
          etiqueta="Productos"
          valor={String(totales.productos)}
          detalle={`${totales.productosBajoStock} con bajo stock`}
          acento="border-l-amber-400"
          iconoBg="bg-amber-50"
          iconoColor="text-amber-700"
        />
        <Kpi
          Icono={Stethoscope}
          etiqueta="Historias clínicas"
          valor={String(totales.historiales)}
          detalle="consultas registradas"
          acento="border-l-brand-800"
          iconoBg="bg-brand-100"
          iconoColor="text-brand-800"
        />
      </div>

      {/* ------------------------------------------- Tendencia y estados */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Tarjeta
          titulo="Facturación · últimos 12 meses"
          className="lg:col-span-2"
          acciones={<Badge tono="exito">{formatearMoneda(totales.totalFacturado)}</Badge>}
        >
          <GraficoArea
            datos={facturacion}
            nombre="Facturado"
            formatear={formatearMonedaCompacta}
            vacio="Aún no hay facturación registrada"
          />
        </Tarjeta>

        <Tarjeta titulo="Veterinarias por estado">
          <GraficoDonut
            datos={puntosConteo(panel.empresasPorEstado)}
            vacio="Sin veterinarias registradas"
          />
        </Tarjeta>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Tarjeta
          titulo="Citas por mes"
          className="lg:col-span-2"
          acciones={<Badge tono="info">{totales.citas} en total</Badge>}
        >
          <GraficoBarras
            datos={citasMes}
            nombre="Citas"
            color="#3BA3ED"
            formatear={formatearCompacto}
            vacio="Aún no hay citas registradas"
          />
        </Tarjeta>

        <Tarjeta titulo="Citas por estado">
          <GraficoDonut datos={puntosConteo(panel.citasPorEstado)} vacio="Sin citas registradas" />
        </Tarjeta>
      </div>

      {/* ------------------------------------------- Desgloses */}
      <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Tarjeta titulo="Usuarios por rol">
          <GraficoBarrasHorizontales
            datos={puntosConteo(panel.usuariosPorRol)}
            nombre="Usuarios"
            formatear={formatearCompacto}
            vacio="Sin usuarios registrados"
          />
        </Tarjeta>

        <Tarjeta titulo="Mascotas por especie">
          <GraficoDonut
            datos={puntosConteo(panel.mascotasPorEspecie)}
            vacio="Sin mascotas registradas"
          />
        </Tarjeta>
      </div>

      {/* ------------------------------------------- Ranking */}
      <div className="mt-4">
        <Tarjeta
          titulo="Ranking de veterinarias · por facturación"
          acciones={<span className="text-xs text-slate-400">Top {panel.topEmpresas.length}</span>}
        >
          {panel.topEmpresas.length === 0 ? (
            <SinDatos mensaje="Aún no hay veterinarias para mostrar" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
                    <th className="px-3 py-2 font-semibold">Veterinaria</th>
                    <th className="px-3 py-2 font-semibold">Estado</th>
                    <th className="px-3 py-2 text-right font-semibold">Clientes</th>
                    <th className="px-3 py-2 text-right font-semibold">Mascotas</th>
                    <th className="px-3 py-2 text-right font-semibold">Facturas</th>
                    <th className="px-3 py-2 text-right font-semibold">Facturado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {panel.topEmpresas.map((empresa) => (
                    <tr key={empresa.idEmpresa} className="transition-colors hover:bg-brand-50/50">
                      <td className="px-3 py-2.5 font-medium text-slate-800">
                        {empresa.razonSocial}
                      </td>
                      <td className="px-3 py-2.5">
                        <Badge tono={empresa.activo ? 'exito' : 'peligro'}>
                          {empresa.activo ? 'Activa' : 'Inactiva'}
                        </Badge>
                      </td>
                      <td className="px-3 py-2.5 text-right text-slate-600">{empresa.clientes}</td>
                      <td className="px-3 py-2.5 text-right text-slate-600">{empresa.mascotas}</td>
                      <td className="px-3 py-2.5 text-right text-slate-600">{empresa.facturas}</td>
                      <td className="px-3 py-2.5 text-right font-semibold text-slate-800">
                        {formatearMoneda(empresa.facturado)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Tarjeta>
      </div>

      <p className="mt-5 text-right text-xs text-slate-400">
        Actualizado el {formatearFechaHora(panel.generadoEn)}
      </p>
    </>
  )
}

/* --------------------------------------------------------------- Tarjeta KPI */

function Kpi({
  Icono,
  etiqueta,
  valor,
  detalle,
  acento,
  iconoBg,
  iconoColor,
}: {
  Icono: React.ComponentType<{ className?: string }>
  etiqueta: string
  valor: string
  detalle: string
  acento: string
  iconoBg: string
  iconoColor: string
}) {
  return (
    <div
      className={cn(
        'group rounded-xl border border-slate-200 border-l-4 bg-white p-4 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md sm:p-5',
        acento,
      )}
    >
      <div
        className={cn(
          'mb-4 flex h-10 w-10 items-center justify-center rounded-lg transition-transform duration-200 group-hover:scale-105',
          iconoBg,
        )}
      >
        <Icono className={cn('h-5 w-5', iconoColor)} />
      </div>
      <p className="text-xl font-bold tracking-tight text-slate-900 sm:text-2xl">{valor}</p>
      <p className="mt-0.5 text-sm font-medium text-slate-700">{etiqueta}</p>
      <p className="mt-0.5 text-xs text-slate-400">{detalle}</p>
    </div>
  )
}
