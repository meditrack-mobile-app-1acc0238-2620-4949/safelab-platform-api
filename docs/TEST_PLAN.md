# Plan de aceptación v2 — antes del tag

## Comprobaciones previas

1. `npm run check:data` (semilla JSON).
2. `mvn clean test` (pruebas unitarias y Swagger ES/EN). Las pruebas de integración PostgreSQL se ejecutan aparte con `SAFELAB_TEST_POSTGRES=true` porque requieren BD local con seed.
3. Inicializa PostgreSQL local con `setup-postgres.ps1`, arranca Spring y observa en logs Flyway V1 / V2 / V3.
4. Ejecuta `seed-demo.ps1` solo en una BD local de prácticas.

## En pgAdmin, conectado como postgres

```sql
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
SELECT (SELECT count(*) FROM organizations) organizations,
       (SELECT count(*) FROM users) users,
       (SELECT count(*) FROM sensors) sensors,
       (SELECT count(*) FROM assets) assets;
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb FROM pg_roles WHERE rolname IN ('safelab_migrator','safelab_app');
SELECT tablename, rowsecurity FROM pg_tables WHERE schemaname='public' AND tablename IN ('users','sensors','incidents');
```

Esperado con seed demo: tres migraciones exitosas, 3 empresas, 19 usuarios, 12 sensores, 10 equipos; `safelab_app` sin superusuario ni createdb y RLS activado.

## En Swagger POSTGRESQL 8080

1. `POST /api/v1/auth/login` con `admin@safelab.test` y `SafeLab2026%`: HTTP 200, `accessToken`, sin contraseña en la respuesta.
2. Autoriza el token de administrador y consulta `GET /api/v1/platform/users`: 200 y 19 cuentas.
3. Inicia sesión como `gerente.andes@safelab.test`, reemplaza el token; `/api/v1/platform/users` → 403.
4. Como gerente Andinos: `/api/v1/identityaccess/organizations/org-andes/users` → 200 y solo esa empresa; `/api/v1/identityaccess/organizations/org-gabriel/users` → 403.
5. `GET /api/v1/sensormonitoring/sensors` → solo los sensores de su organización.
6. Verifica GET, POST, PATCH y DELETE con **datos de prueba**. Una restricción `UNIQUE` debe dar 409 ante duplicados; una FK del mismo tenant debe impedir asociar registros de empresas distintas.
7. Cambia el selector de Swagger `es`/`en`: mismo orden de BC, 66 operaciones, respuestas de error documentadas.
8. Reinicia Spring y repite GET: las modificaciones anteriores persisten.

## Verificación opcional del modo JSON 8081

Arranca Node + Spring `jsonserver` y repite login. Escribe un registro de prueba y verifica que aparezca en `data/runtime/db.json`; `npm run reset:data` revierte **solo la copia JSON**, nunca PostgreSQL.

## Verificar RLS sin poner en riesgo la base

Desde una conexión temporal como `safelab_app` en **psql** (solo para la prueba local):

```sql
-- Sin contexto de empresa: RLS no debe entregar filas.
SELECT count(*) FROM sensors;
BEGIN;
SELECT set_config('safelab.tenant_id', 'org-andes', true);
SELECT set_config('safelab.platform_access', 'false', true);
SELECT DISTINCT organization_id FROM sensors; -- solo org-andes
ROLLBACK;
```

Después, `SELECT * FROM flyway_schema_history;` debe responder **permission denied** para `safelab_app`. No compartas esta contraseña SQL con ningún usuario final. Una persona que la conozca puede alterar el contexto personalizado de RLS: esta barrera se apoya en el backend como único cliente SQL.

**La revisión automática de SQL/archivos no reemplaza estas pruebas manuales.** No crear el tag hasta ver el resultado de Maven y de las pruebas reales en PostgreSQL 18.
