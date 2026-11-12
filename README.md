> **Preparación de despliegue AV2 v2.5.1:** consultar [`docs/DEPLOY_RENDER_NEON_AV2.md`](docs/DEPLOY_RENDER_NEON_AV2.md). Este paquete no implica que Render o Neon ya estén configurados.

# SafeLab v2.5.0 RC3 — Sincronización móvil y paridad API (2026-11-07)

> **Candidata de validación local.** No crear todavía un tag estable: primero aprobar `npm run build`, compilación Android, tests Maven y pruebas reales PostgreSQL.

## Incidencias corregidas respecto de RC2

- **Mobile:** `GET /api/v1/integration/bootstrap` se vuelve a consultar al cambiar módulo/pestaña o submódulo, y al recuperar el primer plano. Hay un botón **Actualizar datos** y un mensaje de error con opción de reintento; **ya no hace falta cerrar sesión** para obtener cambios de Web. No se implementaron WebSockets ni sincronización push instantánea.
- **Mobile:** las colecciones de **activos** y **sensores** ofrecen editar y eliminar, y **mantenimiento** ofrece editar, cuando el token tiene el permiso correspondiente. El formulario de activos incluye nombre, categoría, sede, ubicación, estado, fecha y responsable existente por ID (solo el gerente). Sensores permiten configuración de umbrales y envío de lectura; eliminaciones piden confirmación y respetan las FK.
- **Mobile:** acciones del backend para reconocer/resolver/escalar alertas, investigar/resolver/cerrar incidentes y activar/pausar reglas de cumplimiento, de acuerdo con los permisos. Se conserva el alta de recursos, invitaciones y asignaciones de RC2.
- **Web + API + Mobile:** los permisos efectivos se publican en el bootstrap autenticado, en vez de que cada interfaz los adivine desde valores simulados. El backend sigue validando permisos en cada endpoint. El backend limita el permiso implícito del gerente a su catálogo oficial y evita recursos sin asignación para roles técnicos.
- **Backend:** HTTP 409 claro al intentar borrar equipos o sensores que conservan referencias clínicas; **V1–V4 de Flyway no se modificaron**. No borrar ni reinicializar `safelab_db`.
- **Relaciones:** se han comprobado las referencias del seed JSON en 11 colecciones sin encontrar IDs inexistentes ni cruces entre organizaciones; no equivale a verificar la base local ya modificada. Ejecutar `database/diagnostics/verify_rc3_relations.sql` en pgAdmin.

## Compatibilidad y límites

**Leer:** los 12 BC siguen consumiendo colecciones autorizadas de Spring Boot.
**Escribir:** sólo existen operaciones reales para endpoints específicos. Las pantallas sin API equivalente continúan siendo de consulta o demostración; no se simula un guardado exitoso. Para detalle consulta `docs/COMPATIBILITY_MATRIX_RC3.md`.
**Sincronización:** al navegar, volver a primer plano o pulsar Actualizar; no es tiempo real continuo mientras ambas pantallas permanecen abiertas.
**Roles:** 403 del backend es la autoridad; los botones visibles no garantizan permisos si la sesión cambió.

### Orden para pruebas

1. Haz copia de seguridad de `safelab_db` en pgAdmin. Detén Web/Mobile/Backend.
2. Inicia **Backend RC3** en IntelliJ (`postgres`, las cinco variables privadas ya configuradas). Verifica Swagger y Flyway V4.
3. Inicia **Web RC3** (`npm install`, `npm run dev`, opcional `npm run build`).
4. Ejecuta **Mobile RC3** en Android Emulator (servidor local `http://10.0.2.2:8080/api/v1`).
5. Ingresa como gerente en ambas aplicaciones. Crea un activo en Web; cambia de módulo en Mobile y vuelve a Activos: debe aparecer **sin cerrar sesión**.
6. Edita ubicación/estado desde Mobile; cambia de módulo en Web y vuelve: verifica el mismo registro.
7. Comprueba actualización desde pgAdmin, intenta eliminar un activo sin dependencias y uno con sensores vinculados (este último debe devolver 409), y valida una cuenta de técnico sin permisos de edición.
8. Corre las consultas de diagnóstico y la matriz QA antes de cualquier commit o tag.

---

# SafeLab Platform API — v2.5.0 (API local)

