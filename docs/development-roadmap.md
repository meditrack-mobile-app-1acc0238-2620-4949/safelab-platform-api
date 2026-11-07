> **Actualización v2.0.0:** PostgreSQL es el perfil predeterminado y JSON Server se conserva opcionalmente. Consulta [LOCAL_SETUP](LOCAL_SETUP.md).

# Backend evolution

| Tag | Scope | Database | Frontend integration | Deployment |
|---|---|---|---|---|
| `backend-v1.0.0` | Spring Boot REST, Swagger, validation, multi-tenant mock tokens, JSON Server | JSON files behind JSON Server | No | No |
| `backend-v2.0.0` | JSON Server opcional + PostgreSQL local, Flyway, ACL SQL, constraints, RLS y BCrypt | PostgreSQL / JSON Server | No | No |
| `backend-v2.5.0` | Update Vue and Android repositories to consume same API; integration tests | PostgreSQL | Web + Mobile | Local |
| `backend-v3.0.0` | Security hardening, production observability, Docker Compose, deployment | PostgreSQL | Yes | Yes |

Use GitFlow: feature -> develop -> main -> annotated tag after verification. Tags hold code, not local data changes or secrets.
