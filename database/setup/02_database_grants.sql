-- Ejecutar como postgres conectado a safelab_db, DESPUES de 01.
-- No cambiar silenciosamente propietarios de bases existentes.
DO $$ BEGIN
  IF (SELECT pg_get_userbyid(datdba) FROM pg_database WHERE datname=current_database()) <> 'safelab_migrator' THEN
    RAISE EXCEPTION 'safelab_db debe pertenecer a safelab_migrator. Revisa el owner en pgAdmin.';
  END IF;
END $$;
GRANT CONNECT ON DATABASE safelab_db TO safelab_app;
GRANT USAGE ON SCHEMA public TO safelab_app;
GRANT USAGE, CREATE ON SCHEMA public TO safelab_migrator;
REVOKE CREATE ON SCHEMA public FROM safelab_app;
-- ACL de tablas e indices se otorga en la migracion Flyway V3.
