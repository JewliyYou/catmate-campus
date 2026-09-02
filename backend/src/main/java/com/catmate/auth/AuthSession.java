package com.catmate.auth;

import com.catmate.user.UserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_session", indexes = @Index(name = "idx_auth_token", columnList = "token", unique = true))
public class AuthSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 64) private String token;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "user_id") private UserAccount user;
    @Column(nullable = false) private LocalDateTime expiresAt;
    public Long getId() { return id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
