import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
  Boxes,
  Building2,
  CalendarDays,
  ChevronRight,
  ClipboardList,
  Dog,
  LayoutDashboard,
  LogOut,
  Menu,
  Package,
  Receipt,
  ShieldCheck,
  Stethoscope,
  Syringe,
  Tags,
  Users,
  UserSquare,
  X,
} from 'lucide-react'
import { LogoIcono, LogoTexto } from '@/shared/components/Logo'
import { cn } from '@/shared/lib/utils'
import { esSuperUsuario, useSesion } from '@/shared/session/sesion'
import { usePermisos } from '@/shared/session/permisos'
import { BloqueoEmpresa } from './BloqueoEmpresa'

interface ItemNav {
  ruta: string
  etiqueta: string
  Icono: React.ComponentType<{ className?: string }>
  exacto?: boolean
  /**
   * Módulo RBAC que protege esta ruta en el backend (ver Autorizacion.exigir
   * en cada Servlet). `null` = sin permiso dedicado, visible para cualquier
   * usuario autenticado. `soloSuperUsuario` = reservado (ver EmpresaServlet).
   */
  modulo: string | null
  soloSuperUsuario?: boolean
  /**
   * Ítems que no aplican al SuperUsuario porque operan sobre la veterinaria
   * del JWT, que él no tiene (p. ej. Mensualidades o Veterinarios).
   */
  ocultoParaSuperUsuario?: boolean
}

const SECCIONES: { titulo: string; items: ItemNav[] }[] = [
  {
    titulo: 'General',
    items: [{ ruta: '/app', etiqueta: 'Panel', Icono: LayoutDashboard, exacto: true, modulo: null }],
  },
  {
    titulo: 'Clínica',
    items: [
      { ruta: '/app/citas', etiqueta: 'Agenda de citas', Icono: CalendarDays, modulo: 'CITAS' },
      { ruta: '/app/clientes', etiqueta: 'Clientes', Icono: Users, modulo: 'CLIENTES' },
      { ruta: '/app/mascotas', etiqueta: 'Mascotas', Icono: Dog, modulo: 'MASCOTAS' },
      {
        ruta: '/app/historiales',
        etiqueta: 'Historias clínicas',
        Icono: Stethoscope,
        modulo: 'HISTORIALES',
      },
      { ruta: '/app/vacunacion', etiqueta: 'Vacunación', Icono: Syringe, modulo: 'MASCOTAS' },
      { ruta: '/app/recetas', etiqueta: 'Recetas', Icono: ClipboardList, modulo: null },
    ],
  },
  {
    titulo: 'Cuenta',
    items: [
      {
        ruta: '/app/mensualidades',
        etiqueta: 'Mensualidades',
        Icono: Receipt,
        modulo: 'MENSUALIDADES',
        ocultoParaSuperUsuario: true,
      },
    ],
  },
  {
    titulo: 'Comercial',
    items: [
      { ruta: '/app/productos', etiqueta: 'Productos', Icono: Package, modulo: 'PRODUCTOS' },
      { ruta: '/app/inventario', etiqueta: 'Inventario', Icono: Boxes, modulo: 'INVENTARIO' },
      { ruta: '/app/facturas', etiqueta: 'Facturación', Icono: Receipt, modulo: 'FACTURAS' },
    ],
  },
  {
    titulo: 'Configuración',
    items: [
      { ruta: '/app/catalogos', etiqueta: 'Catálogos', Icono: Tags, modulo: 'CATEGORIAS' },
      { ruta: '/app/usuarios', etiqueta: 'Usuarios', Icono: UserSquare, modulo: 'USUARIOS' },
      {
        ruta: '/app/veterinarios',
        etiqueta: 'Veterinarios',
        Icono: Stethoscope,
        modulo: 'VETERINARIOS',
        // El SuperUsuario no tiene veterinaria propia: no hay veterinarios que gestionar.
        ocultoParaSuperUsuario: true,
      },
      { ruta: '/app/accesos', etiqueta: 'Roles y permisos', Icono: ShieldCheck, modulo: 'USUARIOS' },
      {
        ruta: '/app/empresas',
        etiqueta: 'Veterinarias (SaaS)',
        Icono: Building2,
        modulo: 'EMPRESAS',
        soloSuperUsuario: true,
      },
    ],
  },
]

