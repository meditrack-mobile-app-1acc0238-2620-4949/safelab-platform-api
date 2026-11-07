package com.safelab.platform.shared.infrastructure;

import com.safelab.platform.identityaccess.domain.OrganizationRecord;
import com.safelab.platform.identityaccess.domain.UserRecord;
import com.safelab.platform.identityaccess.domain.InvitationRecord;
import com.safelab.platform.identityaccess.domain.PermissionAssignmentRecord;
import com.safelab.platform.identityaccess.domain.AccountRequestRecord;
import com.safelab.platform.facilityassets.domain.FacilityRecord;
import com.safelab.platform.facilityassets.domain.AssetRecord;
import com.safelab.platform.sensormonitoring.domain.SensorRecord;
import com.safelab.platform.equipmentmaintenance.domain.MaintenanceRecord;
import com.safelab.platform.alertsincidents.domain.AlertRecord;
import com.safelab.platform.alertsincidents.domain.NotificationRecord;
import com.safelab.platform.alertsincidents.domain.IncidentRecord;
import com.safelab.platform.reportinganalytics.domain.ReportRecord;
import com.safelab.platform.audittraceability.domain.AuditLogRecord;
import com.safelab.platform.subscriptionbilling.domain.SubscriptionRecord;
import com.safelab.platform.environmentalcompliance.domain.ComplianceRuleRecord;
import com.safelab.platform.environmentalcompliance.domain.ComplianceEvaluationRecord;
import com.safelab.platform.environmentalcompliance.domain.ComplianceViolationRecord;
import com.safelab.platform.remoteactuation.domain.ActuatorRecord;
import com.safelab.platform.remoteactuation.domain.RemoteCommandRecord;
import com.safelab.platform.shared.domain.StoredEntity;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registro fijo de colecciones. No se aceptan nombres de tabla desde HTTP. */
@Component
public class EntityRegistry {
    private final Map<String, Class<? extends StoredEntity>> collectionTypes = new LinkedHashMap<>();

    public EntityRegistry() {
        collectionTypes.put("organizations", com.safelab.platform.identityaccess.domain.OrganizationRecord.class);
        collectionTypes.put("users", com.safelab.platform.identityaccess.domain.UserRecord.class);
        collectionTypes.put("invitations", com.safelab.platform.identityaccess.domain.InvitationRecord.class);
        collectionTypes.put("permissionAssignments", com.safelab.platform.identityaccess.domain.PermissionAssignmentRecord.class);
        collectionTypes.put("accountRequests", com.safelab.platform.identityaccess.domain.AccountRequestRecord.class);
        collectionTypes.put("facilities", com.safelab.platform.facilityassets.domain.FacilityRecord.class);
        collectionTypes.put("assets", com.safelab.platform.facilityassets.domain.AssetRecord.class);
        collectionTypes.put("sensors", com.safelab.platform.sensormonitoring.domain.SensorRecord.class);
        collectionTypes.put("maintenanceRecords", com.safelab.platform.equipmentmaintenance.domain.MaintenanceRecord.class);
        collectionTypes.put("alerts", com.safelab.platform.alertsincidents.domain.AlertRecord.class);
        collectionTypes.put("notifications", com.safelab.platform.alertsincidents.domain.NotificationRecord.class);
        collectionTypes.put("incidents", com.safelab.platform.alertsincidents.domain.IncidentRecord.class);
        collectionTypes.put("reports", com.safelab.platform.reportinganalytics.domain.ReportRecord.class);
        collectionTypes.put("auditLogs", com.safelab.platform.audittraceability.domain.AuditLogRecord.class);
        collectionTypes.put("subscriptions", com.safelab.platform.subscriptionbilling.domain.SubscriptionRecord.class);
        collectionTypes.put("complianceRules", com.safelab.platform.environmentalcompliance.domain.ComplianceRuleRecord.class);
        collectionTypes.put("complianceEvaluations", com.safelab.platform.environmentalcompliance.domain.ComplianceEvaluationRecord.class);
        collectionTypes.put("complianceViolations", com.safelab.platform.environmentalcompliance.domain.ComplianceViolationRecord.class);
        collectionTypes.put("actuators", com.safelab.platform.remoteactuation.domain.ActuatorRecord.class);
        collectionTypes.put("remoteCommands", com.safelab.platform.remoteactuation.domain.RemoteCommandRecord.class);
    }

    public Class<? extends StoredEntity> type(String collection) {
        Class<? extends StoredEntity> entity = collectionTypes.get(collection);
        if (entity == null) throw new IllegalArgumentException("Coleccion desconocida: " + collection);
        return entity;
    }
}
