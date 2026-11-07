-- SafeLab: integridad relacional y restricciones, sin insertar datos ficticios.
-- Version inicial: nuevas bases vacias. Mantener este script INMUTABLE una vez aplicado.

ALTER TABLE users ADD CONSTRAINT ck_users_username_18_digits CHECK (username ~ '^[1-9][0-9]{17}$');
ALTER TABLE users ADD CONSTRAINT ck_users_uuid CHECK (id ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$');
ALTER TABLE users ADD CONSTRAINT ck_users_role_tenant CHECK ((role = 'platformAdmin' AND organization_id IS NULL) OR (role <> 'platformAdmin' AND organization_id IS NOT NULL));
ALTER TABLE users ADD CONSTRAINT ck_users_email CHECK (email LIKE '%@%');
ALTER TABLE users ALTER COLUMN username SET NOT NULL;
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
ALTER TABLE users ALTER COLUMN status SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT ck_user_password_bcrypt CHECK (length(password) >= 59 AND left(password,4) IN ('$2a$','$2b$','$2y$'));
ALTER TABLE users ADD CONSTRAINT ck_user_status CHECK (status IN ('active','inactive','suspended','pending_email_verification','pending_corporate_verification'));
ALTER TABLE users ADD CONSTRAINT ck_user_role CHECK (role IN ('platformAdmin','organizationAdmin','bioclinicalCoordinator','labTechnician','maintenanceOperator','complianceOfficer','pharmaOperationsCoordinator','pharmaTechnician'));
ALTER TABLE invitations ADD CONSTRAINT ck_invitation_status CHECK (status IN ('pending','used','cancelled','expired'));
ALTER TABLE subscriptions ADD CONSTRAINT ck_subscription_status CHECK (status IN ('active','inactive','pending','suspended','cancelled','expired'));
ALTER TABLE organizations ADD CONSTRAINT ck_organizations_ruc CHECK (ruc ~ '^[0-9]{11}$');
ALTER TABLE organizations ALTER COLUMN name SET NOT NULL;
ALTER TABLE organizations ALTER COLUMN ruc SET NOT NULL;
ALTER TABLE organizations ADD CONSTRAINT uq_organizations_org_id UNIQUE (id);
ALTER TABLE subscriptions ADD CONSTRAINT ck_subscriptions_seats CHECK (included_seats >= 0 AND additional_seats >= 0);
ALTER TABLE subscriptions ADD CONSTRAINT ck_subscriptions_amount CHECK (monthly_cost >= 0 AND seat_price >= 0 AND base_amount >= 0);
ALTER TABLE sensors ADD CONSTRAINT ck_sensors_range CHECK (min IS NULL OR max IS NULL OR min <= max);
ALTER TABLE compliance_rules ADD CONSTRAINT ck_rules_range CHECK (min IS NULL OR max IS NULL OR min <= max);
ALTER TABLE incidents ADD CONSTRAINT ck_incidents_evidence CHECK (evidence IS NULL OR evidence >= 0);
ALTER TABLE organizations ADD CONSTRAINT fk_organizations_manager FOREIGN KEY (manager_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE users ADD CONSTRAINT uq_users_org_id UNIQUE (organization_id,id);
ALTER TABLE invitations ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE invitations ADD CONSTRAINT uq_invitations_org_id UNIQUE (organization_id,id);
ALTER TABLE permission_assignments ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE permission_assignments ADD CONSTRAINT uq_permission_assignments_org_id UNIQUE (organization_id,id);
ALTER TABLE account_requests ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE account_requests ADD CONSTRAINT uq_account_requests_org_id UNIQUE (organization_id,id);
ALTER TABLE facilities ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE facilities ADD CONSTRAINT uq_facilities_org_id UNIQUE (organization_id,id);
ALTER TABLE assets ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE assets ADD CONSTRAINT uq_assets_org_id UNIQUE (organization_id,id);
ALTER TABLE sensors ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE sensors ADD CONSTRAINT uq_sensors_org_id UNIQUE (organization_id,id);
ALTER TABLE maintenance_records ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE maintenance_records ADD CONSTRAINT uq_maintenance_records_org_id UNIQUE (organization_id,id);
ALTER TABLE alerts ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE alerts ADD CONSTRAINT uq_alerts_org_id UNIQUE (organization_id,id);
ALTER TABLE notifications ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE notifications ADD CONSTRAINT uq_notifications_org_id UNIQUE (organization_id,id);
ALTER TABLE incidents ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE incidents ADD CONSTRAINT uq_incidents_org_id UNIQUE (organization_id,id);
ALTER TABLE reports ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE reports ADD CONSTRAINT uq_reports_org_id UNIQUE (organization_id,id);
ALTER TABLE audit_logs ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE audit_logs ADD CONSTRAINT uq_audit_logs_org_id UNIQUE (organization_id,id);
ALTER TABLE subscriptions ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE subscriptions ADD CONSTRAINT uq_subscriptions_org_id UNIQUE (organization_id,id);
ALTER TABLE compliance_rules ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE compliance_rules ADD CONSTRAINT uq_compliance_rules_org_id UNIQUE (organization_id,id);
ALTER TABLE compliance_evaluations ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE compliance_evaluations ADD CONSTRAINT uq_compliance_evaluations_org_id UNIQUE (organization_id,id);
ALTER TABLE compliance_violations ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE compliance_violations ADD CONSTRAINT uq_compliance_violations_org_id UNIQUE (organization_id,id);
ALTER TABLE actuators ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE actuators ADD CONSTRAINT uq_actuators_org_id UNIQUE (organization_id,id);
ALTER TABLE remote_commands ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE remote_commands ADD CONSTRAINT uq_remote_commands_org_id UNIQUE (organization_id,id);
ALTER TABLE users ADD CONSTRAINT fk_users_facility_id_tenant FOREIGN KEY (organization_id,facility_id) REFERENCES facilities(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE invitations ADD CONSTRAINT fk_invitations_used_by_tenant FOREIGN KEY (organization_id,used_by) REFERENCES users(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE permission_assignments ADD CONSTRAINT fk_permission_assignments_user_id_tenant FOREIGN KEY (organization_id,user_id) REFERENCES users(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE assets ADD CONSTRAINT fk_assets_facility_id_tenant FOREIGN KEY (organization_id,facility_id) REFERENCES facilities(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE sensors ADD CONSTRAINT fk_sensors_facility_id_tenant FOREIGN KEY (organization_id,facility_id) REFERENCES facilities(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE sensors ADD CONSTRAINT fk_sensors_asset_id_tenant FOREIGN KEY (organization_id,asset_id) REFERENCES assets(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE maintenance_records ADD CONSTRAINT fk_maintenance_records_equipment_id_tenant FOREIGN KEY (organization_id,equipment_id) REFERENCES assets(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE alerts ADD CONSTRAINT fk_alerts_sensor_id_tenant FOREIGN KEY (organization_id,sensor_id) REFERENCES sensors(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE alerts ADD CONSTRAINT fk_alerts_asset_id_tenant FOREIGN KEY (organization_id,asset_id) REFERENCES assets(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE alerts ADD CONSTRAINT fk_alerts_facility_id_tenant FOREIGN KEY (organization_id,facility_id) REFERENCES facilities(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE incidents ADD CONSTRAINT fk_incidents_facility_id_tenant FOREIGN KEY (organization_id,facility_id) REFERENCES facilities(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE incidents ADD CONSTRAINT fk_incidents_related_alert_id_tenant FOREIGN KEY (organization_id,related_alert_id) REFERENCES alerts(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE incidents ADD CONSTRAINT fk_incidents_related_sensor_id_tenant FOREIGN KEY (organization_id,related_sensor_id) REFERENCES sensors(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE incidents ADD CONSTRAINT fk_incidents_related_asset_id_tenant FOREIGN KEY (organization_id,related_asset_id) REFERENCES assets(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE compliance_rules ADD CONSTRAINT fk_compliance_rules_sensor_id_tenant FOREIGN KEY (organization_id,sensor_id) REFERENCES sensors(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE compliance_evaluations ADD CONSTRAINT fk_compliance_evaluations_rule_id_tenant FOREIGN KEY (organization_id,rule_id) REFERENCES compliance_rules(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE compliance_evaluations ADD CONSTRAINT fk_compliance_evaluations_sensor_id_tenant FOREIGN KEY (organization_id,sensor_id) REFERENCES sensors(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE compliance_violations ADD CONSTRAINT fk_compliance_violations_evaluation_id_tenant FOREIGN KEY (organization_id,evaluation_id) REFERENCES compliance_evaluations(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE actuators ADD CONSTRAINT fk_actuators_asset_id_tenant FOREIGN KEY (organization_id,asset_id) REFERENCES assets(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE actuators ADD CONSTRAINT fk_actuators_facility_id_tenant FOREIGN KEY (organization_id,facility_id) REFERENCES facilities(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE actuators ADD CONSTRAINT fk_actuators_linked_sensor_id_tenant FOREIGN KEY (organization_id,linked_sensor_id) REFERENCES sensors(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE remote_commands ADD CONSTRAINT fk_remote_commands_actuator_id_tenant FOREIGN KEY (organization_id,actuator_id) REFERENCES actuators(organization_id,id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE incidents ADD CONSTRAINT uq_incidents_code_org UNIQUE (organization_id,code);
ALTER TABLE compliance_violations ADD CONSTRAINT fk_violations_incident_tenant
FOREIGN KEY (organization_id,incident_code) REFERENCES incidents(organization_id,code) DEFERRABLE INITIALLY DEFERRED;
CREATE UNIQUE INDEX ux_invitation_pending_email ON invitations(organization_id, lower(email)) WHERE status = 'pending';
CREATE INDEX ix_sensors_facility ON sensors(organization_id,facility_id);
CREATE INDEX ix_assets_facility ON assets(organization_id,facility_id);
CREATE INDEX ix_alerts_sensor ON alerts(organization_id,sensor_id);

-- Control de cupos al COMMIT (considera invitaciones pendientes y empleados, excluye gerente).
-- Bloquea la fila de suscripcion para serializar altas concurrentes de la misma empresa.
CREATE OR REPLACE FUNCTION safelab_check_seats() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE target_org text;
DECLARE capacity integer;
DECLARE occupied integer;
BEGIN
    target_org := COALESCE(NEW.organization_id, OLD.organization_id);
    SELECT (included_seats + additional_seats) INTO capacity
      FROM subscriptions WHERE organization_id = target_org AND status = 'active' FOR UPDATE;
    IF capacity IS NULL THEN RETURN NULL; END IF;
    SELECT (SELECT count(*) FROM users WHERE organization_id = target_org AND role <> 'organizationAdmin') +
           (SELECT count(*) FROM invitations WHERE organization_id = target_org AND status = 'pending')
      INTO occupied;
    IF occupied > capacity THEN
        RAISE EXCEPTION 'NO_AVAILABLE_SEATS for organization %', target_org USING ERRCODE = '23514', CONSTRAINT = 'ck_seat_capacity';
    END IF;
    RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER ck_seat_users AFTER INSERT OR UPDATE OR DELETE ON users
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION safelab_check_seats();
CREATE CONSTRAINT TRIGGER ck_seat_invitations AFTER INSERT OR UPDATE OR DELETE ON invitations
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION safelab_check_seats();

CREATE CONSTRAINT TRIGGER ck_seat_subscriptions AFTER INSERT OR UPDATE ON subscriptions
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION safelab_check_seats();
