package criminal_network_intelligence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import criminal_network_intelligence.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findFirstByUsernameIgnoreCase(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);
}