package criminal_network_intelligence.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import criminal_network_intelligence.model.AppUser;
import criminal_network_intelligence.repository.AppUserRepository;

@Component
public class SecurityDataInitializer implements CommandLineRunner {

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

        if (userRepository.count() > 0) {
            return;
        }

        createUser(
                "admin",
                "admin123",
                "System Administrator",
                "ADMIN"
        );

        createUser(
                "officer",
                "officer123",
                "Investigating Officer",
                "OFFICER"
        );

        createUser(
                "analyst",
                "analyst123",
                "Intelligence Analyst",
                "ANALYST"
        );
    }

    private void createUser(
            String username,
            String password,
            String fullName,
            String role
    ) {

        AppUser user = new AppUser();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setEnabled(true);

        userRepository.save(user);
    }
}
