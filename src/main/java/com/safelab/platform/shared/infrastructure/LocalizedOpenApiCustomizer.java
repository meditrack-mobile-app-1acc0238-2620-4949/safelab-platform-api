package com.safelab.platform.shared.infrastructure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.MessageSource;

import java.util.*;

/**
 * Traduce solo la DOCUMENTACIÓN. Los contratos REST, identificadores JSON,
 * permisos, validaciones y lógica de negocio son exactamente los de v1.
 *
 * Para añadir un endpoint en el futuro, agrega su clave op.METODO.ruta.summary
 * a openapi_es.properties y openapi_en.properties.
 */
public class LocalizedOpenApiCustomizer implements OpenApiCustomizer {
    private final MessageSource messages;
    private final Locale locale;

    // Los nombres originales de @Tag permiten que los controladores sigan siendo legibles.
    private static final Map<String, String> TAG_KEYS = Map.ofEntries(
        Map.entry("00. Local health", "health"),
        Map.entry("01. Identity & Access Management", "iam"),
        Map.entry("01. SafeLab Platform Administration", "platform"),
        Map.entry("02. Facility & Asset Management", "assets"),
        Map.entry("03. Sensor & Telemetry Monitoring", "sensors"),
        Map.entry("04. Equipment Health & Maintenance", "maintenance"),
        Map.entry("05. Alert & Incident Management", "alerts"),
        Map.entry("06. Operational Dashboard & Insights", "dashboard"),
        Map.entry("07. Reporting & Analytics", "reports"),
        Map.entry("08. Audit & Traceability", "audit"),
        Map.entry("09. User Profile Management", "profile"),
        Map.entry("10. Subscription & Billing", "billing"),
        Map.entry("11. Environmental Compliance Management", "compliance"),
        Map.entry("12. Remote Control & Actuation", "actuation")
    );

    // Orden estable de las secciones para ES y EN; nunca depender de su traduccion.
    // El bloque de administracion de plataforma se muestra despues de IAM.
    private static final List<String> TAG_ORDER = List.of(
        "00. Local health",
        "01. Identity & Access Management",
        "01. SafeLab Platform Administration",
        "02. Facility & Asset Management",
        "03. Sensor & Telemetry Monitoring",
        "04. Equipment Health & Maintenance",
        "05. Alert & Incident Management",
        "06. Operational Dashboard & Insights",
        "07. Reporting & Analytics",
        "08. Audit & Traceability",
        "09. User Profile Management",
        "10. Subscription & Billing",
        "11. Environmental Compliance Management",
        "12. Remote Control & Actuation"
    );

