package com.catmate.auth;

import com.catmate.common.ApiException;
import com.catmate.user.UserAccount;
import com.catmate.user.UserAccountRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAccountRepository users;
    private final AuthSessionRepository sessions;
    private final PasswordService passwords;
    private final long sessionHours;

    public AuthController(UserAccountRepository users, AuthSessionRepository sessions, PasswordService passwords, @Value("${catmate.session-hours:24}") long sessionHours) {
        this.users = users; this.sessions = sessions; this.passwords = passwords; this.sessionHours = sessionHours;
    }

    @PostMapping("/login")
    public Map<String,Object> login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = users.findByUsername(request.username()).filter(UserAccount::isEnabled).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "账号或密码错误"));
        if (!passwords.matches(request.password(), user.getPasswordHash())) throw new ApiException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        return createSession(user);
    }

    @PostMapping("/register")
    @Transactional
    public Map<String,Object> register(@Valid @RequestBody RegisterRequest request) {
        String username = request.username().trim();
        if (users.findByUsername(username).isPresent()) throw new ApiException(HttpStatus.CONFLICT, "该账号已被注册");
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwords.hash(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setRole(UserAccount.Role.USER);
        users.save(user);
        return createSession(user);
    }

    private Map<String,Object> createSession(UserAccount user) {
        AuthSession session = new AuthSession(); session.setToken(UUID.randomUUID().toString().replace("-", "")); session.setUser(user); session.setExpiresAt(LocalDateTime.now().plusHours(sessionHours)); sessions.save(session);
        return Map.of("token", session.getToken(), "username", user.getUsername(), "name", user.getDisplayName(), "role", user.getRole().name());
    }

    @GetMapping("/me")
    public Map<String,Object> me(@RequestAttribute("currentUser") UserAccount user) {
        return Map.of("username", user.getUsername(), "name", user.getDisplayName(), "role", user.getRole().name());
    }

    @PostMapping("/logout") @Transactional
    public void logout(@RequestHeader(value="Authorization", required=false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) sessions.deleteByToken(authorization.substring(7));
    }

    public record LoginRequest(@NotBlank(message="请输入账号") String username, @NotBlank(message="请输入密码") String password) {}
    public record RegisterRequest(
        @NotBlank(message="请输入账号")
        @Pattern(regexp="^[A-Za-z0-9_]{3,20}$", message="账号须为3至20位字母、数字或下划线") String username,
        @NotBlank(message="请输入昵称")
        @Size(max=20, message="昵称不能超过20个字符") String displayName,
        @NotBlank(message="请输入密码")
        @Size(min=6, max=50, message="密码须为6至50个字符") String password
    ) {}
}
