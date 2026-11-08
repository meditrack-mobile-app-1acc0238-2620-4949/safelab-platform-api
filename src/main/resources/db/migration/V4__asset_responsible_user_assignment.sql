-- SafeLab v2.5.1: vincular el responsable del activo a un usuario real.
-- V1-V3 son inmutables. Esta migracion se aplica una vez a safelab_db existente.
ALTER TABLE public.assets ADD COLUMN responsible_user_id text;
CREATE INDEX ix_assets_responsible_user ON public.assets(responsible_user_id);

-- Rechazar referencias cruzadas entre empresas (el FK incluye organization_id).
ALTER TABLE public.users ADD CONSTRAINT ux_users_id_organization UNIQUE (id, organization_id);
ALTER TABLE public.assets ADD CONSTRAINT fk_assets_responsible_same_organization
  FOREIGN KEY (responsible_user_id, organization_id)
  REFERENCES public.users(id, organization_id);

-- Datos antiguos: resolver por nombre SOLO si hay coincidencia unica dentro de
-- la empresa y el empleado esta activo y autorizado para la sede del activo.
-- Evitamos adjudicar recursos a homonimos y a cuentas de otras empresas.
WITH valid_matches AS (
  SELECT a.id AS asset_id, min(u.id) AS user_id
  FROM public.assets a
  JOIN public.users u ON u.organization_id = a.organization_id
    AND lower(btrim(u.full_name)) = lower(btrim(a.responsible))
    AND u.status = 'active'
    AND u.role NOT IN ('organizationAdmin', 'platformAdmin')
    AND (u.facility_ids IS NULL OR u.facility_ids = '[]'::jsonb OR u.facility_ids ? a.facility_id)
  WHERE a.responsible IS NOT NULL AND btrim(a.responsible) <> ''
  GROUP BY a.id
  HAVING count(*) = 1
)
UPDATE public.assets a
SET responsible_user_id = m.user_id
FROM valid_matches m
WHERE a.id = m.asset_id;

-- En v2.5.0, responsible era texto mientras que el acceso dependia de resource_ids.
-- Sincronizamos los enlaces migrados con el alcance autorizado del responsable.
-- No eliminamos permisos manuales existentes ni duplicamos IDs.
UPDATE public.users u
SET resource_ids = COALESCE(u.resource_ids, '[]'::jsonb) || (
    SELECT COALESCE(jsonb_agg(a.id), '[]'::jsonb)
    FROM public.assets a
    WHERE a.responsible_user_id = u.id
      AND NOT (COALESCE(u.resource_ids, '[]'::jsonb) ? a.id)
)
WHERE EXISTS (
    SELECT 1 FROM public.assets a
    WHERE a.responsible_user_id = u.id
      AND NOT (COALESCE(u.resource_ids, '[]'::jsonb) ? a.id)
);
