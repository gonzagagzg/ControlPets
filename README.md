# ControlPets - Sistema de Gestión Veterinaria

ControlPets es una aplicación web completa para la administración y gestión de clínicas veterinarias. Permite llevar un control integral de mascotas, clientes, citas, historiales clínicos, facturación, inventario, vacunación y más.

---

## 🎯 Propósito

ControlPets está diseñado para digitalizar y optimizar la gestión diaria de una clínica veterinaria, proporcionando herramientas para:

- **Gestión de mascotas**: Registro, historiales clínicos, vacunación, recetas médicas
- **Gestión de clientes**: Datos de propietarios, historial de mascotas, facturación
- **Agenda de citas**: Programación, seguimiento y control de citas médicas
- **Facturación y contabilidad**: Emisión de facturas, control de pagos, mensualidades empresariales
- **Inventario y productos**: Control de stock, movimientos de inventario, catálogos
- **Administración de usuarios y roles**: Control de acceso por perfiles (veterinarios, recepción, administradores)
- **Reportes y estadísticas**: Dashboards con métricas clave del negocio

---

## 🏗️ Arquitectura

```
ControlPets/
├── Backend/          # API REST (Java 17 + Servlets + Maven)
│   └── src/main/java/com/itq/
│       ├── model/       # Entidades JPA (23 modelos)
│       ├── servlet/     # Endpoints REST (26 servlets)
│       ├── dao/         # Acceso a datos
│       ├── service/     # Lógica de negocio
│       ├── security/    # JWT, filtros, autenticación
│       └── dto/         # Transferencia de datos
│
├── Frontend/         # SPA (React 18 + TypeScript + Vite)
│   └── src/
│       ├── features/    # Módulos funcionales (15 features)
│       ├── shared/      # Componentes, hooks, utils compartidos
│       └── app/         # Configuración global, routing
│
└── BD/               # Scripts de base de datos (PostgreSQL)
```

---

## 🛠️ Stack Tecnológico

### Backend
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17 | Lenguaje principal |
| Jakarta Servlets | 6.0 | API REST |
| Maven | - | Gestión de dependencias y build |
| PostgreSQL | - | Base de datos relacional |
| HikariCP | 6.3 | Pool de conexiones |
| JWT (Auth0) | 4.5 | Autenticación stateless |
| BCrypt | 0.4 | Hash de contraseñas |
| Gson | 2.13 | Serialización JSON |
| Jakarta Mail | 2.0 | Envío de emails |
| Dotenv | 3.0 | Variables de entorno |

### Frontend
| Tecnología | Versión | Uso |
|------------|---------|-----|
| React | 18.3 | UI Library |
| TypeScript | 5.7 | Tipado estático |
| Vite | 6.0 | Build tool & dev server |
| Tailwind CSS | 3.4 | Estilos utilitarios |
| React Router | 6.28 | Enrutamiento SPA |
| TanStack Query | 5.62 | Server state management |
| React Hook Form | 7.54 | Formularios |
| Zod | 3.24 | Validación de esquemas |
| Zustand | 5.0 | Client state management |
| Recharts | 2.15 | Gráficos y dashboards |
| TanStack Table | 8.20 | Tablas de datos |
| Lucide React | 0.468 | Iconografía |
| date-fns | 4.1 | Manipulación de fechas |

---

## 📦 Módulos Principales (Features)