    private static final Set<String> CREATED = Set.of(
        "POST /api/v1/auth/register/company",
        "POST /api/v1/auth/register/invitation",
        "POST /api/v1/identityaccess/organizations/{orgId}/invitations",
        "POST /api/v1/facilityassets/assets",
        "POST /api/v1/sensormonitoring/sensors",
        "POST /api/v1/equipmentmaintenance/records",
        "POST /api/v1/alertsincidents/incidents",
        "POST /api/v1/reportinganalytics/reports",
        "POST /api/v1/environmentalcompliance/rules",
        "POST /api/v1/remoteactuation/actuators/{id}/commands"
    );
    private static final Set<String> NO_CONTENT = Set.of(
        "POST /api/v1/auth/logout", "DELETE /api/v1/facilityassets/assets/{id}"
    );
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
        "GET /api/v1/health", "POST /api/v1/auth/login",
        "POST /api/v1/auth/register/company", "POST /api/v1/auth/register/invitation"
    );

    public LocalizedOpenApiCustomizer(MessageSource messages, Locale locale) {
        this.messages = messages;
        this.locale = locale;
    }

    private String t(String key, String fallback) {
        return messages.getMessage(key, null, fallback, locale);
    }

    @Override
    public void customise(OpenAPI api) {
        api.setInfo(new Info().title(t("api.title", "SafeLab Platform API"))
            .version("1.0.0").description(t("api.description", "SafeLab local API")));
        if (api.getComponents() == null) api.setComponents(new io.swagger.v3.oas.models.Components());
        if (api.getComponents().getSecuritySchemes() != null
                && api.getComponents().getSecuritySchemes().get("bearerAuth") != null) {
            api.getComponents().getSecuritySchemes().get("bearerAuth")
                .setDescription(t("security.bearer.description", "Log in first"));
        }
        ensureErrorSchema(api);
        if (api.getComponents().getSchemas() != null) {
            api.getComponents().getSchemas().forEach((name, schema) -> {
                if ("ApiError".equals(name)) return;
                schema.setDescription(t("schema." + name + ".description", schema.getDescription()));
                if (schema.getProperties() != null) {
                    schema.getProperties().forEach((field, value) -> {
                        // getProperties() puede exponer los valores como Object.
                        // Verificamos su tipo antes de utilizar metodos de Swagger Schema.
                        if (value instanceof Schema<?> fieldSchema) {
                            fieldSchema.setDescription(t(
                                "field." + field + ".description",
                                fieldSchema.getDescription()
                            ));
                        }
                    });
                }
            });
        }
        // OpenAPI define el orden canonico de las secciones. Como los nombres se
        // traducen, ordenarlas alfabeticamente en Swagger las moveria por idioma.
        Map<String, Tag> originalTags = new LinkedHashMap<>();
        if (api.getTags() != null) {
            for (Tag tag : api.getTags()) originalTags.put(tag.getName(), tag);
        }
        List<Tag> orderedTags = new ArrayList<>();
        for (String originalName : TAG_ORDER) {
            Tag tag = originalTags.remove(originalName);
            if (tag == null) tag = new Tag().name(originalName);
            String key = TAG_KEYS.get(originalName);
            tag.setName(t("tag." + key + ".name", originalName));
            tag.setDescription(t("tag." + key + ".description", tag.getDescription()));
            orderedTags.add(tag);
        }
        // Etiquetas nuevas no previstas: no se pierden al agregar futuros endpoints.
        orderedTags.addAll(originalTags.values());
        api.setTags(orderedTags);
        if (api.getPaths() == null) return;
        api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
            String key = method.name() + " " + path;
            String translationKey = "op." + method.name() + "." + path;
            operation.setSummary(t(translationKey + ".summary", operation.getSummary()));
            operation.setDescription(t(translationKey + ".description", operation.getDescription()));
            if (operation.getTags() != null) {
                operation.setTags(operation.getTags().stream().map(name -> {
                    String tagKey = TAG_KEYS.get(name);
                    return tagKey == null ? name : t("tag." + tagKey + ".name", name);
                }).toList());
            }
            if (operation.getParameters() != null) {
                for (Parameter param : operation.getParameters()) {
                    param.setDescription(t("param." + param.getName() + ".description", param.getDescription()));
                }
            }
            // La documentación cambia con el selector; el idioma de los ERRORES se
            // elige explícitamente con este encabezado en Try it out.
            StringSchema allowedLanguages = new StringSchema();
            allowedLanguages.setEnum(List.of("es", "en"));
            Parameter languageHeader = new Parameter().in("header").name("Accept-Language")
                .description(t("header.language.description", "Response language (es/en)"))
                .required(false)
                .schema(allowedLanguages)
                .example(locale.getLanguage());
            operation.addParametersItem(languageHeader);
            documentResponses(operation, key, path);
        }));
    }

    private void ensureErrorSchema(OpenAPI api) {
        if (api.getComponents().getSchemas() != null && api.getComponents().getSchemas().containsKey("ApiError")) return;
        Schema<?> error = new ObjectSchema().description(t("schema.ApiError.description", "API error"));
        Map<String, Schema> fields = new LinkedHashMap<>();
        fields.put("timestamp", new DateTimeSchema().description("UTC timestamp"));
        fields.put("status", new IntegerSchema().description("HTTP status"));
        fields.put("code", new StringSchema().description("Stable error code"));
        fields.put("message", new StringSchema().description("Localized error message"));
        fields.put("path", new StringSchema().description("Requested path"));
        fields.put("fieldErrors", new MapSchema().additionalProperties(new StringSchema())
            .description("Field validation errors"));
        error.setProperties(fields);
        api.getComponents().addSchemas("ApiError", error);
    }

    private void documentResponses(Operation operation, String key, String path) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) { responses = new ApiResponses(); operation.setResponses(responses); }
        String success = CREATED.contains(key) ? "201" : NO_CONTENT.contains(key) ? "204" : "200";
        ApiResponse from200 = responses.remove("200");
        ApiResponse result = from200 == null ? new ApiResponse() : from200;
        result.setDescription(t("response." + success + ".description", "Success"));
        if ("204".equals(success)) result.setContent(null);
        responses.addApiResponse(success, result);

        boolean requiresAuth = !PUBLIC_ENDPOINTS.contains(key);
        boolean hasBody = operation.getRequestBody() != null;
        if (hasBody || path.contains("{")) errorResponse(responses, "400", path);
        if (requiresAuth || key.equals("POST /api/v1/auth/login")) errorResponse(responses, "401", path);
        if (requiresAuth) errorResponse(responses, "403", path);
        if (path.contains("{") || key.startsWith("POST /api/v1/auth/register/invitation")) errorResponse(responses, "404", path);
        if (key.contains("/register/") || key.contains("/invitations") || CREATED.contains(key)) errorResponse(responses, "409", path);
        if (!NO_CONTENT.contains(key) || requiresAuth) {
            errorResponse(responses, "502", path);
            errorResponse(responses, "503", path);
        }
        errorResponse(responses, "500", path);
    }

    private void errorResponse(ApiResponses responses, String code, String path) {
        // Mantiene un esquema común en todas las respuestas de fallo.
        Schema<?> ref = new Schema<>().$ref("#/components/schemas/ApiError");
        io.swagger.v3.oas.models.media.MediaType body = new io.swagger.v3.oas.models.media.MediaType().schema(ref);
        if ("403".equals(code)) {
            Map<String, Object> example = new LinkedHashMap<>();
            example.put("timestamp", "2026-11-07T10:12:11Z");
            example.put("status", 403);
            example.put("code", "ACCESS_DENIED");
            example.put("message", t("example.forbidden.message", "Access denied"));
            example.put("path", path.replace("{orgId}", "org-gabriel").replace("{id}", "example-id").replace("{userId}", "user-id"));
            example.put("fieldErrors", Map.of());
            body.example(example);
        }
        responses.addApiResponse(code, new ApiResponse()
            .description(t("response." + code + ".description", "Error"))
            .content(new Content().addMediaType("application/json", body)));
    }
}
