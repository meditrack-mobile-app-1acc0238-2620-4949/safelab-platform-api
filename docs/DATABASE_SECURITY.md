# Seguridad SQL v2.0.0

## Cuentas técnicas (tres en total)

| Rol | Permisos | Uso |
|---|---|---|
| `postgres` | Administración general | Instalación y configuración inicial, nunca para consultas normales de la API |
| `safelab_migrator` | Propietario de `safelab_db`, Flyway/DDL | Crear tablas, índices, constraints, políticas y migraciones |
| `safelab_app` | CONNECT, USAGE, DML sobre 20 tablas + sesiones; sin privilegios en flyway_schema_history ni CREATE/ALTER/DROP | Conexión de Hikari para operaciones REST |

Los usuarios registrados en IAM **NO** son roles de PostgreSQL. No se entrega la contraseña de `safelab_app` a clientes, auditores ni navegadores.

## Validaciones del esquema

- `PRIMARY KEY` de cada entidad; `users.id` es UUID textual de 36 caracteres, `users.username` son **18 dígitos** no editables y únicos. Son campos distintos.
- Unicidad case-insensitive del correo y de los RUC/invitaciones/suscripciones; `email`, `username`, `role`, `password` obligatorios.
- Contraseñas demo y nuevas cuentas PostgreSQL: hash BCrypt (no contraseña en texto plano). `auth_sessions` guarda hashes SHA-256 de tokens aleatorios.
- Relación FK `(organization_id, referencia)` para recursos de la misma empresa, además de FK de organización; impide asociar sensores/equipos/alertas de clientes distintos.
- Validaciones de rangos, montos, estados y cupos; una función SQL de cupos comprueba usuarios e invitaciones pendientes al hacer COMMIT, bloqueando la suscripción para evitar carreras.
- Las migraciones son **V1 (tablas), V2 (restricciones), V3 (RLS y GRANT)**. Ninguna inserta automáticamente empresas o empleados ficticios.

## RLS — defensa en profundidad

Todas las tablas tenant y `organizations` usan políticas RLS para `safelab_app`: sin contexto SQL ninguna fila es visible; con una empresa autenticada solo se ven sus filas; el administrador de plataforma puede visualizar globalmente. El repositorio PostgreSQL fija los valores `safelab.tenant_id` y `safelab.platform_access` con `set_config(..., true)` **dentro de cada transacción**, evitando persistirlos entre conexiones del pool. La autorización inicial se valida mediante el filtro de la API.

**Límite de seguridad fundamental:** estas variables personalizadas pueden modificarse desde una conexión SQL que conozca la contraseña de `safelab_app`; por ello **RLS no es una barrera frente a alguien que obtuvo las credenciales técnicas**. El mecanismo no sustituye RBAC de aplicación, privilegios mínimos, conexión privada al servidor, protección de secretos y auditoría. Por eso `safelab_app` es exclusiva del backend y no se habilita acceso SQL directo a clientes.

## Reglas operativas

- No ejecutar el SQL demo en bases con información real.
- Evitar `DROP TABLE`, `TRUNCATE`, `flyway clean`, contraseñas embebidas y migraciones reeditadas.
- Copias de seguridad y rotación de secretos serán requisitos de v3.0.0.
- Antes de producción deberán auditarse escenarios de concurrencia, políticas RLS, disponibilidad del correo y flujos de activación/pago; no se afirma certificación productiva en esta entrega.