| Módulo | Descripción |
|--------|-------------|
| **Acceso** | Login, registro, recuperación de contraseña, gestión de sesiones |
| **Admin** | Panel de administración, usuarios, roles, permisos, empresas |
| **Catálogos** | Especies, razas, categorías, IVA (SRI) |
| **Citas** | Agenda, programación, estados, recordatorios |
| **Clientes** | CRUD propietarios, historial, búsqueda avanzada |
| **Facturación** | Facturas, detalles, notas de crédito, reportes tributarios |
| **Historiales** | Historias clínicas, evolución, anexos |
| **Inicio** | Dashboard principal, estadísticas, accesos rápidos |
| **Inventario** | Movimientos, stock, kardex, alertas de mínimo |
| **Mascotas** | Ficha completa, fotos, alergias, propietario |
| **Mensualidades** | Planes empresariales, cobros recurrentes, vencimientos |
| **Panel** | Vista personalizada por rol (veterinario, recepción, admin) |
| **Productos** | Catálogo, precios, presentación, laboratorio |
| **Recetas** | Prescripciones médicas, medicamentos, dosificación |
| **Vacunación** | Calendario, aplicación, recordatorios, certificados |

---

## 🗄️ Modelo de Datos (Entidades Principales)

- **Usuario** - Personal del sistema (veterinarios, recepción, admins)
- **Rol / Permiso** - Control de acceso basado en roles (RBAC)
- **Empresa** - Multi-tenancy para cadenas veterinarias
- **Cliente** - Propietarios de mascotas
- **Mascota** - Pacientes (especie, raza, sexo, fecha nacimiento, foto)
- **Veterinario** - Profesionales con especialidad, horario
- **Cita** - Agenda médica (fecha, hora, motivo, estado, veterinario)
- **HistorialClinico** - Registro clínico por cita (anamnesis, diagnóstico, tratamiento)
- **Receta / RecetaDetalle** - Prescripciones y sus ítems
- **Vacuna / MascotaVacuna** - Catálogo de vacunas y registro de aplicaciones
- **Producto / Categoria** - Inventario y catálogo de venta
- **Factura / FacturaDetalle** - Facturación electrónica (SRI Ecuador)
- **MovimientoInventario** - Entradas/salidas/ajustes de stock
- **MensualidadEmpresa** - Planes de suscripción para empresas cliente

---

## 🚀 Instalación y Ejecución

### Prerrequisitos
- **Java 17+** (JDK)
- **Maven 3.8+**
- **Node.js 18+** y **pnpm** (recomendado) o npm
- **PostgreSQL 14+**
- **Apache Tomcat 10.1+** (para deploy del WAR)

### 1. Base de Datos
```sql
-- Crear base de datos
CREATE DATABASE controlpets;
-- Ejecutar scripts en BD/ para crear tablas y datos iniciales
```

### 2. Backend
```bash
cd Backend

# Configurar variables de entorno
cp .env.example .env
# Editar .env con credenciales de BD, JWT secret, email, etc.

# Compilar y generar WAR
mvn clean package

# Desplegar en Tomcat
# Copiar target/VETERINARIAITQ.war a $CATALINA_HOME/webapps/
# Iniciar Tomcat
```

### 3. Frontend
```bash
cd Frontend

# Instalar dependencias
pnpm install

# Configurar variables de entorno
cp .env.example .env
# Editar .env con VITE_API_URL=http://localhost:8080/VETERINARIAITQ/api

# Desarrollo
pnpm dev

# Producción
pnpm build
# Servir dist/ con nginx o Apache
```

---

## 📊 Funcionalidades Destacadas

### Para Veterinarios
- ✅ Vista de agenda diaria/semanal
- ✅ Historia clínica completa con evolución
- ✅ Recetario digital con medicamentos
- ✅ Registro de vacunación y certificados
- ✅ Dashboard de pacientes asignados

### Para Recepción
- ✅ Gestión de citas (crear, reprogramar, cancelar)
- ✅ Check-in de pacientes
- ✅ Facturación punto de venta
- ✅ Registro de nuevos clientes/mascotas
- ✅ Consulta de disponibilidad veterinarios

### Para Administradores
- ✅ Gestión de usuarios, roles y permisos
- ✅ Configuración de empresa/sucursales
- ✅ Catálogos maestros (especies, razas, IVA)
- ✅ Reportes financieros y operacionales
- ✅ Control de inventario y alertas de stock
- ✅ Gestión de mensualidades empresariales

