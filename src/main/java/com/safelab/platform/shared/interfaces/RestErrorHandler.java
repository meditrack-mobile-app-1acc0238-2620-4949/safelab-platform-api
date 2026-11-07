package com.safelab.platform.shared.interfaces;

import com.safelab.platform.shared.domain.ApiError;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.infrastructure.ApiMessageTranslator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestErrorHandler {
    private final ApiMessageTranslator translations;
    public RestErrorHandler(ApiMessageTranslator translations) { this.translations = translations; }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> app(ApiException e, HttpServletRequest request) {
        return ResponseEntity.status(e.status()).body(ApiError.of(e.status().value(), e.code(), translations.translate(e.code(), e.getMessage(), request), request.getRequestURI(), Map.of()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiError.of(400, "VALIDATION_ERROR", translations.translate("VALIDATION_ERROR", "Revisa los campos indicados.", request), request.getRequestURI(), fields));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> invalidBody(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(400, "INVALID_JSON", translations.translate("INVALID_JSON", "El cuerpo de la solicitud no es JSON válido.", request), request.getRequestURI(), Map.of()));
    }
    @ExceptionHandler({CannotCreateTransactionException.class, DataAccessResourceFailureException.class})
    ResponseEntity<ApiError> databaseUnavailable(Exception e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(503, "DATABASE_UNAVAILABLE", translations.translate("DATABASE_UNAVAILABLE",
                    "PostgreSQL no está disponible.", request), request.getRequestURI(), Map.of()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException e, HttpServletRequest request) {
        String name = "";
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation && violation.getConstraintName() != null) {
                name = violation.getConstraintName().toLowerCase(java.util.Locale.ROOT);
                break;
            }
            cause = cause.getCause();
        }
        String code = name.contains("email") ? "EMAIL_ALREADY_REGISTERED" :
            name.contains("username") ? "USERNAME_ALREADY_EXISTS" :
            name.contains("seat") ? "NO_AVAILABLE_SEATS" :
            name.contains("foreign") || name.startsWith("fk_") ? "INVALID_REFERENCE" : "DATA_CONFLICT";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, code, translations.translate(code,
                    "El registro tiene datos duplicados o referencias inválidas.", request),
                    request.getRequestURI(), Map.of()));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "INTERNAL_ERROR", translations.translate("INTERNAL_ERROR", "No se pudo procesar la operación.", request), request.getRequestURI(), Map.of()));
    }
}
