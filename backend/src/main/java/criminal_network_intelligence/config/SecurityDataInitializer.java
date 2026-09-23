package criminal_network_intelligence.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import criminal_network_intelligence.model.AppUser;
import criminal_network_intelligence.repository.AppUserRepository;

@Component
@Order(1)
public class SecurityDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SecurityDataInitializer.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityDataInitializer(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Verifying and seeding required system users...");

        // Ensure Administrator exists
        upsertUser(
                "admin",
                "admin123",
                "System Administrator",
                "ADMIN"
        );

        // Ensure Investigating Officer exists
        upsertUser(
                "officer",
                "officer123",
                "Investigating Officer",
                "OFFICER"
        );

        // Ensure Senior Officer exists (used by frontend Senior Officer persona)
        upsertUser(
                "senior_officer",
                "officer123",
                "Senior Superintendent Verma",
                "SENIOR_OFFICER"
        );

        // Ensure Investigator alias exists (used by alternative login chips)
        upsertUser(
                "investigator",
                "officer123",
                "Investigating Officer Sharma",
                "OFFICER"
        );

        // Ensure Analyst exists
        upsertUser(
                "analyst",
                "analyst123",
                "Intelligence Analyst",
                "ANALYST"
        );

        log.info("System users verification complete. Total users in database: {}", userRepository.count());
    }

    private void upsertUser(
            String username,
            String rawPassword,
            String fullName,
            String role
    ) {
        try {
            Optional<AppUser> existing = userRepository.findByUsernameIgnoreCase(username);

            if (existing.isEmpty()) {
                AppUser user = new AppUser();
                user.setUsername(username.trim().toLowerCase());
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setFullName(fullName);
                user.setRole(role.toUpperCase());
                user.setEnabled(true);
                userRepository.save(user);
                log.info("Created system user: {} with role: {}", username, role);
            } else {
                AppUser user = existing.get();
                boolean needsUpdate = false;

                // If stored password does not match expected password, re-encode with BCrypt
                if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                    log.warn("Password mismatch for existing user: {}. Synchronizing password hash.", username);
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    needsUpdate = true;
                }

                if (!user.isEnabled()) {
                    user.setEnabled(true);
                    needsUpdate = true;
                }

                if (!role.equalsIgnoreCase(user.getRole())) {
                    user.setRole(role.toUpperCase());
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    userRepository.save(user);
                    log.info("Updated existing system user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Error ensuring system user {}: {}", username, e.getMessage(), e);
        }
    }
}
