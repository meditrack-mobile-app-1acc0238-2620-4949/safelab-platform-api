# SafeLab Platform API — backend-v1.0.0

Scope: JSON Server, Spring Boot HTTP endpoints for 12 BC, DTOs, multi-tenant mock access, Swagger UI.

No relational DB, no deployment, no Web/Mobile API integration. The two reference frontend fixtures were compared before copying IDs and relationships. Only `permissionAssignments: []` was added for explicit saved RBAC permissions in the backend mock adapter.

Publish this tag only after the local Maven build, Node JSON Server startup, Swagger smoke tests and isolation checks pass.

## Revisión de documentación ES/EN (antes de publicar el mismo tag)

- Selector `Español` / `English` en Swagger UI mediante grupos `/v3/api-docs/es` y `/v3/api-docs/en`.
- 66 operaciones, 14 grupos Swagger (12 BC y salud/plataforma), traducidos sin duplicar las rutas.
- Descripción de parámetros, DTOs y respuestas de error por idioma.
- `Accept-Language: es|en` cambia el campo `message` del JSON de error sin cambiar el `code`, `status` ni `path`.
- Documentación explícita de errores HTTP y del modelo `ApiError`.
- No se modifica el fixture JSON, el backend de datos ni los permisos de aplicación.
- **Sigue siendo backend-v1.0.0**: etiquetar solo tras ejecutar las pruebas locales.
