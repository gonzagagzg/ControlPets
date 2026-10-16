import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowRight,
  Boxes,
  CalendarDays,
  ClipboardList,
  Menu,
  Package,
  Receipt,
  ShieldCheck,
  Stethoscope,
  Syringe,
  X,
} from 'lucide-react'
import { MarcaControlPets } from '@/shared/components/Logo'
import { Revelar } from '@/shared/components/Revelar'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'

const MODULOS = [
  {
    Icono: CalendarDays,
    nombre: 'Agenda de citas',
    descripcion: 'Calendario semanal por veterinario con estados de atención y filtros.',
    color: 'bg-brand-50 text-brand-600',
  },
  {
    Icono: Stethoscope,
    nombre: 'Historias clínicas',
    descripcion: 'Registro completo por paciente: diagnóstico, tratamiento y evolución.',
    color: 'bg-acento-50 text-acento-600',
  },
  {
    Icono: Syringe,
    nombre: 'Vacunación',
    descripcion: 'Control de esquemas de vacunación y seguimiento de próximas dosis.',
    color: 'bg-brand-100 text-brand-700',
  },
  {
    Icono: ClipboardList,
    nombre: 'Recetas médicas',
    descripcion: 'Prescripciones vinculadas directamente a la historia clínica.',
    color: 'bg-acento-100 text-acento-700',
  },
  {
    Icono: Boxes,
    nombre: 'Inventario',
    descripcion: 'Stock de productos con alertas automáticas por mínimos críticos.',
    color: 'bg-amber-50 text-amber-700',
  },
  {
    Icono: Receipt,
    nombre: 'Facturación',
    descripcion: 'Emisión de facturas, cálculo de impuestos y control de anulaciones.',
    color: 'bg-red-50 text-red-600',
  },
]

