# Cómo elegir entre JSON Server y PostgreSQL

## PostgreSQL (por defecto)

`spring.profiles.default=postgres`. Una sola instancia en `http://localhost:8080`. No requiere Node.js. PostgreSQL 18 se conecta con `DB_USER=safelab_app`; Flyway usa `FLYWAY_USER=safelab_migrator`. Swagger: `/swagger-ui.html`, selectores de documentos `es` y `en`.

## JSON Server (opcional)

Primero `npm install` y `npm run server`, después iniciar Spring con perfil `jsonserver` (`-Dspring-boot.run.profiles=jsonserver` si se usa Maven). Java en `http://localhost:8081`; JSON Server en `http://127.0.0.1:3000`. Los datos quedan en `data/runtime/db.json`, y no en el navegador. `npm run reset:data` restaura la copia demo. Este modo carece de transacciones SQL y RLS y es solo para pruebas.

## Selector de servidores en Swagger

Swagger ofrece documentos en dos idiomas y permite configurar destinos HTTP de la API. **Elegir otra URL no cambia la BD de un backend ya iniciado**. Para consultar ambos modos simultáneamente debes iniciar dos procesos Spring Boot, uno por perfil y puerto. No configuramos un servidor remoto ficticio en v2; un endpoint de Render real se añadirá durante el despliegue v3.
