import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

/**
 * Gráficos del panel estadístico, construidos sobre Recharts y alineados a la
 * identidad visual del aplicativo (teal `brand`, azul `acento`, ámbar y coral).
 * Todos consumen la misma forma de datos para mantenerse simples de reutilizar.
 */
export interface PuntoGrafico {
  etiqueta: string
  valor: number
}

/** Paleta de series: teal, azul, ámbar, coral, teal oscuro, azul oscuro... */
export const PALETA_GRAFICOS = [
  '#189D8B',
  '#3BA3ED',
  '#F5A524',
  '#FF6B6B',
  '#0F685D',
  '#2A8AD1',
  '#B26B08',
  '#9CA3AF',
]

const numeroCompacto = new Intl.NumberFormat('es-EC', {
  notation: 'compact',
  maximumFractionDigits: 1,
})

/** Formatea un conteo de forma compacta para los ejes (1.2k, 3,4 M). */
export function formatearCompacto(valor: number) {
  return numeroCompacto.format(valor)
}

/** Igual que `formatearCompacto` pero con símbolo de moneda. */
export function formatearMonedaCompacta(valor: number) {
  return `$${numeroCompacto.format(valor)}`
}

/* ------------------------------------------------------------------ tooltip */

interface EntradaTooltip {
  name?: string
  value?: number | string
  color?: string
  payload?: { fill?: string }
}

interface PropsTooltip {
  active?: boolean
  payload?: EntradaTooltip[]
  label?: string
  formatear?: (valor: number) => string
}

function TooltipPanel({ active, payload, label, formatear }: PropsTooltip) {
  if (!active || !payload || payload.length === 0) return null

  const formato = formatear ?? ((valor: number) => String(valor))

  return (
    <div className="rounded-lg border border-slate-200 bg-white/95 px-3 py-2 shadow-md backdrop-blur">
      <p className="mb-1 text-xs font-semibold text-slate-700">{label}</p>
      {payload.map((entrada, indice) => (
        <p key={indice} className="flex items-center gap-2 text-xs text-slate-600">
          <span
            className="h-2 w-2 shrink-0 rounded-full"
            style={{ background: entrada.color ?? entrada.payload?.fill ?? '#189D8B' }}
          />
          {entrada.name}:{' '}
          <span className="font-semibold text-slate-800">
            {formato(Number(entrada.value ?? 0))}
          </span>
        </p>
      ))}
    </div>
  )
}

/* --------------------------------------------------------------- sin datos */

function SinDatosGrafico({ mensaje }: { mensaje: string }) {
  return (
    <div className="flex min-h-[180px] items-center justify-center px-4 text-center text-sm text-slate-400">
      {mensaje}
    </div>
  )
}

/* --------------------------------------------------------- área (tendencia) */

export function GraficoArea({
  datos,
  nombre,
  color = '#189D8B',
  altura = 280,
  formatear,
  vacio = 'Sin datos para graficar',
}: {
  datos: PuntoGrafico[]
  nombre: string
  color?: string
  altura?: number
  formatear?: (valor: number) => string
  vacio?: string
}) {
  const gradiente = `grad-${nombre.replace(/\s+/g, '-').toLowerCase()}`

  if (datos.length === 0) return <SinDatosGrafico mensaje={vacio} />

  return (
    <ResponsiveContainer width="100%" height={altura}>
      <AreaChart data={datos} margin={{ top: 10, right: 12, left: -14, bottom: 0 }}>
        <defs>
          <linearGradient id={gradiente} x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor={color} stopOpacity={0.35} />
            <stop offset="95%" stopColor={color} stopOpacity={0.02} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E1EBEB" />
        <XAxis
          dataKey="etiqueta"
          tickLine={false}
          axisLine={false}
          tick={{ fontSize: 11, fill: '#6B7280' }}
          interval="preserveStartEnd"
        />
        <YAxis
          tickLine={false}
          axisLine={false}
          width={54}
          tick={{ fontSize: 11, fill: '#6B7280' }}
          tickFormatter={formatear}
        />
        <Tooltip
          content={<TooltipPanel formatear={formatear} />}
          cursor={{ stroke: '#A3D9CF', strokeWidth: 1 }}
        />
        <Area
          type="monotone"
          dataKey="valor"
          name={nombre}
          stroke={color}
          strokeWidth={2.5}
          fill={`url(#${gradiente})`}
        />
      </AreaChart>
    </ResponsiveContainer>
  )
}

/* ------------------------------------------------------------ barras (eje X) */

