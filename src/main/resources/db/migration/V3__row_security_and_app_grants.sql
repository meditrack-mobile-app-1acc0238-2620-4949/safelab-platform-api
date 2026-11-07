-- Solo la conexion tecnica del backend usa safelab_app.
-- RLS protege filas sin contexto. El backend fija contexto por transaccion,
-- despues de autenticar al usuario. Ningun cliente recibe credenciales SQL.
-- NOTA: quien conozca la clave de safelab_app podria cambiar GUC: RLS
-- por si sola no sirve como aislamiento ante credenciales SQL filtradas.
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
CREATE POLICY organizations_tenant_policy ON organizations FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
CREATE POLICY users_tenant_policy ON users FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE invitations ENABLE ROW LEVEL SECURITY;
CREATE POLICY invitations_tenant_policy ON invitations FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE permission_assignments ENABLE ROW LEVEL SECURITY;
CREATE POLICY permission_assignments_tenant_policy ON permission_assignments FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE account_requests ENABLE ROW LEVEL SECURITY;
CREATE POLICY account_requests_tenant_policy ON account_requests FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE facilities ENABLE ROW LEVEL SECURITY;
CREATE POLICY facilities_tenant_policy ON facilities FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE assets ENABLE ROW LEVEL SECURITY;
CREATE POLICY assets_tenant_policy ON assets FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE sensors ENABLE ROW LEVEL SECURITY;
CREATE POLICY sensors_tenant_policy ON sensors FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE maintenance_records ENABLE ROW LEVEL SECURITY;
CREATE POLICY maintenance_records_tenant_policy ON maintenance_records FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE alerts ENABLE ROW LEVEL SECURITY;
CREATE POLICY alerts_tenant_policy ON alerts FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
CREATE POLICY notifications_tenant_policy ON notifications FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE incidents ENABLE ROW LEVEL SECURITY;
CREATE POLICY incidents_tenant_policy ON incidents FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE reports ENABLE ROW LEVEL SECURITY;
CREATE POLICY reports_tenant_policy ON reports FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
CREATE POLICY audit_logs_tenant_policy ON audit_logs FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE subscriptions ENABLE ROW LEVEL SECURITY;
CREATE POLICY subscriptions_tenant_policy ON subscriptions FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE compliance_rules ENABLE ROW LEVEL SECURITY;
CREATE POLICY compliance_rules_tenant_policy ON compliance_rules FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE compliance_evaluations ENABLE ROW LEVEL SECURITY;
CREATE POLICY compliance_evaluations_tenant_policy ON compliance_evaluations FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE compliance_violations ENABLE ROW LEVEL SECURITY;
CREATE POLICY compliance_violations_tenant_policy ON compliance_violations FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE actuators ENABLE ROW LEVEL SECURITY;
CREATE POLICY actuators_tenant_policy ON actuators FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));
ALTER TABLE remote_commands ENABLE ROW LEVEL SECURITY;
CREATE POLICY remote_commands_tenant_policy ON remote_commands FOR ALL TO safelab_app
    USING (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),'')) WITH CHECK (current_setting('safelab.platform_access', true) = 'true' OR organization_id = nullif(current_setting('safelab.tenant_id', true),''));

-- Privilegios exclusivamente para las 20 tablas de BC y auth_sessions.
-- NO se concede a safelab_app acceso al historial de migraciones.
GRANT USAGE ON SCHEMA public TO safelab_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.organizations,
    public.users,
    public.invitations,
    public.permission_assignments,
    public.account_requests,
    public.facilities,
    public.assets,
    public.sensors,
    public.maintenance_records,
    public.alerts,
    public.notifications,
    public.incidents,
    public.reports,
    public.audit_logs,
    public.subscriptions,
    public.compliance_rules,
    public.compliance_evaluations,
    public.compliance_violations,
    public.actuators,
    public.remote_commands,
    public.auth_sessions TO safelab_app;
REVOKE ALL ON TABLE public.flyway_schema_history FROM safelab_app;
REVOKE CREATE ON SCHEMA public FROM safelab_app;
-- En cada migracion futura que cree una tabla nueva, conceder los permisos
-- necesarios de forma explicita, nunca GRANT sobre todas las tablas.
