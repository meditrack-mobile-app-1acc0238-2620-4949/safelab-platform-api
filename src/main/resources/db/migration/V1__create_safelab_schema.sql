-- SafeLab Backend v2.0.0 - estructuras relacionales para los 12 BC.
-- Usar en una base safelab_db nueva, creada desde pgAdmin con owner safelab_migrator.
-- Flyway ejecuta esta migracion una sola vez, nunca hay DROP ni reinicializacion automatica.

CREATE TABLE organizations (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    name text,
    ruc text,
    segment text,
    corporate_email text,
    city text,
    status text,
    manager_id text,
    facility_ids jsonb,
    created_at text,
    updated_at text
);

CREATE TABLE users (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    username text,
    email text,
    password text,
    first_name text,
    last_name text,
    full_name text,
    initials text,
    phone text,
    role text,
    position text,
    status text,
    organization text,
    facility_id text,
    facility_ids jsonb,
    segment text,
    resource_ids jsonb,
    age text,
    city text,
    job_title text,
    timezone text,
    created_at text,
    updated_at text,
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_users_organization ON users(organization_id);

CREATE TABLE invitations (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    code text,
    email text,
    role text,
    status text,
    expires_at text,
    created_at text,
    used_by text,
    facility_ids jsonb,
    resource_ids jsonb,
    CONSTRAINT fk_invitations_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_invitations_organization ON invitations(organization_id);

CREATE TABLE permission_assignments (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    user_id text,
    permissions jsonb,
    CONSTRAINT fk_permission_assignments_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_permission_assignments_organization ON permission_assignments(organization_id);

CREATE TABLE account_requests (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    type text,
    email text,
    status text,
    created_at text,
    notes text,
    CONSTRAINT fk_account_requests_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_account_requests_organization ON account_requests(organization_id);

CREATE TABLE facilities (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    name text,
    type text,
    city text,
    manager text,
    health_score integer,
    compliance_score integer,
    telemetry_score integer,
    CONSTRAINT fk_facilities_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_facilities_organization ON facilities(organization_id);

CREATE TABLE assets (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    name text,
    category text,
    facility_id text,
    storage_unit text,
    location text,
    last_inspection text,
    responsible text,
    status text,
    scope text,
    updated_at text,
    created_at text,
    CONSTRAINT fk_assets_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_assets_organization ON assets(organization_id);

CREATE TABLE sensors (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    code text,
    name text,
    facility_id text,
    asset_id text,
    type text,
    unit text,
    value jsonb,
    min integer,
    max integer,
    status text,
    connection text,
    last_reading text,
    responsible text,
    updated_at text,
    created_at text,
    CONSTRAINT fk_sensors_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_sensors_organization ON sensors(organization_id);

CREATE TABLE maintenance_records (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    equipment_id text,
    type text,
    status text,
    date text,
    responsible text,
    notes text,
    updated_at text,
    created_at text,
    CONSTRAINT fk_maintenance_records_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_maintenance_records_organization ON maintenance_records(organization_id);

CREATE TABLE alerts (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    type text,
    title text,
    message text,
    sensor_id text,
    asset_id text,
    facility_id text,
    severity text,
    status text,
    created_at text,
    assigned_to text,
    notes text,
    updated_at text,
    CONSTRAINT fk_alerts_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_alerts_organization ON alerts(organization_id);

CREATE TABLE notifications (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    title text,
    message text,
    context text,
    severity text,
    unread boolean,
    created_at text,
    updated_at text,
    CONSTRAINT fk_notifications_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_notifications_organization ON notifications(organization_id);

CREATE TABLE incidents (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    code text,
    title text,
    description text,
    facility_id text,
    related_alert_id text,
    related_sensor_id text,
    related_asset_id text,
    severity text,
    status text,
    assigned_to text,
    due_date text,
    evidence integer,
    created_at text,
    updated_at text,
    notes text,
    CONSTRAINT fk_incidents_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_incidents_organization ON incidents(organization_id);

CREATE TABLE reports (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    title text,
    type text,
    format text,
    status text,
    generated_at text,
    updated_at text,
    created_at text,
    CONSTRAINT fk_reports_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_reports_organization ON reports(organization_id);

CREATE TABLE audit_logs (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    actor text,
    action text,
    module text,
    severity text,
    status text,
    created_at text,
    evidence text,
    CONSTRAINT fk_audit_logs_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_audit_logs_organization ON audit_logs(organization_id);

CREATE TABLE subscriptions (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    plan text,
    status text,
    start_date text,
    renewal_date text,
    included_seats integer,
    additional_seats integer,
    seat_price integer,
    base_amount integer,
    monthly_cost integer,
    currency_code text,
    invoices jsonb,
    CONSTRAINT fk_subscriptions_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_subscriptions_organization ON subscriptions(organization_id);

CREATE TABLE compliance_rules (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    title text,
    title_es text,
    type text,
    sensor_id text,
    min integer,
    max integer,
    status text,
    required_evidence boolean,
    updated_at text,
    created_at text,
    CONSTRAINT fk_compliance_rules_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_compliance_rules_organization ON compliance_rules(organization_id);

CREATE TABLE compliance_evaluations (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    rule_id text,
    sensor_id text,
    observed_at text,
    value double precision,
    status text,
    CONSTRAINT fk_compliance_evaluations_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_compliance_evaluations_organization ON compliance_evaluations(organization_id);

CREATE TABLE compliance_violations (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    evaluation_id text,
    incident_code text,
    started_at text,
    ended_at text,
    severity text,
    status text,
    CONSTRAINT fk_compliance_violations_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_compliance_violations_organization ON compliance_violations(organization_id);

CREATE TABLE actuators (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    asset_id text,
    code text,
    name text,
    type text,
    facility_id text,
    location text,
    linked_sensor_id text,
    safety_state text,
    status text,
    running boolean,
    last_command text,
    updated_at text,
    created_at text,
    CONSTRAINT fk_actuators_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_actuators_organization ON actuators(organization_id);

CREATE TABLE remote_commands (
    id text PRIMARY KEY,
    organization_id text,
    extra_attributes jsonb NOT NULL DEFAULT '{}'::jsonb,
    actuator_id text,
    command text,
    device text,
    status text,
    requested_by text,
    created_at text,
    updated_at text,
    CONSTRAINT fk_remote_commands_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX ix_remote_commands_organization ON remote_commands(organization_id);

-- Restricciones de identidad, facturacion y empleados.
CREATE UNIQUE INDEX ux_users_email ON users(lower(email));
CREATE UNIQUE INDEX ux_users_username ON users(username);
CREATE UNIQUE INDEX ux_organizations_ruc ON organizations(ruc);
CREATE UNIQUE INDEX ux_invitations_code ON invitations(code);
CREATE UNIQUE INDEX ux_permission_assignments_user_id ON permission_assignments(user_id);
CREATE UNIQUE INDEX ux_subscriptions_organization_id ON subscriptions(organization_id);
-- Solo se guardan hashes SHA-256 de tokens aleatorios (nunca el token en texto plano).
CREATE TABLE auth_sessions (
    token_hash text PRIMARY KEY,
    user_id text NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at timestamptz NOT NULL
);
CREATE INDEX ix_auth_sessions_user ON auth_sessions(user_id);
CREATE INDEX ix_auth_sessions_expires ON auth_sessions(expires_at);
