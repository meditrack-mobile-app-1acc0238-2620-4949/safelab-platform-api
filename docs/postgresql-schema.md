# Modelo relacional de SafeLab v2.0.0

Las tablas se crean con Flyway y se comprueban con JPA `ddl-auto=validate`. **Los registros demo solo se insertan si se ejecuta el seed opcional**.

Cada colección del antiguo JSON Server corresponde a una tabla independiente con columnas SQL reales.
Las listas (`facilityIds`, `resourceIds`, `invoices`, `permissions`) utilizan JSONB porque son arreglos; los campos simples son columnas tipadas.
`extra_attributes` conserva solo campos adicionales no previstos y no sustituye a las columnas reales.

| Colección original | Tabla PostgreSQL | Entidad JPA | Registros iniciales |
|---|---|---|---|
| `organizations` | `organizations` | `identityaccess.domain.OrganizationRecord` | 3 |
| `users` | `users` | `identityaccess.domain.UserRecord` | 19 |
| `invitations` | `invitations` | `identityaccess.domain.InvitationRecord` | 0 |
| `permissionAssignments` | `permission_assignments` | `identityaccess.domain.PermissionAssignmentRecord` | 0 |
| `accountRequests` | `account_requests` | `identityaccess.domain.AccountRequestRecord` | 0 |
| `facilities` | `facilities` | `facilityassets.domain.FacilityRecord` | 4 |
| `assets` | `assets` | `facilityassets.domain.AssetRecord` | 10 |
| `sensors` | `sensors` | `sensormonitoring.domain.SensorRecord` | 12 |
| `maintenanceRecords` | `maintenance_records` | `equipmentmaintenance.domain.MaintenanceRecord` | 9 |
| `alerts` | `alerts` | `alertsincidents.domain.AlertRecord` | 5 |
| `notifications` | `notifications` | `alertsincidents.domain.NotificationRecord` | 3 |
| `incidents` | `incidents` | `alertsincidents.domain.IncidentRecord` | 4 |
| `reports` | `reports` | `reportinganalytics.domain.ReportRecord` | 3 |
| `auditLogs` | `audit_logs` | `audittraceability.domain.AuditLogRecord` | 4 |
| `subscriptions` | `subscriptions` | `subscriptionbilling.domain.SubscriptionRecord` | 3 |
| `complianceRules` | `compliance_rules` | `environmentalcompliance.domain.ComplianceRuleRecord` | 6 |
| `complianceEvaluations` | `compliance_evaluations` | `environmentalcompliance.domain.ComplianceEvaluationRecord` | 8 |
| `complianceViolations` | `compliance_violations` | `environmentalcompliance.domain.ComplianceViolationRecord` | 3 |
| `actuators` | `actuators` | `remoteactuation.domain.ActuatorRecord` | 7 |
| `remoteCommands` | `remote_commands` | `remoteactuation.domain.RemoteCommandRecord` | 3 |

## Notas de integridad

- `organization_id` referencia `organizations.id` en las tablas empresariales. El administrador global y las organizaciones no tienen empresa propietaria.
- Correos y usernames únicos, RUC único, una suscripción por organización y una asignación de permisos por usuario.
- `auth_sessions` almacena solo el SHA-256 del token, con fecha de expiración y usuario relacionado.
- El documento legacy `billing` del JSON era un duplicado del registro de Andinos de `subscriptions`; no se crea una tabla duplicada.
- `permissionOverrides` era un objeto vacío sin consultas en la API; la persistencia de permisos corresponde a `permission_assignments`.
- Los `id` de Web/Mobile se preservan sin cambios.

## Consultas rápidas de pgAdmin

```sql
SELECT COUNT(*) FROM organizations;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM sensors;
SELECT COUNT(*) FROM assets;
SELECT COUNT(*) FROM subscriptions;
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```