export function AppLayout() {
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const { rol, nombreUsuario, limpiar } = useSesion()
  const permisos = usePermisos()
  const esSuper = esSuperUsuario(rol)
  const [menuAbierto, setMenuAbierto] = useState(false)

  // El SuperUsuario no opera dentro de una veterinaria concreta: del menú
  // clínico/comercial solo le aplica "Configuración" (Veterinarias/SaaS, etc.)
  // más el "General → Panel", que para él es el panel estadístico global
  // (PaginaPanelSuperUsuario). El resto de secciones no le corresponde.
  const secciones = SECCIONES.filter(
    (seccion) =>
      !esSuper || seccion.titulo === 'General' || seccion.titulo === 'Configuración',
  )
    .map((seccion) => ({
      ...seccion,
      items: seccion.items.filter((item) => {
        if (item.soloSuperUsuario) return esSuper
        if (item.ocultoParaSuperUsuario && esSuper) return false
        if (item.modulo === null) return true
        return permisos.tieneAccesoAModulo(item.modulo)
      }),
    }))
    .filter((seccion) => seccion.items.length > 0)

  // Ruta activa, para el breadcrumb de la barra superior de escritorio.
  const itemActual = secciones
    .flatMap((seccion) => seccion.items.map((item) => ({ ...item, seccion: seccion.titulo })))
    .filter((item) => pathname === item.ruta || pathname.startsWith(`${item.ruta}/`))
    .sort((a, b) => b.ruta.length - a.ruta.length)[0]

  // Iniciales para el avatar de la barra superior (máx. 2 letras).
  const iniciales =
    (nombreUsuario ?? '')
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((parte) => parte.charAt(0).toUpperCase())
      .join('') || '?'

  // Cierra el menú móvil al cambiar de ruta.
  useEffect(() => {
    setMenuAbierto(false)
  }, [pathname])

  // Mientras el drawer está abierto: bloquea el scroll del fondo y cierra con Esc.
  useEffect(() => {
    if (!menuAbierto) return

    const alPresionarTecla = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') setMenuAbierto(false)
    }
    document.addEventListener('keydown', alPresionarTecla)

    const overflowPrevio = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.removeEventListener('keydown', alPresionarTecla)
      document.body.style.overflow = overflowPrevio
    }
  }, [menuAbierto])

  function cerrarSesion() {
    limpiar()
    navigate('/acceso')
  }

  const marca = (
    <>
      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white/15 ring-1 ring-white/25">
        <LogoIcono alt="" className="h-6 w-6" />
      </span>
      <div className="min-w-0">
        <LogoTexto tamano="md" className="block truncate" />
        <p className="mt-0.5 truncate text-xs text-brand-100">{rol ?? 'Sin rol'}</p>
      </div>
    </>
  )

  const navegacion = (
    <nav className="flex-1 space-y-6 overflow-y-auto px-3 py-5">
      {secciones.map((seccion) => (
        <div key={seccion.titulo}>
          <p className="px-2 pb-2 text-[10px] font-semibold uppercase tracking-widest text-brand-200">
            {seccion.titulo}
          </p>
          <div className="space-y-1">
            {seccion.items.map(({ ruta, etiqueta, Icono, exacto }) => (
              <NavLink
                key={ruta}
                to={ruta}
                end={exacto}
                className={({ isActive }) =>
                  cn(
                    'group relative flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                    isActive
                      ? 'bg-white font-semibold text-brand-700 shadow-sm'
                      : 'text-brand-50/85 hover:bg-white/10 hover:text-white',
                  )
                }
              >
                {({ isActive }) => (
                  <>
                    {isActive && (
                      <span className="absolute -left-3 top-1/2 h-5 w-1 -translate-y-1/2 animate-escala rounded-r-full bg-acento-400" />
                    )}
                    <Icono className="h-4 w-4 shrink-0 transition-transform duration-200 group-hover:scale-110" />
                    <span className="truncate">{etiqueta}</span>
                  </>
                )}
              </NavLink>
            ))}
          </div>
        </div>
      ))}
    </nav>
  )

  const pieSidebar = (
    <div className="border-t border-white/15 p-3">
      <div className="mb-2 rounded-lg px-3 py-2">
        <p className="truncate text-sm font-semibold text-white">{nombreUsuario}</p>
        <p className="text-xs text-brand-100">Sesión activa</p>
      </div>
      <button
        type="button"
        onClick={cerrarSesion}
        className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-brand-50/85 transition-colors hover:bg-white/10 hover:text-white"
      >
        <LogOut className="h-4 w-4" />
        Cerrar sesión
      </button>
    </div>
  )

  return (
    <>
      <BloqueoEmpresa />

      <div className="flex min-h-screen flex-col">
        {/* Barra superior (solo móvil/tablet) */}
        <header className="sticky top-0 z-30 flex items-center justify-between gap-2 bg-gradient-to-r from-brand-700 to-brand-900 px-2.5 py-2.5 text-white shadow-md lg:hidden">
          <button
            type="button"
            onClick={() => setMenuAbierto(true)}
            aria-label="Abrir menú"
            className="rounded-lg p-2 transition-colors hover:bg-white/10"
          >
            <Menu className="h-5 w-5" />
          </button>
          <span className="inline-flex items-center gap-2">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-white/15 ring-1 ring-white/25">
              <LogoIcono alt="" className="h-5 w-5" />
            </span>
            <LogoTexto tamano="sm" />
          </span>
          <button
            type="button"
            onClick={cerrarSesion}
            aria-label="Cerrar sesión"
            className="rounded-lg p-2 transition-colors hover:bg-white/10"
          >
            <LogOut className="h-5 w-5" />
          </button>
        </header>

        <div className="flex flex-1">
          {/* Sidebar escritorio */}
          <aside className="hidden w-64 shrink-0 flex-col bg-gradient-to-b from-brand-600 via-brand-700 to-brand-900 lg:sticky lg:top-0 lg:flex lg:h-screen">
            <div className="flex items-center gap-3 border-b border-white/15 px-5 py-5">
              {marca}
            </div>
            {navegacion}
            {pieSidebar}
          </aside>

          <main className="min-w-0 flex-1">
            {/* Barra superior de escritorio: breadcrumb + identidad de sesión */}
            <header className="cristal sticky top-0 z-20 hidden items-center justify-between gap-4 border-b border-slate-200/70 px-8 py-3 lg:flex">
              <nav aria-label="Ruta de navegación" className="flex items-center gap-1.5 text-sm">
                <span className="text-slate-400">{itemActual?.seccion ?? 'General'}</span>
                <ChevronRight className="h-3.5 w-3.5 text-slate-300" />
                <span className="font-semibold text-slate-800">
                  {itemActual?.etiqueta ?? 'Panel'}
                </span>
              </nav>
              <div className="flex items-center gap-3">
                <div className="text-right leading-tight">
                  <p className="text-sm font-semibold text-slate-800">{nombreUsuario}</p>
                  <p className="text-[11px] text-slate-400">{rol}</p>
                </div>
                <span
                  aria-hidden="true"
                  className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-brand-700 text-xs font-bold text-white shadow-sm"
                >
                  {iniciales}
                </span>
              </div>
            </header>

            <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
              {/* key por ruta: al navegar re-monta y dispara la transición de entrada */}
              <div key={pathname} className="animate-aparecer">
                <Outlet />
              </div>
            </div>
          </main>
        </div>
      </div>

      {/* Navegación móvil (drawer lateral) */}
      {menuAbierto && (
        <div
          className="fixed inset-0 z-50 lg:hidden"
          role="dialog"
          aria-modal="true"
          aria-label="Menú de navegación"
        >
          <div
            className="animate-aparecer absolute inset-0 bg-brand-950/60 backdrop-blur-sm"
            onClick={() => setMenuAbierto(false)}
            aria-hidden="true"
          />
          <aside className="animate-deslizar-izq absolute inset-y-0 left-0 flex w-72 max-w-[85vw] flex-col bg-gradient-to-b from-brand-600 via-brand-700 to-brand-900 shadow-2xl">
            <div className="flex items-center justify-between gap-3 border-b border-white/15 px-4 py-4">
              {marca}
              <button
                type="button"
                onClick={() => setMenuAbierto(false)}
                aria-label="Cerrar menú"
                className="shrink-0 rounded-lg p-2 text-brand-50/85 transition-colors hover:bg-white/10 hover:text-white"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            {navegacion}
            {pieSidebar}
          </aside>
        </div>
      )}
    </>
  )
}
