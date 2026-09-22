package criminal_network_intelligence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import criminal_network_intelligence.model.Alert;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByTimestampDesc();

    List<Alert> findByCaseIdOrderByTimestampDesc(String caseId);

    List<Alert> findBySeverityOrderByTimestampDesc(String severity);

    long countByIsReadFalse();
}
