package com.safelab.platform.identityaccess.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.identityaccess.presentation.dto.*;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name="01. Identity & Access Management",description="Sesiones persistidas, registro local y permisos empresariales. Sin envío de correos ni pagos.")
public class AuthController {
    private final AuthService sessions;
    private final DataGateway db;
    private final PasswordEncoder passwordEncoder;
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[@%&$]).{10,}$");
    public AuthController(AuthService sessions,DataGateway db,PasswordEncoder passwordEncoder){
        this.sessions=sessions;this.db=db;this.passwordEncoder=passwordEncoder;
    }
    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary="Iniciar sesión con correo o username",description="Devuelve un token temporal de sesión persistida para usar en Authorize de Swagger.")
    public ObjectNode login(@Valid @RequestBody LoginRequest request){return sessions.authenticate(request.identifier().trim(),request.password());}

    @GetMapping("/me")
    @Operation(summary="Ver identidad de la sesión actual")
    public ObjectNode me(){return sessions.sanitized(sessions.current());}

    @PostMapping("/logout")
    @Operation(summary="Cerrar la sesión actual")
    public ResponseEntity<Void> logout(){sessions.logout();return ResponseEntity.noContent().build();}

    @PostMapping("/register/company")
    @SecurityRequirements
    @Operation(summary="Registrar una empresa y su primer gerente",description="La solicitud queda pendiente de validación empresarial y pago: no activa el acceso ni envía correos.")
    @Transactional
    public ResponseEntity<ObjectNode> registerCompany(@Valid @RequestBody CompanyRegistrationRequest request) {
        checkPassword(request.password());
        for(JsonNode item:db.all("organizations"))
            if(request.ruc().equals(item.path("ruc").asText()))throw new ApiException(HttpStatus.CONFLICT,"RUC_ALREADY_REGISTERED","El RUC ya se encuentra registrado.");
        checkEmail(request.email());
        String orgId="org-"+UUID.randomUUID(); String uid=UUID.randomUUID().toString();
        ObjectNode company=db.object();company.put("id",orgId);company.put("name",request.companyName().trim());
        company.put("ruc",request.ruc());company.put("segment",request.segment());company.put("corporateEmail",request.corporateEmail().toLowerCase());
        company.put("managerId",uid);company.put("status","pending_corporate_verification");company.set("facilityIds",db.array());
        db.create("organizations",company);
        ObjectNode user=db.object();user.put("id",uid);user.put("username",numericUsername());user.put("email",request.email().toLowerCase());
        user.put("password",passwordEncoder.encode(request.password()));user.put("firstName",request.firstName().trim());user.put("lastName",request.lastName().trim());
        user.put("fullName",request.firstName().trim()+" "+request.lastName().trim());user.put("phone",request.phone());user.put("role","organizationAdmin");
        user.put("status","pending_corporate_verification");user.put("organizationId",orgId);user.put("organization",request.companyName().trim());
        user.put("segment",request.segment());user.set("facilityIds",db.array());user.set("resourceIds",db.array());
        db.create("users",user);
        ObjectNode result=db.object();result.put("message","Registro recibido. Falta verificar la empresa y el pago.");
        result.put("status","pending_corporate_verification");result.put("organizationId",orgId);result.put("userId",uid);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/register/invitation")
    @SecurityRequirements
    @Operation(summary="Registrarse con una invitación activa",description="No requiere pago del empleado. El acceso queda pendiente de confirmar su correo.")
    @Transactional
    public ResponseEntity<ObjectNode> registerInvited(@Valid @RequestBody InvitationRegisterRequest request){
        checkPassword(request.password());
        JsonNode invitation=null;
        for(JsonNode value:db.all("invitations"))if(request.code().equals(value.path("code").asText())){invitation=value;break;}
        if(invitation==null)throw new ApiException(HttpStatus.NOT_FOUND,"INVITATION_NOT_FOUND","Código de invitación no encontrado.");
        if(!"pending".equals(invitation.path("status").asText()))throw new ApiException(HttpStatus.CONFLICT,"INVITATION_USED","La invitación ya fue utilizada o cancelada.");
        if(Instant.parse(invitation.path("expiresAt").asText()).isBefore(Instant.now()))
            throw new ApiException(HttpStatus.CONFLICT,"INVITATION_EXPIRED","La invitación ha expirado.");
        String email=invitation.path("email").asText();checkEmail(email);
        ObjectNode user=db.object();user.put("id",UUID.randomUUID().toString());user.put("username",numericUsername());
        user.put("email",email);user.put("password",passwordEncoder.encode(request.password()));user.put("firstName",request.firstName().trim());
        user.put("lastName",request.lastName().trim());user.put("fullName",request.firstName().trim()+" "+request.lastName().trim());
        user.put("phone",request.phone());user.put("organizationId",invitation.path("organizationId").asText());
        user.put("role",invitation.path("role").asText());user.put("status","pending_email_verification");
        JsonNode organization=db.get("organizations",invitation.path("organizationId").asText());
        user.put("organization",organization.path("name").asText());user.put("segment",organization.path("segment").asText());
        user.set("facilityIds",invitation.path("facilityIds").deepCopy());user.set("resourceIds",invitation.path("resourceIds").deepCopy());
        db.create("users",user);
        ObjectNode patch=db.object();patch.put("status","used");patch.put("usedBy",user.path("id").asText());
        db.patch("invitations",invitation.path("id").asText(),patch);
        ObjectNode result=db.object();result.put("userId",user.path("id").asText());result.put("status","pending_email_verification");
        result.put("message","Cuenta creada. Pendiente de verificación de correo.");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    private void checkPassword(String password){if(!PASSWORD.matcher(password).matches())
        throw new ApiException(HttpStatus.BAD_REQUEST,"WEAK_PASSWORD","La contraseña requiere 10 caracteres, letras, números y @, %, & o $.");}
    private void checkEmail(String email){for(JsonNode u:db.all("users"))if(email.equalsIgnoreCase(u.path("email").asText()))
        throw new ApiException(HttpStatus.CONFLICT,"EMAIL_ALREADY_REGISTERED","El correo ya está registrado.");}
    private String numericUsername(){
        java.security.SecureRandom random=new java.security.SecureRandom();
        for(int attempts=0;attempts<20;attempts++){
            StringBuilder id=new StringBuilder();id.append(1+random.nextInt(9));for(int i=1;i<18;i++)id.append(random.nextInt(10));
            boolean duplicate=false;for(JsonNode u:db.all("users"))if(id.toString().equals(u.path("username").asText()))duplicate=true;
            if(!duplicate)return id.toString();
        }
        throw new ApiException(HttpStatus.CONFLICT,"USERNAME_GENERATION_FAILED","No fue posible generar un username único.");
    }
}
