package criminal_network_intelligence.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.model.AppUser;
import criminal_network_intelligence.security.AppUserService;
import criminal_network_intelligence.security.AuthResponse;
import criminal_network_intelligence.security.JwtService;
import criminal_network_intelligence.security.LoginRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final AppUserService appUserService;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            AppUserService appUserService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.appUserService = appUserService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {

        String username = request.getUsername() != null ? request.getUsername().trim() : "";

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    request.getPassword()
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            AppUser user =
                    appUserService.getUser(
                            username
                    );

            String token =
                    jwtService.generateToken(user);

            AuthResponse response =
                    new AuthResponse(
                            token,
                            user.getUsername(),
                            user.getFullName(),
                            user.getRole(),
                            jwtService.getExpiration()
                    );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException exception) {
            log.warn("Authentication failed for user '{}': {}", username, exception.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid username or password"
                    ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Authentication required"
                    ));
        }

        AppUser user =
                appUserService.getUser(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "username", user.getUsername(),
                        "fullName", user.getFullName(),
                        "role", user.getRole()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Logged out successfully"
                )
        );
    }
}