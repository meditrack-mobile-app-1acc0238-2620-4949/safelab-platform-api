# SafeLab API AV2 v2.5.1 — Preparación para Neon y Render

> **Entorno de evaluación académica.** Estos archivos preparan el backend; no realizan un despliegue ni garantizan un funcionamiento validado en la nube. No publicar información clínica real ni datos personales sensibles.

## Qué se modificó

- `Dockerfile` y `.dockerignore`: compilación Maven con Java 21 y ejecución con JRE 21, sin ejecutar como root.
- `application.properties`: `CORS_ALLOWED_ORIGINS` configurable (orígenes exactos) y cabeceras de proxy.
- `LocalCorsConfig`: sin dominios fijos; el backend solo acepta los orígenes definidos en CORS.
- `OpenApiConfig`: URL de servidor relativa `/` para que Swagger use el dominio Render en lugar de `localhost`.
- Versión 2.5.1 en Maven, Swagger y health; `.gitignore` protege `.env`.
- **Se conservan idénticas** las migraciones Flyway V1, V2, V3, V4, las clases de negocio y endpoints.

## Fase A — Base de datos Neon

1. En https://console.neon.tech crea un proyecto, elige una región cercana y una base **nueva y vacía** (por ejemplo `safelab_db`). No copies obligatoriamente la base PostgreSQL local.
2. Copia los parámetros de conexión **directa** (no `-pooler`): host, base y el **rol owner** creado por Neon. Guarda su contraseña en privado.
3. Conéctate con el owner a la base nueva desde el SQL Editor o pgAdmin y ejecuta, **reemplazando la contraseña de muestra con una generada solo para Neon**:

```sql
CREATE ROLE safelab_app LOGIN PASSWORD 'REEMPLAZAR_CON_CONTRASENA_LARGA_ALEATORIA';
GRANT CONNECT ON DATABASE safelab_db TO safelab_app;
GRANT USAGE ON SCHEMA public TO safelab_app;
-- NO conceder neon_superuser ni BYPASSRLS a safelab_app.
```

   Si elegiste otro nombre para la base, reemplaza `safelab_db` en ese SQL. Para una base recién creada, este SQL se ejecuta una sola vez. **No ejecutes** `database/setup/01_roles_and_db.sql` ni `02_database_grants.sql` de la instalación local: asumen superusuario PostgreSQL y propietario `safelab_migrator`, y Neon no proporciona acceso a `postgres` superusuario.
4. Usa el rol owner de Neon **solo como usuario de migración Flyway** (`FLYWAY_USER`, `FLYWAY_PASSWORD`). Utiliza `safelab_app` exclusivamente como usuario de la aplicación. No agregues `safelab_app` desde la consola de Neon, porque los roles creados mediante la consola reciben privilegios elevados que podrían omitir RLS; créalo mediante SQL como arriba.
5. Comprueba que Neon ofrece la conexión directa con TLS. En Java la URL debe tener esta **forma** (sin contraseña incorporada):

```text
jdbc:postgresql://ep-tu-host.region.aws.neon.tech:5432/safelab_db?sslmode=require
```

   Es **un ejemplo**, no una dirección real. Sustituye host, región y nombre de base con los datos de Neon. Copia la cadena JDBC, no la URL PostgreSQL `postgresql://` sin convertir. Para Flyway se usa la misma conexión directa. `sslmode=require` cifra el tráfico; una configuración con verificación de identidad TLS puede requerir ajustes adicionales del truststore.
6. No ejecutes todavía el archivo `database/seed/demo_data.sql` en una base abierta públicamente: contiene cuentas con hashes BCrypt de contraseñas de prueba conocidas en el repositorio. Preparar primero el procedimiento de cuentas de demostración y rotación de contraseñas, y solo usar datos ficticios.

## Fase B — Render, después de probar la conexión Neon

