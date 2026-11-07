package com.safelab.platform.dashboardinsights.presentation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.RecordsService;
import com.safelab.platform.shared.infrastructure.JsonServerGateway;
import com.safelab.platform.shared.security.AccessRules;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboardinsights")
@Tag(name="06. Operational Dashboard & Insights")
public class DashboardInsightsController {
    private final RecordsService r;private final JsonServerGateway db;private final AccessRules a;
    public DashboardInsightsController(RecordsService r,JsonServerGateway db,AccessRules a){this.r=r;this.db=db;this.a=a;}
    @GetMapping("/overview") @Operation(summary="KPIs calculados para la empresa y permisos actuales")
    public ObjectNode overview(){a.require("dashboard.view");ObjectNode o=db.object();
        o.put("organizations",a.platform()?db.all("organizations").size():1);
        o.put("sensors",r.list("sensors","dashboard.view").size());o.put("assets",r.list("assets","dashboard.view").size());
        o.put("alerts",r.list("alerts","dashboard.view").size());o.put("incidents",r.list("incidents","dashboard.view").size());
        o.put("reports",r.list("reports","dashboard.view").size());return o;}
}
