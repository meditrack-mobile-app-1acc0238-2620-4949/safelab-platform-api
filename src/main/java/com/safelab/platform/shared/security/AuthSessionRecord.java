package com.safelab.platform.shared.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Una sesion por token. El valor almacenado es el SHA-256 del bearer token. */
@Entity
@Table(name = "auth_sessions")
public class AuthSessionRecord {
    @Id
    @Column(name = "token_hash", columnDefinition = "text")
    public String tokenHash;

    @Column(name = "user_id", columnDefinition = "text", nullable = false)
    public String userId;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;
}
