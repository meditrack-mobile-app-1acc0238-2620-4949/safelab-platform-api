-- Ejecutar SOLO con psql, desde la base postgres y como postgres (rol admin).
-- Los roles y la base EXISTENTES se conservan; no se cambian contrasenas.
-- psql permite \gexec; pgAdmin Query Tool NO soporta este comando.
SELECT 'CREATE ROLE safelab_migrator LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'safelab_migrator') \gexec
SELECT 'CREATE ROLE safelab_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'safelab_app') \gexec
SELECT 'CREATE DATABASE safelab_db OWNER safelab_migrator'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'safelab_db') \gexec
