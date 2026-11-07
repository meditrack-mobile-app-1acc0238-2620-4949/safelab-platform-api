# SafeLab Platform API

**Backend académico de SafeLab**, sistema multiempresa para supervisión de cadena de frío clínica, sensores IoT, mantenimiento, alertas, cumplimiento ambiental y auditoría. Comparte los 12 Bounded Contexts y los IDs de datos simulados de **SafeLab Web** y **SafeLab Mobile**.

## Versión v1.0.0 — JSON Server

Esta versión **no utiliza PostgreSQL, MySQL, H2 ni Docker**. Spring Boot expone la API REST, valida requests y permisos, y consulta datos de un **JSON Server local**, independiente de los frontends. Web/Mobile **no están conectados** en esta etapa.

| Requisito | Versión |
|---|---|
| Java JDK | 21 |
| Maven | 3.9+ (o Maven integrado en IntelliJ IDEA) |
| Node.js / npm | Node 20+ |

### 1. Ejecutar JSON Server (terminal A)

```bash
npm install
npm run check:data
npm run server
```

JSON Server queda disponible exclusivamente en `http://127.0.0.1:3000`. Sus datos se leen/escriben en `data/db.json`. **Solo para desarrollo local: contiene claves de cuentas ficticias**.

### 2. Ejecutar Spring Boot (terminal B)

```bash
mvn spring-boot:run
```

Alternativamente, abre el proyecto Maven en **IntelliJ IDEA** y ejecuta `SafeLabPlatformApiApplication`.

- **Swagger UI:** <http://localhost:8080/swagger-ui.html>
- **OpenAPI JSON (Español):** <http://localhost:8080/v3/api-docs/es>
- **OpenAPI JSON (English):** <http://localhost:8080/v3/api-docs/en>
- **Health:** <http://localhost:8080/api/v1/health>


### 2.1 Swagger bilingüe (Español / English)

En la parte superior de **Swagger UI**, junto al título de la API, usa el selector
**es / en** (Español / English). Las dos opciones muestran **las mismas 66 operaciones** de los
12 Bounded Contexts; cambian únicamente la documentación (títulos, descripciones,
parámetros, DTOs y respuestas HTTP).

| Especificación | Ruta |
|---|---|
| Español (predeterminada) | `/v3/api-docs/es` |
| English | `/v3/api-docs/en` |

Los nombres JSON (`orgId`, `password`, `status`, etc.), las rutas, los roles y los
códigos de error (`ACCESS_DENIED`) **nunca se traducen**: son contratos técnicos.
El selector cambia el idioma del documento OpenAPI, **no** el idioma que la
API devuelve para cada solicitud. Para recibir errores en inglés, selecciona
`en` en el parámetro opcional **Accept-Language** de cada endpoint; para español
usa `es`. El valor por defecto de los mensajes sin un idioma compatible es ES.

**Swagger UI nativo:** botones como `Try it out`, `Execute`, `Authorize`,
`Responses` permanecen en inglés. Traducirlos requeriría personalizar
Swagger UI; no es parte de esta versión. Las explicaciones de cada endpoint
sí están disponibles en ambos idiomas.

**Respuestas HTTP:** los errores `400`, `401`, `403`, `404`, `409`, `500`, `502`
y `503` que correspondan a cada ruta se documentan con el mismo esquema
`ApiError` y un ejemplo de `403 ACCESS_DENIED` traducido. Las operaciones
creadas incluyen `201 Created`, y `logout`/`DELETE asset` documentan `204`.

**Mantenimiento de textos:** las traducciones de Swagger se encuentran en
`src/main/resources/i18n/openapi_es.properties` y `openapi_en.properties`;
las respuestas de error en `messages_es.properties` y `messages_en.properties`.
La clase `LocalizedOpenApiCustomizer` convierte estas claves en dos OpenAPI
válidos sin duplicar controladores.

**Verificación rápida:**

```powershell
# Después de ejecutar JSON Server y Spring Boot
Invoke-RestMethod http://localhost:8080/v3/api-docs/es | Select-Object -ExpandProperty info
Invoke-RestMethod http://localhost:8080/v3/api-docs/en | Select-Object -ExpandProperty info

# Error del endpoint protegido en inglés, sin bearer token
Invoke-RestMethod http://localhost:8080/api/v1/platform/users -Headers @{ "Accept-Language" = "en" }
# PowerShell mostrará 401; en el cuerpo aparecerá el mensaje en inglés.
```

### 3. Iniciar sesión en Swagger

En Swagger, abre `POST /api/v1/auth/login` y usa este body (cuentas de prueba exclusivamente):

```json
{
  "identifier": "admin@safelab.test",
  "password": "SafeLab2026%"
}
```

Copia el valor de `accessToken`, pulsa **Authorize** y pega el token. Luego utiliza los endpoints de los 12 BC. Para probar aislamiento, inicia otra sesión con un gerente, por ejemplo `gerente.andes@safelab.test` y la misma contraseña ficticia.

### 4. Ejecutar pruebas

```bash
npm run check:data
mvn test
```

La instalación de dependencias requiere conexión a npm y Maven Central. Cuando cierres JSON Server, las llamadas de la API devolverán `503 DATA_SOURCE_UNAVAILABLE`.

## Arquitectura sencilla (DDD)

```text
src/main/java/com/safelab/platform/
  identityaccess/        # login, registro, invitaciones, RBAC, plataforma
  userprofiles/          # perfil personal
  subscriptionbilling/   # suscripción y recibos (lectura)
  facilityassets/        # empresas, sedes y activos
  sensormonitoring/      # sensores y mediciones
  equipmentmaintenance/  # estado y mantenimiento
  alertsincidents/       # alertas e incidentes
  environmentalcompliance/ # reglas, evaluaciones y violaciones
  remoteactuation/      # comandos simulados
  dashboardinsights/     # KPIs
  reportinganalytics/    # reportes
  audittraceability/     # auditoría
  shared/                # cliente JSON Server, sesión simulada, errores, reglas comunes

data/
  db.json                # mismas colecciones e IDs de los dos frontends
  permission-catalog.json  # mismos 21 permisos
```

Los controladores son pequeños y usan servicios compartidos para practicar validaciones, acceso multiempresa y contratos REST. El acceso a JSON Server está encapsulado en `JsonServerGateway`: en **v2.0.0** se reemplazará por repositorios PostgreSQL, migraciones y seguridad real.

## Límites de seguridad (v1 únicamente)

- **No usar en producción:** las contraseñas ficticias siguen en el JSON local y el token es una sesión temporal en memoria.
- Una cuenta no activa no puede iniciar sesión. Las credenciales nunca se devuelven por la API pública.
- Los roles y `organizationId` se consultan en el servidor desde el token; no se aceptan encabezados arbitrarios que cambien de empresa.
- Las operaciones de registro, invitación, pago, IoT y correo son simuladas. No se envían emails ni se ejecutan cargos o comandos físicos.
- No expongas el puerto 3000 de JSON Server a Internet ni uses contraseñas reales en este fixture.

## Documentación

- [Contratos REST y códigos HTTP](docs/api-contracts.md)
- [Evolución v1 → v2 → v2.5 → v3](docs/development-roadmap.md)

**Stack:** Java 21 · Spring Boot 3.4.5 · Spring Web · Bean Validation · Springdoc OpenAPI · JSON Server 0.17.4.
