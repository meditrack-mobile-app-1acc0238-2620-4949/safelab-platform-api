-- SafeLab v2.5.0 RC3: SOLO LECTURA, compatible con PostgreSQL V4.
-- Ejecutar en Query Tool de pgAdmin contra safelab_db DESPUÉS de iniciar el backend.
-- Cada fila anómala representa una relación que debes revisar; lo esperado es cero.

SELECT 'asset_facility_cross_tenant_or_missing' AS check_name, count(*) AS issues
FROM public.assets a LEFT JOIN public.facilities f ON f.id = a.facility_id
WHERE f.id IS NULL OR a.organization_id <> f.organization_id
UNION ALL
SELECT 'asset_responsible_cross_tenant_or_missing', count(*)
FROM public.assets a LEFT JOIN public.users u ON u.id = a.responsible_user_id
WHERE a.responsible_user_id IS NOT NULL
  AND (u.id IS NULL OR u.organization_id <> a.organization_id)
UNION ALL
SELECT 'asset_responsible_missing_resource_grant', count(*)
FROM public.assets a JOIN public.users u ON u.id = a.responsible_user_id
WHERE NOT (COALESCE(u.resource_ids, '[]'::jsonb) ? a.id)
UNION ALL
SELECT 'asset_responsible_facility_mismatch', count(*)
FROM public.assets a JOIN public.users u ON u.id = a.responsible_user_id
WHERE u.facility_ids IS NOT NULL AND u.facility_ids <> '[]'::jsonb
  AND NOT (u.facility_ids ? a.facility_id)
UNION ALL
SELECT 'sensor_asset_cross_tenant_or_missing', count(*)
FROM public.sensors s LEFT JOIN public.assets a ON a.id = s.asset_id
WHERE s.asset_id IS NOT NULL
  AND (a.id IS NULL OR a.organization_id <> s.organization_id)
UNION ALL
SELECT 'sensor_facility_cross_tenant_or_missing', count(*)
FROM public.sensors s LEFT JOIN public.facilities f ON f.id = s.facility_id
WHERE f.id IS NULL OR f.organization_id <> s.organization_id
UNION ALL
SELECT 'maintenance_asset_cross_tenant_or_missing', count(*)
FROM public.maintenance_records m LEFT JOIN public.assets a ON a.id = m.equipment_id
WHERE m.equipment_id IS NOT NULL
  AND (a.id IS NULL OR a.organization_id <> m.organization_id)
UNION ALL
SELECT 'alert_sensor_cross_tenant_or_missing', count(*)
FROM public.alerts a LEFT JOIN public.sensors s ON s.id = a.sensor_id
WHERE a.sensor_id IS NOT NULL
  AND (s.id IS NULL OR s.organization_id <> a.organization_id)
UNION ALL
SELECT 'actuator_asset_cross_tenant_or_missing', count(*)
FROM public.actuators a LEFT JOIN public.assets x ON x.id = a.asset_id
WHERE a.asset_id IS NOT NULL
  AND (x.id IS NULL OR x.organization_id <> a.organization_id)
UNION ALL
SELECT 'user_resource_id_orphan_or_cross_tenant', count(*)
FROM public.users u CROSS JOIN LATERAL jsonb_array_elements_text(COALESCE(u.resource_ids,'[]'::jsonb)) AS rid(resource_id)
WHERE NOT EXISTS (
    SELECT 1 FROM public.assets a WHERE a.id = rid.resource_id AND a.organization_id = u.organization_id
) AND NOT EXISTS (
    SELECT 1 FROM public.sensors s WHERE s.id = rid.resource_id AND s.organization_id = u.organization_id
);

-- Valores efectivos de Andrea; la cifra es variable y depende de asignaciones reales.
SELECT u.id, u.full_name, u.organization_id, u.facility_ids, u.resource_ids,
       count(DISTINCT a.id) AS assets_where_primary_responsible
FROM public.users u LEFT JOIN public.assets a ON a.responsible_user_id = u.id
WHERE u.full_name = 'Andrea Torres'
GROUP BY u.id, u.full_name, u.organization_id, u.facility_ids, u.resource_ids;

-- Confirmar que las migraciones quedaron aplicadas sin errores.
SELECT version, description, success FROM public.flyway_schema_history ORDER BY installed_rank;
