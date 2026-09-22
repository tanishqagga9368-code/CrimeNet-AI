package criminal_network_intelligence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import criminal_network_intelligence.model.AuditLog;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    List<AuditLog> findByUsernameOrderByCreatedAtDesc(
            String username
    );

    List<AuditLog> findByCaseIdOrderByCreatedAtDesc(
            String caseId
    );
}