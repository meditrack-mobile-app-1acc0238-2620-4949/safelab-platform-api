# Instalación local para un nuevo colaborador — v2.0.0

## Historia paso a paso

**Soy una persona que acaba de unirse a SafeLab. Quiero ejecutar el backend sin depender de la computadora de otro integrante.**

1. **Instalo PostgreSQL 18.** El instalador me solicita la contraseña de `postgres`. Es la contraseña del **administrador de mi propio servidor de bases de datos**, no la contraseña de ningún usuario de SafeLab. La elijo yo y la guardo de manera privada. PostgreSQL funciona como servicio de Windows. Si ya lo tengo, no necesito reinstalarlo.
2. **Descargo el repositorio.** Abro la carpeta `safelab-platform-api` en IntelliJ IDEA y dejo que Maven importe las dependencias. Selecciono JDK 21.
3. **Preparo mi PostgreSQL solo una vez.** Desde la carpeta del proyecto ejecuto:
   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup-postgres.ps1
   ```
   El asistente me solicita mi contraseña de `postgres`. Si faltan, crea `safelab_migrator`, `safelab_app` y `safelab_db`; después solicita una contraseña **diferente** para cada cuenta SQL nueva. La primera cuenta es para **crear/actualizar tablas** con Flyway. La segunda es para que **Spring Boot lea y escriba datos**. No creo tablas manualmente en pgAdmin.
4. **Configuro IntelliJ.** En la configuración de ejecución de la clase `SafeLabPlatformApiApplication` introduzco `DB_PASSWORD` y `FLYWAY_PASSWORD`. Cada una corresponde al usuario indicado arriba. `DB_URL` apunta a `jdbc:postgresql://localhost:5432/safelab_db`; `DB_USER=safelab_app` y `FLYWAY_USER=safelab_migrator` ya son los valores predeterminados. Mi configuración personal NO va a GitHub.
5. **Inicio el backend.** Dejo activo el perfil `postgres` y pulso ▶. Flyway crea las tablas automáticamente **solo si no existen según el historial**. Un siguiente inicio NO recrea ni vacía las tablas.
6. **Decido si quiero datos de demostración.** Si necesito las 3 empresas y cuentas ficticias del curso, ejecuto una sola vez:
   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\seed-demo.ps1
   ```
   Escribo `DEMO` para confirmar. psql me pedirá la contraseña local de `safelab_migrator`. Si quiero comenzar con una base vacía, **no ejecuto** el comando; no habrá usuarios de demo.
7. **Abro Swagger.** Navego a `http://localhost:8080/swagger-ui.html`, selecciono `es` o `en` y consulto `/api/v1/health`. Si cargué los datos demo, puedo iniciar sesión:
   ```json
   {"identifier":"admin@safelab.test","password":"SafeLab2026%"}
   ```
   Esta es **únicamente la contraseña de cuentas ficticias**, no la contraseña de PostgreSQL. Utilizo el `accessToken` devuelto en **Authorize**.
8. **Al día siguiente.** Simplemente compruebo que PostgreSQL esté iniciado y vuelvo a pulsar ▶. Mis datos siguen guardados. No vuelvo a configurar roles ni a importar el seed.

### Si ya tengo la configuración hecha en pgAdmin

Si en pgAdmin veo la BD `safelab_db` y los roles `safelab_migrator` y `safelab_app`, y ya ejecuté `GRANT CONNECT` + `GRANT USAGE`, **no necesito ejecutar setup-postgres.ps1**. Configuro mis contraseñas en IntelliJ, inicio Spring Boot y, si quiero, cargo datos demo.

### Si necesito otra base o puerto

En `DB_URL` indico el host, puerto y BD deseados. Para una instalación personalizada distinta de `safelab_db`, primero ajusta y revisa los scripts de bootstrap. Los nombres predeterminados de v2 son fijos para facilitar las pruebas de clase.

### Si prefiero JSON Server

No instalo PostgreSQL. Instalo Node.js, ejecuto `npm install` y `npm run server`; inicio IntelliJ con **Active profiles = jsonserver**. Swagger es `http://localhost:8081/swagger-ui.html`. La semilla `data/db.seed.json` es inmutable. Los cambios de API se guardan en `data/runtime/db.json`, no en localStorage del navegador.

### Errores frecuentes

- **Port 8080 already in use:** cierra la otra instancia o cambia `PORT`.
- **password authentication failed for user:** revisa qué contraseña corresponde a `safelab_app` y cuál a `safelab_migrator`.
- **permission denied for schema public:** falta la configuración inicial y/o el propietario de `safelab_db` no es `safelab_migrator`.
- **Flyway history / checksum:** no edites una migración aplicada; crea una nueva versión `V4__...sql`.
- **Login inválido en PostgreSQL:** los datos demo son opcionales; ejecuta el seed únicamente en tu base local de prácticas.
- **JSON Server unavailable:** inicia `npm run server` en una terminal separada y usa el perfil `jsonserver`.
