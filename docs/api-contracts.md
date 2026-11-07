> **Actualización v2.0.0:** el origen de datos es PostgreSQL (no JSON Server). Los contratos REST y los cuerpos de solicitud siguen siendo compatibles con la v1. Para configurar PostgreSQL revisa [README](../README.md) y [Pruebas PostgreSQL](postgresql-smoke-tests.md).

# SafeLab REST API — v2.0.0

Base URL PostgreSQL: `http://localhost:8080/api/v1`; en modo JSON Server: `http://localhost:8081/api/v1`.

## HTTP conventions

- All private operations: `Authorization: Bearer <accessToken>` from `POST /auth/login`.
- `GET`: **200 OK** (list or detail). `POST`: **201 Created** for new records, **200 OK** for simulated actions. `PATCH`/`PUT`: **200 OK**. `DELETE`: **204 No Content**.
- Errors: **400** invalid JSON/fields, **401** unauthenticated, **403** forbidden (role/tenant/resource), **404** absent, **409** business conflict, **503** fuente de datos temporalmente no disponible, **500** unexpected.
- Errors always use `{timestamp,status,code,message,path,fieldErrors}`. A `fieldErrors` map contains bean-validation messages.
- IDs and organization scopes are derived from the logged-in user. Never trust client-supplied `organizationId` for company users.
- Sesiones: **tokens opacos de 1 hora**; únicamente sus hashes SHA-256 se guardan en `auth_sessions`. Contraseñas BCrypt en PostgreSQL. No desplegar todavía sin protección de producción.

## API grouping — 12 Bounded Contexts

| BC | Prefix | Major operations |
|---|---|---|
| Identity & Access Management | `/auth`, `/identityaccess`, `/platform` | login, me, logout, company/invitation registration, roles, members, invitation, save permissions, platform overview |
| Facility & Asset Management | `/facilityassets` | organizations, facilities, assets GET/POST/PATCH/DELETE |
| Sensor & Telemetry Monitoring | `/sensormonitoring` | sensors GET/POST/PATCH, reading PATCH |
| Equipment Health & Maintenance | `/equipmentmaintenance` | equipment, records GET/POST/PATCH |
| Alert & Incident Management | `/alertsincidents` | alerts, notifications, incidents GET/POST, acknowledge, resolve |
| Operational Dashboard & Insights | `/dashboardinsights` | computed overview |
| Reporting & Analytics | `/reportinganalytics` | reports GET/POST, summary |
| Audit & Traceability | `/audittraceability` | logs, timeline |
| User Profile Management | `/userprofiles` | own profile GET/PATCH |
| Subscription & Billing | `/subscriptionbilling` | own subscription, invoices, platform subscriptions GET |
| Environmental Compliance Management | `/environmentalcompliance` | rules, evaluations, violations, rules GET/POST/PATCH |
| Remote Control & Actuation | `/remoteactuation` | actuators, commands, simulate command POST |

See Swagger UI for exact DTO schemas, required fields and interactive examples.

## Example error

```json
{
  "timestamp": "2026-11-07T08:30:00Z",
  "status": 403,
  "code": "ACCESS_DENIED",
  "message": "No tienes permisos para esta empresa o recurso.",
  "path": "/api/v1/sensormonitoring/sensors/sen-001",
  "fieldErrors": {}
}
```

## Persistencia en v2 y limitaciones académicas

- JSON Server se conserva como perfil `jsonserver`; por defecto las operaciones usan PostgreSQL/JPA.
- Flyway aplica V1 (esquema), V2 (restricciones) y V3 (RLS y GRANT) una sola vez; el seed ficticio es explícito y separado.
- `@Transactional` agrupa escrituras relacionadas, incluyendo el registro de auditoría.
- Los permisos RBAC siguen gestionados por el backend; los tokens de sesión se guardan hasheados en PostgreSQL.
- La suscripción, las invitaciones, el envío de emails, los pagos y los comandos físicos aún no son servicios de producción.
- Los identificadores de Web/Mobile se preservan para la integración prevista en v2.5.0.

## Documentación OpenAPI en dos idiomas

- `GET /v3/api-docs/es`: descripción en español.
- `GET /v3/api-docs/en`: English documentation.
- El selector Swagger usa estos dos documentos, sin cambiar las rutas reales.
- Todas las respuestas de error conservan el contrato `{timestamp,status,code,message,path,fieldErrors}`.
- Para el idioma de `message` envía `Accept-Language: en` o `Accept-Language: es`.
- Los códigos HTTP exitosos documentan `200`, `201` o `204` según la operación.
- `400`, `401`, `403`, `404`, `409`, `500`, `503` se documentan donde correspondan.
- Los componentes de Swagger UI (`Try it out`, `Execute`) mantienen sus textos nativos.
