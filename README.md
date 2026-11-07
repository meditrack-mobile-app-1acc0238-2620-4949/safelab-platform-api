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
4. Ejecuta `mvn clean test` y después inicia `SafeLabPlatformApiApplication` con ▶. Flyway ejecuta **V1, V2 y V3 una sola vez**; Hibernate solo valida el esquema (`ddl-auto=validate`).
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
