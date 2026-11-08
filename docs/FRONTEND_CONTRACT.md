# Contrato de integración v2.5

## Decisiones

- Mismo prefijo `/api/v1` y los mismos **12 nombres de BC** en Web y Mobile.
- Autenticación: `POST /api/v1/auth/login` → `{ accessToken, tokenType, expiresAt, user }`. En las operaciones privadas incluir `Authorization: Bearer <accessToken>`; `GET /api/v1/auth/me` y `POST /api/v1/auth/logout`.
- `GET /api/v1/integration/bootstrap`: un objeto de listas de los BC + `currentUser`. Autenticado, filtrado por tenant y permisos en el backend y sanitizado de contraseñas. **No es un endpoint genérico de escritura**.
- Escrituras: usar rutas existentes de los controladores específicos, p.ej. POST/PATCH `/facilityassets/assets` y POST `/sensormonitoring/sensors`, no escribir directamente sobre tablas ni JSON Server desde el frontend.
- `PATCH /api/v1/identityaccess/organizations/{orgId}/users/{userId}/assignments`: manager de la misma empresa; valida ids de recursos.
- `DELETE /api/v1/sensormonitoring/sensors/{id}`: permisos `sensors.configure`, FK puede rechazar borrado.
- HTTP `400/401/403/404/409/5xx`: mostrar respuesta `message/code/fieldErrors` y nunca confirmar una escritura fallida.

## Por qué se modificó el backend

La v2.0.0 ya permitía CRUD desde Swagger, pero no tenía una ruta de lectura agregada para hidratar los 12 BC de ambas apps, ni CORS para `localhost:5173`. Sin esos cambios, los frontends tendrían que realizar muchas peticiones distintas y podrían exponer datos cross-tenant si intentaran descargar todo. El endpoint agregado filtra los registros *antes de enviarlos*, y deja la autorización de escrituras en cada BC. No se cambiaron ni se recrean las tablas PostgreSQL; Flyway conserva V1/V2/V3 sin alteraciones.

## Pendientes antes de declarar integración total

Algunas pantallas heredadas tienen acciones para las que faltan endpoints en la API actual (evidencias/archivos, ajustes globales, notificaciones manuales, edición de correo del usuario, operaciones especializadas). Se requiere definir y probar esos contratos en una iteración posterior. Esto **no** es integración completa de todos los flujos operativos ni una release de producción.
