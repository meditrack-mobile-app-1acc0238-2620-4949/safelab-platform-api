package com.safelab.platform.shared.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import java.util.Locale;

/** Traduce por código sin modificar el HTTP status, la ruta ni el código estable. */
@Component
public class ApiMessageTranslator {
    private final MessageSource messages;
    public ApiMessageTranslator(MessageSource messages) { this.messages = messages; }

    public String translate(String code, String original, HttpServletRequest request) {
        // Usamos explícitamente Accept-Language para que funcione incluso desde
        // el filtro de autenticación, antes de que se ejecute Spring MVC.
        String header = request.getHeader("Accept-Language");
        Locale requested = header == null || header.isBlank()
            ? request.getLocale() : Locale.forLanguageTag(header.split(",", 2)[0].trim());
        Locale supported = requested != null && "en".equalsIgnoreCase(requested.getLanguage())
            ? Locale.ENGLISH : Locale.forLanguageTag("es");
        return messages.getMessage("error." + code, null, original, supported);
    }
}
