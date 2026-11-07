package com.safelab.platform.userprofiles.presentation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/userprofiles")
@Tag(name="09. User Profile Management")
public class UserProfilesController {
    private final AuthService session;private final DataGateway db;
    private static final Pattern NAME=Pattern.compile("[\\p{L} ]{2,80}");
    private static final Set<String> EDITABLE=Set.of("firstName","lastName","phone","age","city","jobTitle");
    public UserProfilesController(AuthService session,DataGateway db){this.session=session;this.db=db;}
    @GetMapping("/me") @Operation(summary="Ver mi perfil")
    public ObjectNode profile(){return session.sanitized(session.current());}
    @PatchMapping("/me") @Operation(summary="Actualizar datos personales editables (sin cambiar username ni rol)")
    public ObjectNode update(@RequestBody ObjectNode changes){
        var fields = changes.fields();
        while(fields.hasNext()){ var field = fields.next();
            if(!EDITABLE.contains(field.getKey()))throw new ApiException(HttpStatus.BAD_REQUEST,"FIELD_NOT_EDITABLE","No se puede modificar: "+field.getKey());
            if((field.getKey().equals("firstName")||field.getKey().equals("lastName"))
                && !NAME.matcher(field.getValue().asText()).matches())throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_NAME","Los nombres solo admiten letras y espacios.");
            if(field.getKey().equals("phone")&&!field.getValue().asText().matches("[0-9]{9,15}"))
                throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_PHONE","El teléfono debe tener entre 9 y 15 dígitos.");
            if(field.getKey().equals("age") && !field.getValue().isNull() && (!field.getValue().isInt()||field.getValue().asInt()<18||field.getValue().asInt()>120))
                throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_AGE","La edad debe estar entre 18 y 120 años.");
        }
        ObjectNode patch=changes.deepCopy();ObjectNode user=(ObjectNode)session.current();
        String first=patch.path("firstName").asText(user.path("firstName").asText());
        String last=patch.path("lastName").asText(user.path("lastName").asText());
        patch.put("fullName",first+" "+last);
        return session.sanitized(db.patch("users",user.path("id").asText(),patch));
    }
}