## Corrección de asignaciones de activos — v2.5.0 RC2 (2026-11-07)

**Motivo:** en la entrega RC1 `responsible` era texto libre y `users.resourceIds` era el
control de acceso. Por eso poner "Andrea Torres" en un activo no lo hacía visible
para esa cuenta. Los KPI estaban bien calculados según los activos autorizados,
pero la asignación era inconsistente.

**Cambios:** `responsibleUserId` es el vínculo por ID al empleado existente. El gerente
elige de una lista de empleados activos de **su empresa** y **sede autorizada**;
Spring Boot valida esas restricciones y guarda tanto el activo como su asignación
(`users.resourceIds`). El responsable anterior pierde el acceso derivado de ese
vínculo cuando se transfiere; los recursos adicionales concedidos explícitamente
a través de *Users & Permissions* siguen siendo una autorización independiente.
Los KPI se recalculan sobre la lista actualizada tras volver a sincronizar.

- **PostgreSQL:** Flyway `V4__asset_responsible_user_assignment.sql` añade una
  referencia SQL verificada por empresa, enlaza registros legados cuyo nombre
  coincida con un único empleado activo autorizado para la sede y añade esos
  activos a `resource_ids` sin borrar otras asignaciones. No borra datos, no
  repite el seed y no altera V1–V3.
- **Web:** el campo *Responsible* deja de ser un texto libre y pasa a ser un
  selector de empleados existentes; el formulario envía el ID. Al crear también
  persiste `status` y `lastInspection` (antes se perdían en POST).
- **Android:** en el formulario de alta desde *Facilities* el gerente puede
  elegir un empleado existente; el perfil del empleado y su colección de activos
  se refrescan desde la API cuando la aplicación retoma actividad.
- **JSON Server:** la semilla de demostración conserva el mismo criterio de IDs;
  la actualización de una base JSON de ejecución requiere reset explícito.

**Importante:** no crear aún el tag v2.5.0 ni publicar RC2 como versión final.
Esta es una candidata a pruebas; Maven, Vite, Gradle, PostgreSQL y emulador
requieren comprobación completa en el equipo local.

### Orden obligatorio de prueba

1. Detener los tres procesos. Hacer una **copia de seguridad de `safelab_db`**.
2. Sustituir primero el backend por RC2 (mantener variables privadas actuales).
3. Iniciarlo con perfil `postgres`: Flyway aplica automáticamente **V4 una sola vez**.
   Comprobar en `flyway_schema_history` que la V4 finalizó correctamente.
4. Ejecutar Web RC2 con `npm install` y `npm run dev` (no se necesita su `npm run server`).
5. Entrar como gerente, editar `Freezer QA Web v2.5`, seleccionar **Andrea Torres**
   en el nuevo desplegable y guardar. Cerrar e iniciar la sesión de Andrea,
   o refrescarla, para que vuelva a consultar el alcance.
6. Comprobar el inventario y los KPI de Andrea, los IDs de usuario de los activos
   y el cambio en PostgreSQL. El total **debería ser 7 solo si** permanecen los
   6 activos del seed asignados a Andrea y el Freezer QA de prueba se vincula a ella;
   cualquier filtro de sede o permisos adicional cambia el resultado.
7. Abrir Mobile RC2 en emulador, iniciar sesión como Andrea y revisar su listado.

Consultas SQL de verificación (pgAdmin → `safelab_db`):

```sql
SELECT installed_rank, version, description, success
FROM public.flyway_schema_history ORDER BY installed_rank;

SELECT a.id, a.name, a.responsible, a.responsible_user_id,
       u.full_name AS employee_name, a.facility_id
FROM public.assets a
LEFT JOIN public.users u ON a.responsible_user_id = u.id
WHERE a.organization_id = 'org-andes'
ORDER BY a.name;

SELECT u.full_name, u.resource_ids
FROM public.users u WHERE u.full_name = 'Andrea Torres';

SELECT count(*) AS linked_assets
FROM public.assets a JOIN public.users u
 ON a.responsible_user_id = u.id
WHERE u.full_name = 'Andrea Torres' AND a.organization_id = 'org-andes';
```

**Diferencia entre roles:** la lista del gerente muestra todos los activos de su
organización, mientras la de Andrea solo muestra aquellos autorizados por su
alcance y sedes. `responsible` es una etiqueta legada; el identificador seguro
es `responsibleUserId`. Los ajustes manuales en Users & Permissions conceden
acceso, pero no cambian por sí mismos quién figura como responsable principal.

