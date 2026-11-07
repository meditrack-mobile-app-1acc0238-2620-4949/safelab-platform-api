# SafeLab REST API — v1.0.0

Base URL: `http://localhost:8080/api/v1` — JSON Server: `http://127.0.0.1:3000` (internal local data source).

## HTTP conventions

- All private operations: `Authorization: Bearer <accessToken>` from `POST /auth/login`.
- `GET`: **200 OK** (list or detail). `POST`: **201 Created** for new records, **200 OK** for simulated actions. `PATCH`/`PUT`: **200 OK**. `DELETE`: **204 No Content**.
- Errors: **400** invalid JSON/fields, **401** unauthenticated, **403** forbidden (role/tenant/resource), **404** absent, **409** business conflict, **503** JSON Server unavailable, **500** unexpected.
- Errors always use `{timestamp,status,code,message,path,fieldErrors}`. A `fieldErrors` map contains bean-validation messages.
- IDs and organization scopes are derived from the logged-in user. Never trust client-supplied `organizationId` for company users.
- Mock identity: **ephemeral tokens** in memory, 1 hour; test passwords are present in local `data/db.json` only. Do not deploy this mode.

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

## Intentional v1 limits

- JSON Server has **no relational constraints or multi-request transactions** (including the appended audit record). Validate payloads in Spring; scenarios requiring atomic seat reservation, payment approval, unique identifiers and permission changes will be revisited in v2/PostgreSQL.
- Registration produces **pending** users; there are no real emails, domain ownership verification or payment processing.
- Command execution and report generation are simulated; no IoT actuation, PDF file or email is sent.
- Swagger endpoints expose test operations only; JSON Server must stay bound to `127.0.0.1`.
- `permissionAssignments` is the v1 array for saved effective permissions; the original `permissionOverrides` object stays in the seed for compatibility with prior frontend mocks.


## Documentación OpenAPI en dos idiomas

- `GET /v3/api-docs/es`: descripción en español.
- `GET /v3/api-docs/en`: English documentation.
- El selector Swagger usa estos dos documentos, sin cambiar las rutas reales.
- Todas las respuestas de error conservan el contrato `{timestamp,status,code,message,path,fieldErrors}`.
- Para el idioma de `message` envía `Accept-Language: en` o `Accept-Language: es`.
- Los códigos HTTP exitosos documentan `200`, `201` o `204` según la operación.
- `400`, `401`, `403`, `404`, `409`, `500`, `502`, `503` se documentan donde correspondan.
- Los componentes de Swagger UI (`Try it out`, `Execute`) mantienen sus textos nativos.