1. En https://dashboard.render.com crea **New > Web Service** usando el repositorio GitHub `safelab-platform-api`, rama de release aprobada (habitualmente `main`).
2. Selecciona **Docker** como runtime. `Dockerfile` está en la raíz, sin subcarpetas; no se requiere comando manual de build/start porque Dockerfile los define.
3. Configura las variables en **Environment**, sin agregarlas a Git ni a ningún archivo público:

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `postgres` |
| `DB_URL` | `jdbc:postgresql://HOST:5432/BASE?sslmode=require` (URL DIRECTA) |
| `DB_USER` | `safelab_app` |
| `DB_PASSWORD` | contraseña propia del rol SQL `safelab_app` |
| `FLYWAY_USER` | rol **owner** de Neon |
| `FLYWAY_PASSWORD` | contraseña de owner de Neon |
| `CORS_ALLOWED_ORIGINS` | dominio HTTPS real del frontend, ej. `https://tu-web.onrender.com` |
| `PORT` | Render lo asigna, por defecto `10000`; no es obligatorio editarlo |

   Usa los valores que **Neon te proporcione**, no los textos de ejemplo. Sin el frontend desplegado puedes indicar el dominio que reservarás; después actualiza la variable con la URL definitiva y vuelve a desplegar.
4. Si seleccionas plan gratuito, ten presente que Java/Spring puede ser exigente en memoria y el servicio puede entrar en reposo. El despliegue debe evaluarse con logs y pruebas, no darlo por aprobado por mostrar "Live".
5. Configura **Health Check Path** como `/api/v1/health`. Este endpoint devuelve UP solo si puede acceder a la tabla `organizations`.
6. Revisa **Logs**: migraciones Flyway V1–V4 en base nueva, inicialización de Hikari/JPA y `Tomcat started on port ...` sin excepciones. No incluyas contraseñas en capturas o reportes.
7. Con el dominio real asignado prueba:

```text
https://TU-BACKEND.onrender.com/api/v1/health
https://TU-BACKEND.onrender.com/swagger-ui.html
https://TU-BACKEND.onrender.com/v3/api-docs/es
```

   Para confirmar que la base está migrada, ejecuta en **Neon SQL Editor**:

```sql
SELECT installed_rank, version, description, success
FROM public.flyway_schema_history
ORDER BY installed_rank;
SELECT count(*) AS organizations FROM public.organizations;
```

   Resultado esperado inicial: cuatro migraciones exitosas; `organizations=0` es normal si NO hay semilla. No confundas tener el esquema y Swagger levantados con tener cuentas para login.

## Fase C — Enlaces a frontend y Android (más adelante)

- Frontend Vite: `VITE_API_BASE_URL=https://TU-BACKEND.onrender.com/api/v1`. Esa URL queda compilada en el JS: no colocar secretos `VITE_`.
- Backend: `CORS_ALLOWED_ORIGINS=https://TU-FRONTEND.onrender.com` (varios separados por comas, sin barra final; jamás `*` si no se necesita).
- Android Kotlin: cambiar `BuildConfig.API_BASE_URL` de `https://example.invalid/api/v1` por el backend HTTPS y distribuir un APK firmado mediante Firebase App Distribution. No usar `10.0.2.2` en Android físico.
- La Landing Page requiere sus propios enlaces públicos, no credenciales PostgreSQL.

## Controles previos a publicar

- `mvn test` en tu equipo con Maven y JDK 21, y `mvn -DskipTests package` para validar el empaquetado.
- Confirmar que `safelab_app` **no tiene** membresía `neon_superuser`, `BYPASSRLS`, ni acceso DDL; Flyway usa el owner solamente durante el arranque.
- Revisar límites de seguridad de la versión académica: alta pública, verificación de correo y pagos simulados, falta de ratelimit para login, cuentas demo conocidas; no usar como producción real.
- Crear datos de demostración con contraseñas nuevas antes de compartir acceso. No enviar credenciales por el chat, el repositorio o el frontend.
- Mantener Neon y Render en regiones próximas, para evitar latencia de una consulta SQL hecha entre regiones.

## Rollback

Si falla el build de Render, corregir configuración o revertir el commit del Dockerfile/CORS. Si falla Flyway, leer el error y **no** ejecutar `flyway clean`, `DROP`, `TRUNCATE` ni modificar migraciones V1–V4 aplicadas. Tomar copia antes de cambios manuales en bases ya pobladas.