---


## Integración local v2.5.0 (desarrollo)

Esta versión **no despliega servicios**. Las tres aplicaciones siguen separadas, pero Web y Mobile consumen HTTP/JSON desde **SafeLab Platform API**. La API es la única que conoce las credenciales PostgreSQL, realiza las validaciones y aplica IAM/RBAC/RLS. Ninguna contraseña SQL se incorpora a Vue o Android.

### Arquitectura

```text
Vue (localhost:5173)  ──────┐
                            ├── Spring Boot API (localhost:8080) ── PostgreSQL (5432)
Android emulator (10.0.2.2) ┘
```

**JSON Server se conserva en el repositorio backend** como perfil `jsonserver` opcional. Los frontends siempre consumen la API Spring Boot, nunca `data/db.json` ni PostgreSQL directamente.

### Qué está conectado

- Login real y Bearer token, con datos de sesión obtenidos de Spring Boot.
- `GET /api/v1/integration/bootstrap`: snapshot multiempresa filtrado en servidor, sin contraseñas. Las pantallas de los 12 BC pueden leer las colecciones autorizadas.
- Altas y modificaciones principales de activos y sensores, incidentes, órdenes de mantenimiento y reportes, usando endpoints específicos, no un CRUD global abierto.
- Acciones de alertas, comandos simulados, invitaciones, permisos y asignaciones de empleados donde el frontend dispone de la operación correspondiente.
- Refresco de datos Web al recuperar el foco y Mobile al reanudar la actividad. **No hay WebSocket ni actualización instantánea en segundo plano**.

### Limitaciones explícitas

No todos los flujos heredados del frontend tienen equivalentes seguros en el backend v2. Los controles de configuración global de plataforma, simulaciones puramente visuales, carga de evidencias/archivos y algunas operaciones de gestión especial aún requieren endpoints y pruebas adicionales. No deben considerarse persistidos solo porque una pantalla cambie de apariencia. El modo Android de depuración permite HTTP a la máquina del desarrollador; en producción se requerirá HTTPS y una URL real.

La integración se considera **candidata a validación**, no aprobada ni lista para producción, hasta ejecutar `mvn test`, `npm run build`, el build Android y la matriz de pruebas cruzadas indicada en `docs/INTEGRATION_TEST_PLAN.md`.

---

## Historial y guía de la versión anterior

# SafeLab Platform API — v2.0.0

Backend REST en Java 21 / Spring Boot 3.4.5 para los **12 Bounded Contexts** de SafeLab.
Conserva los contratos REST y la documentación **Swagger ES/EN** de v1.0.0 y añade PostgreSQL 18.

> **Estado:** entrega de código para validación local. No desplegar en producción: JSON Server y las cuentas demo son solo para desarrollo. La compilación Maven y las pruebas contra PostgreSQL del equipo deben confirmarse antes del tag `backend-v2.0.0`.

## Elegir almacenamiento

| Perfil | API | Fuente de datos | Requisitos |
|---|---|---|---|
| **postgres** (por defecto) | http://localhost:8080 | PostgreSQL `safelab_db` | PostgreSQL + 2 credenciales SQL |
| **jsonserver** | http://localhost:8081 | JSON Server http://127.0.0.1:3000 | Node.js + `npm install` + `npm run server` |

**Las rutas `/api/v1/**` son iguales**. Los perfiles cambian únicamente el adaptador de persistencia y la sesión. PostgreSQL requiere configuración inicial una vez; JSON Server no. Las dos opciones de Swagger `es` / `en` traducen la documentación, no cambian la base de datos. Si ambos backends corren, cada uno debe utilizar un puerto distinto. Swagger mostrará **Servers** con ambas URLs locales; elegir una dirige las peticiones a esa instancia, pero no cambia el perfil ni la BD de una instancia ya iniciada.

## Inicio rápido — PostgreSQL local