export function PaginaInicio() {
  const hoy = format(new Date(), "EEEE d 'de' MMMM yyyy", { locale: es })
  const [menuAbierto, setMenuAbierto] = useState(false)

  return (
    <div className="min-h-screen">
      {/* ── HEADER ─────────────────────────────────── */}
      <header className="fixed top-0 z-50 w-full border-b border-brand-800 bg-brand-900/95 backdrop-blur-sm">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6">
          <Link to="/" className="flex items-center" aria-label="ControlPets">
            <MarcaControlPets tamano="md" />
          </Link>

          <nav className="hidden items-center gap-8 md:flex">
            <a href="#servicios" className="text-sm text-brand-100 transition-colors hover:text-white">
              Áreas
            </a>
            <a href="#modulos" className="text-sm text-brand-100 transition-colors hover:text-white">
              Módulos
            </a>
          </nav>

          <div className="flex items-center gap-2">
            <Link
              to="/acceso"
              className="inline-flex items-center gap-2 rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white shadow-sm transition-all hover:-translate-y-0.5 hover:bg-brand-400 hover:shadow-marca"
            >
              Iniciar sesión
              <ArrowRight className="h-3.5 w-3.5" />
            </Link>
            <button
              type="button"
              onClick={() => setMenuAbierto((v) => !v)}
              aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
              aria-expanded={menuAbierto}
              className="rounded-lg p-2 text-brand-100 transition-colors hover:bg-white/10 hover:text-white md:hidden"
            >
              {menuAbierto ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
          </div>
        </div>

        {menuAbierto && (
          <nav className="animate-deslizar-arriba border-t border-white/10 bg-brand-900/98 px-4 py-3 md:hidden">
            <a
              href="#servicios"
              onClick={() => setMenuAbierto(false)}
              className="block rounded-lg px-3 py-2.5 text-sm text-brand-100 transition-colors hover:bg-white/10 hover:text-white"
            >
              Áreas
            </a>
            <a
              href="#modulos"
              onClick={() => setMenuAbierto(false)}
              className="block rounded-lg px-3 py-2.5 text-sm text-brand-100 transition-colors hover:bg-white/10 hover:text-white"
            >
              Módulos
            </a>
          </nav>
        )}
      </header>

      {/* ── HERO ───────────────────────────────────── */}
      <section className="flex min-h-screen flex-col justify-center bg-gradient-to-b from-brand-700 via-brand-800 to-brand-950 px-4 pt-24 pb-10 sm:px-6 sm:pb-14">
        <div className="relative mx-auto w-full max-w-7xl overflow-hidden rounded-3xl shadow-marca ring-1 ring-white/10">
          {/* Imagen de fondo + velo de marca: sólido a la izquierda (texto) y
              translúcido a la derecha (fotografía), con refuerzo en móvil. */}
          <img
            src="/Controlpetsinicio.jpg"
            alt=""
            aria-hidden="true"
            loading="eager"
            decoding="async"
            className="absolute inset-0 h-full w-full object-cover object-center"
          />
          <div
            aria-hidden="true"
            className="absolute inset-0 bg-gradient-to-r from-brand-950 from-10% via-brand-950/95 via-45% to-brand-950/90 sm:to-brand-900/25"
          />
          <div
            aria-hidden="true"
            className="absolute inset-x-0 bottom-0 h-2/5 bg-gradient-to-t from-brand-950/95 via-brand-950/45 to-transparent"
          />

          {/* Burbujas decorativas con movimiento suave */}
          <div aria-hidden="true" className="pointer-events-none absolute inset-0">
            <span className="absolute -right-16 top-24 h-56 w-56 animate-flotar rounded-full bg-acento-400/15 blur-3xl" />
            <span
              className="absolute right-40 bottom-32 h-28 w-28 animate-flotar rounded-full bg-brand-300/18 blur-2xl"
              style={{ animationDelay: '1.8s' }}
            />
          </div>

          <div className="relative px-6 py-14 sm:px-10 sm:py-16 lg:px-14 lg:py-20">
            <p
              className="itq-fade-up mb-6 inline-flex items-center gap-2 rounded-full bg-white/10 px-3.5 py-1.5 text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-100 ring-1 ring-white/20 backdrop-blur-sm"
              style={{ animationDelay: '0ms' }}
            >
              Sistema de gestión clínica veterinaria
            </p>

            <h1
              className="itq-fade-up mb-6 max-w-4xl text-5xl font-extrabold leading-[1.05] tracking-tight text-white drop-shadow md:text-7xl"
              style={{ animationDelay: '120ms' }}
            >
              Cada paciente.
              <br />
              Cada visita.
              <br />
              <span className="text-acento-300">Todo registrado.</span>
            </h1>

            <p
              className="itq-fade-up mb-10 max-w-lg text-lg leading-relaxed text-brand-50 drop-shadow"
              style={{ animationDelay: '240ms' }}
            >
              Control clínico completo para su equipo: agenda, historiales, vacunación, recetas, inventario y facturación — integrados.
            </p>

            <div
              className="itq-fade-up flex flex-wrap gap-4"
              style={{ animationDelay: '360ms' }}
            >
              <Link
                to="/acceso"
                className="inline-flex items-center gap-2 rounded-lg bg-brand-500 px-6 py-3 text-sm font-semibold text-white shadow-marca transition-colors hover:bg-brand-400"
              >
                Iniciar sesión
                <ArrowRight className="h-4 w-4" />
              </Link>
              <a
                href="#modulos"
                className="inline-flex items-center gap-2 rounded-lg border border-white/30 bg-white/5 px-6 py-3 text-sm font-medium text-white backdrop-blur-sm transition-colors hover:border-acento-300 hover:bg-white/15"
              >
                Ver módulos
              </a>
            </div>

            {/* Tira de datos clínicos — el elemento firma */}
            <div
              className="itq-fade-up mt-16 flex flex-wrap items-center gap-x-8 gap-y-2 border-t border-white/20 pt-6"
              style={{ animationDelay: '480ms' }}
            >
              <span className="text-xs capitalize text-brand-100">{hoy}</span>
              <span className="flex items-center gap-2 text-xs text-brand-100">
                <span className="h-1.5 w-1.5 rounded-full bg-brand-300" />
                Sistema operativo
              </span>
              <span className="text-xs text-brand-100">15 módulos activos</span>
              <span className="text-xs text-brand-100">Multi-empresa</span>
            </div>
          </div>
        </div>
      </section>

      {/* ── PILARES ────────────────────────────────── */}
      <section id="servicios" className="bg-brand-50 px-6 py-24">
        <div className="mx-auto max-w-7xl">
          <Revelar>
            <p className="mb-3 text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-600">
              Áreas de gestión
            </p>
            <h2 className="mb-12 text-3xl font-bold tracking-tight text-slate-900 md:text-4xl">
              Todo lo que necesita su clínica
            </h2>
          </Revelar>
          <div className="grid-escalonado grid grid-cols-1 gap-6 md:grid-cols-3">
            <PilarCard
              Icono={Stethoscope}
              titulo="Clínica"
              descripcion="Gestión completa del flujo de atención médica veterinaria."
              items={['Agenda de citas', 'Historias clínicas', 'Vacunación', 'Recetas médicas']}
              iconBg="bg-brand-100"
              iconColor="text-brand-700"
              acento="border-brand-500"
            />
            <PilarCard
              Icono={Package}
              titulo="Comercial"
              descripcion="Control de productos, stock y facturación al cliente."
              items={['Catálogo de productos', 'Control de inventario', 'Alertas de stock', 'Facturación con IVA']}
              iconBg="bg-acento-100"
              iconColor="text-acento-700"
              acento="border-acento-500"
            />
            <PilarCard
              Icono={ShieldCheck}
              titulo="Administración"
              descripcion="Configuración de usuarios, roles y estructura multi-empresa."
              items={['Gestión de usuarios', 'Roles y permisos', 'Perfiles veterinarios', 'Multi-empresa']}
              iconBg="bg-brand-50"
              iconColor="text-brand-800"
              acento="border-brand-800"
            />
          </div>
        </div>
      </section>

      {/* ── MÓDULOS ────────────────────────────────── */}
      <section id="modulos" className="bg-white px-6 py-24">
        <div className="mx-auto max-w-7xl">
          <Revelar>
            <p className="mb-3 text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-600">
              Módulos del sistema
            </p>
            <h2 className="mb-4 text-3xl font-bold tracking-tight text-slate-900 md:text-4xl">
              Funcionalidad integrada
            </h2>
            <p className="mb-12 max-w-xl text-base leading-relaxed text-slate-500">
              Cada módulo está diseñado para el flujo real de una clínica veterinaria, conectado con el resto del sistema.
            </p>
          </Revelar>
          <div className="grid-escalonado grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {MODULOS.map((m) => (
              <ModuloCard key={m.nombre} {...m} />
            ))}
          </div>
        </div>
      </section>

      {/* ── STATS ──────────────────────────────────── */}
      <section className="bg-gradient-to-r from-brand-800 via-brand-900 to-brand-950 px-6 py-16">
        <div className="mx-auto max-w-7xl">
          <Revelar className="flex flex-wrap items-center justify-around gap-10 text-center">
            <StatItem valor="15+" etiqueta="Módulos" />
            <StatItem valor="360°" etiqueta="Visión del paciente" />
            <StatItem valor="∞" etiqueta="Registros clínicos" />
            <StatItem valor="1" etiqueta="Sistema unificado" />
          </Revelar>
        </div>
      </section>

      {/* ── CTA FINAL ──────────────────────────────── */}
      <section className="bg-gradient-to-b from-brand-900 to-brand-950 px-6 py-24 text-center">
        <div className="mx-auto max-w-2xl">
          <Revelar>
            <p className="mb-3 text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-300">
              Listo para empezar
            </p>
            <h2 className="mb-4 text-3xl font-bold text-white md:text-4xl">
              Su clínica, organizada.
            </h2>
            <p className="mb-8 text-base leading-relaxed text-brand-100">
              Acceda al sistema con su usuario y comience a gestionar su clínica veterinaria de forma profesional.
            </p>
            <Link
              to="/acceso"
              className="inline-flex items-center gap-2 rounded-lg bg-brand-500 px-8 py-3.5 text-sm font-semibold text-white shadow-marca transition-all hover:-translate-y-0.5 hover:bg-brand-400"
            >
              Iniciar sesión
              <ArrowRight className="h-4 w-4" />
            </Link>
          </Revelar>
        </div>
      </section>

      {/* ── FOOTER ─────────────────────────────────── */}
      <footer className="border-t border-white/10 bg-brand-950 px-6 py-8">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4">
          <Link to="/" className="flex items-center" aria-label="ControlPets">
            <MarcaControlPets tamano="sm" />
          </Link>
          <div className="flex items-center gap-6">
            <Link to="/acceso" className="text-xs text-acento-300 transition-colors hover:text-white">
              Iniciar sesión
            </Link>
            <span className="text-xs text-brand-300">
              © {new Date().getFullYear()} · DEVs ITQ. Todos los derechos reservados.
            </span>
          </div>
        </div>
      </footer>
    </div>
  )
}

