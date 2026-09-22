package criminal_network_intelligence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import criminal_network_intelligence.model.Case;

public interface CaseRepository extends JpaRepository<Case, String> {
}