1. Instala **JDK 21**, Maven, PostgreSQL 18 (incluye `psql`) e IntelliJ IDEA. Verifica `java -version`, `mvn -version` y `psql --version`.
2. Primera instalación: abre PowerShell en la raíz del proyecto y ejecuta:
   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup-postgres.ps1
   ```
   Introduce tu **propia contraseña de `postgres`** y define las contraseñas de los dos nuevos roles mediante los mensajes interactivos. **NO** compartas esas contraseñas. El script **no borra** usuarios ni BD existentes y **no cambia** contraseñas existentes salvo que se solicite con `-ResetPasswords`.
   Si **ya tienes** `safelab_db` con propietario `safelab_migrator`, rol `safelab_app` y permisos de conexión/uso de schema (como en la instalación anterior), **omite este paso**.
3. En **IntelliJ → Run → Edit Configurations → Environment variables**, coloca (con tus propios secretos):
   ```text
   DB_URL=jdbc:postgresql://localhost:5432/safelab_db
   DB_USER=safelab_app
   DB_PASSWORD=<contraseña de safelab_app>
   FLYWAY_USER=safelab_migrator
   FLYWAY_PASSWORD=<contraseña de safelab_migrator>
   ```
   Asegúrate de que **Active profiles** sea `postgres` o déjalo vacío (es el perfil por defecto). JDK recomendado: **21**.
4. Ejecuta `mvn clean test` y después inicia `SafeLabPlatformApiApplication` con ▶. Flyway ejecuta **V1–V4 una sola vez**; Hibernate solo valida el esquema (`ddl-auto=validate`).
5. La BD estará vacía (por seguridad); para cargar **opcionalmente** las 3 empresas y 19 cuentas demo, ejecuta **una sola vez**:
   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\seed-demo.ps1
   ```
   Introduce la contraseña de `safelab_migrator` cuando `psql` la solicite. El archivo semilla es idempotente (`ON CONFLICT DO NOTHING`), pero **no debe ejecutarse en producción**.
6. Abre [Swagger](http://localhost:8080/swagger-ui.html). Prueba `POST /api/v1/auth/login` con la cuenta de demo `admin@safelab.test` y la contraseña de prueba documentada en `docs/LOCAL_SETUP.md`. **Sin seed demo no hay cuenta inicial**, por lo que la API estará activa pero el login requerirá registrar/activar cuentas por un flujo autorizado.

## Inicio rápido — JSON Server

```powershell
npm install
npm run check:data
npm run server
```

En la terminal de IntelliJ selecciona `jsonserver` como Spring Profile y ▶; Swagger estará en [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html). El archivo **`data/db.seed.json` nunca se modifica**: JSON Server escribe en `data/runtime/db.json`. Las operaciones POST/PATCH/DELETE **persisten** en ese archivo y sobreviven a recargas y reinicios. Solo `npm run reset:data` restaura la semilla original (detén JSON Server antes).

## Cómo trabajar en equipo

- Rama: `feature/av2-backend-postgresql`, desde `develop`.
- **Nunca** subir contraseñas, `.env` privados, `data/runtime/db.json`, carpetas `target/` y `node_modules/`.
- Cambios de esquema: crear nueva migración `V4__...sql` (no alterar V1, V2, V3 después del primer uso).
- Con datos reales: **no ejecutar `seed-demo.ps1`**. PostgreSQL conserva los datos entre reinicios. Flyway solo aplica migraciones pendientes.
- `postgres` es administrador de la instalación; `safelab_migrator` posee el esquema y ejecuta Flyway; `safelab_app` solo efectúa operaciones DML autorizadas.

## Documentación

- [Guía paso a paso para nuevos colaboradores](docs/LOCAL_SETUP.md)
- [Seguridad SQL, RLS y limitaciones](docs/DATABASE_SECURITY.md)
- [Plan de pruebas en IntelliJ, pgAdmin y Swagger](docs/TEST_PLAN.md)
- [Modos de ejecución y Swagger](docs/API_MODES.md)
- [Contratos REST](docs/api-contracts.md)

## Alcance y límites

Este ZIP es para **prueba local v2.0.0**, no producción. El control IAM/RBAC de la API se mantiene; en PostgreSQL se han añadido FK compuestas, índices, unicidad, restricciones, cupos transaccionales y RLS basada en un contexto **que proporciona la propia aplicación**. La clave de `safelab_app` debe permanecer privada: no entregar acceso SQL directo a usuarios finales. Las funciones de pagos, correos reales, control físico de actuadores, gestión de secretos y despliegue quedan fuera de esta etapa.