function PilarCard({
  Icono,
  titulo,
  descripcion,
  items,
  iconBg,
  iconColor,
  acento,
}: {
  Icono: React.ComponentType<{ className?: string }>
  titulo: string
  descripcion: string
  items: string[]
  iconBg: string
  iconColor: string
  acento: string
}) {
  return (
    <div
      className={`group rounded-xl border-t-4 bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md ${acento}`}
    >
      <div
        className={`mb-4 flex h-11 w-11 items-center justify-center rounded-lg transition-transform duration-200 group-hover:scale-110 ${iconBg}`}
      >
        <Icono className={`h-5 w-5 ${iconColor}`} />
      </div>
      <h3 className="mb-2 text-lg font-bold text-slate-900">{titulo}</h3>
      <p className="mb-5 text-sm leading-relaxed text-slate-500">{descripcion}</p>
      <ul className="space-y-2">
        {items.map((item) => (
          <li key={item} className="flex items-center gap-2.5 text-sm text-slate-600">
            <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-acento-500 transition-transform duration-200 group-hover:scale-150" />
            {item}
          </li>
        ))}
      </ul>
    </div>
  )
}

function ModuloCard({
  Icono,
  nombre,
  descripcion,
  color,
}: {
  Icono: React.ComponentType<{ className?: string }>
  nombre: string
  descripcion: string
  color: string
}) {
  return (
    <div className="group rounded-xl border border-slate-200 bg-white p-5 transition-all hover:-translate-y-0.5 hover:border-brand-300 hover:shadow-md">
      <div
        className={`mb-4 flex h-10 w-10 items-center justify-center rounded-lg transition-transform duration-200 group-hover:scale-110 ${color}`}
      >
        <Icono className="h-5 w-5" />
      </div>
      <h3 className="mb-1.5 text-sm font-semibold text-slate-900">{nombre}</h3>
      <p className="text-sm leading-relaxed text-slate-500">{descripcion}</p>
    </div>
  )
}

function StatItem({ valor, etiqueta }: { valor: string; etiqueta: string }) {
  return (
    <div className="group">
      <p className="text-4xl font-extrabold tracking-tight text-white transition-transform duration-200 group-hover:scale-105">
        {valor}
      </p>
      <p className="mt-1 text-sm text-brand-200">{etiqueta}</p>
    </div>
  )
}

