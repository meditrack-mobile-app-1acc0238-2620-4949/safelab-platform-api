# Pruebas sugeridas en Swagger (JSON Server v1)

1. Abre `http://localhost:8080/api/v1/health` y confirma `UP`.
2. `POST /api/v1/auth/login` con `admin@safelab.test` y `SafeLab2026%` → 200. Copia `accessToken` y abre **Authorize**.
3. `GET /api/v1/platform/overview` → tres empresas, 19 cuentas (sin añadir nuevos registros previamente).
4. `GET /api/v1/platform/users` → 19 cuentas y **ninguna propiedad `password`**.
5. Abre otra sesión con `gerente.andes@safelab.test`, usa su token y prueba `GET /api/v1/identityaccess/organizations/org-andes/users` → únicamente empleados de Andinos.
6. Con el mismo token solicita empleados `org-gabriel` → **403 ACCESS_DENIED**.
7. `GET /api/v1/subscriptionbilling/current` → solo la suscripción de Andinos.
8. Como gerente, prueba `POST /api/v1/identityaccess/organizations/org-andes/invitations` con `{ "email":"nuevo@empresa.example", "role":"labTechnician", "facilityIds":["fac-central"], "resourceIds":[] }` → 201, código de prueba y vencimiento.
9. `PUT /api/v1/identityaccess/organizations/org-andes/users/00000000-0000-4000-8000-000000000101/permissions` con `{ "permissions":["dashboard.view","sensors.view"] }` → 200. En modo v1 el cambio se almacena en `permissionAssignments`.
10. Inicia sesión con `lab-technician2.andes@safelab.test` (si figura en `data/db.json`). Comprueba que no pueda entrar a `/api/v1/platform/overview` → **403** y que los sensores mostrados respeten sus recursos.
11. Prueba un body inválido en un DTO anotado con `@Valid`: debe devolver **400 VALIDATION_ERROR** con `fieldErrors`.
12. Cierra JSON Server y consulta `/api/v1/health` → **503 DATA_SOURCE_UNAVAILABLE**.

**Atención:** ejecutar POST/PATCH/DELETE modifica `data/db.json` del JSON Server. Antes de taggear el repositorio, restaura el fixture original con `git restore data/db.json` si solo estabas haciendo pruebas.

## Verificaciones de internacionalización (antes del tag v1.0.0)

13. En Swagger abre el selector del documento, arriba. Cambia a **English**: los títulos de los BC y resúmenes de endpoints deben mostrarse en inglés.
14. Vuelve a **Español**: comprueba que los mismos endpoints existan en español; no cambian los paths.
15. En `GET /api/v1/platform/users` (sin login), usa `Accept-Language: en`: la respuesta debe ser `401`, código `AUTH_REQUIRED` y mensaje en inglés. Repite con `es` y comprueba el mensaje en español.
16. En el mismo endpoint, autentícate como `gerente.andes@safelab.test` y consulta `/api/v1/platform/users`: debe mostrar `403 ACCESS_DENIED` y ahora **documentado en Responses**, no `Undocumented`.
17. En ambos idiomas, comprueba que aparecen `400`/`401`/`403` según el endpoint y el modelo `ApiError`.
18. Ejecuta `mvn test` para validar ambos documentos OpenAPI y los dos idiomas de error.

**Importante:** los controles internos de Swagger UI (`Try it out`, `Execute`) son de la interfaz de terceros y permanecen en inglés.