export function GraficoBarras({
  datos,
  nombre,
  color = '#189D8B',
  altura = 280,
  formatear,
  vacio = 'Sin datos para graficar',
}: {
  datos: PuntoGrafico[]
  nombre: string
  color?: string
  altura?: number
  formatear?: (valor: number) => string
  vacio?: string
}) {
  if (datos.length === 0) return <SinDatosGrafico mensaje={vacio} />

  return (
    <ResponsiveContainer width="100%" height={altura}>
      <BarChart data={datos} margin={{ top: 10, right: 12, left: -14, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E1EBEB" />
        <XAxis
          dataKey="etiqueta"
          tickLine={false}
          axisLine={false}
          tick={{ fontSize: 11, fill: '#6B7280' }}
          interval="preserveStartEnd"
        />
        <YAxis
          tickLine={false}
          axisLine={false}
          width={48}
          allowDecimals={false}
          tick={{ fontSize: 11, fill: '#6B7280' }}
          tickFormatter={formatear}
        />
        <Tooltip
          content={<TooltipPanel formatear={formatear} />}
          cursor={{ fill: '#E6F4F1' }}
        />
        <Bar dataKey="valor" name={nombre} fill={color} radius={[6, 6, 0, 0]} maxBarSize={46} />
      </BarChart>
    </ResponsiveContainer>
  )
}

/* ------------------------------------------------ barras horizontales (ranking) */

export function GraficoBarrasHorizontales({
  datos,
  nombre,
  color = '#3BA3ED',
  formatear,
  vacio = 'Sin datos para graficar',
}: {
  datos: PuntoGrafico[]
  nombre: string
  color?: string
  formatear?: (valor: number) => string
  vacio?: string
}) {
  if (datos.length === 0) return <SinDatosGrafico mensaje={vacio} />

  const altura = Math.max(180, datos.length * 40 + 24)

  return (
    <ResponsiveContainer width="100%" height={altura}>
      <BarChart
        data={datos}
        layout="vertical"
        margin={{ top: 4, right: 20, left: 8, bottom: 4 }}
      >
        <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#E1EBEB" />
        <XAxis
          type="number"
          tickLine={false}
          axisLine={false}
          allowDecimals={false}
          tick={{ fontSize: 11, fill: '#6B7280' }}
          tickFormatter={formatear}
        />
        <YAxis
          type="category"
          dataKey="etiqueta"
          width={150}
          tickLine={false}
          axisLine={false}
          tick={{ fontSize: 11, fill: '#4B5563' }}
          tickFormatter={(valor: string) =>
            valor.length > 20 ? `${valor.slice(0, 19)}…` : valor
          }
        />
        <Tooltip
          content={<TooltipPanel formatear={formatear} />}
          cursor={{ fill: '#EAF5FD' }}
        />
        <Bar dataKey="valor" name={nombre} fill={color} radius={[0, 6, 6, 0]} maxBarSize={24} />
      </BarChart>
    </ResponsiveContainer>
  )
}

/* ----------------------------------------------------------------- dona */

export function GraficoDonut({
  datos,
  formatear,
  vacio = 'Sin datos para graficar',
}: {
  datos: PuntoGrafico[]
  formatear?: (valor: number) => string
  vacio?: string
}) {
  const total = datos.reduce((suma, punto) => suma + punto.valor, 0)

  if (datos.length === 0 || total === 0) {
    return <SinDatosGrafico mensaje={vacio} />
  }

  return (
    <div className="flex flex-col items-center gap-4">
      <div className="mx-auto h-[190px] w-full max-w-[190px]">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={datos}
              dataKey="valor"
              nameKey="etiqueta"
              innerRadius={48}
              outerRadius={78}
              paddingAngle={2}
              stroke="none"
            >
              {datos.map((punto, indice) => (
                <Cell key={punto.etiqueta} fill={PALETA_GRAFICOS[indice % PALETA_GRAFICOS.length]} />
              ))}
            </Pie>
            <Tooltip content={<TooltipPanel formatear={formatear} />} />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <ul className="w-full min-w-0 space-y-1.5">
        {datos.map((punto, indice) => (
          <li key={punto.etiqueta} className="flex items-center gap-2 text-sm">
            <span
              className="h-2.5 w-2.5 shrink-0 rounded-full"
              style={{ background: PALETA_GRAFICOS[indice % PALETA_GRAFICOS.length] }}
            />
            <span className="min-w-0 flex-1 truncate text-slate-600">{punto.etiqueta}</span>
            <span className="font-semibold text-slate-800">{punto.valor}</span>
            <span className="w-10 shrink-0 text-right text-xs text-slate-400">
              {Math.round((punto.valor / total) * 100)}%
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}
