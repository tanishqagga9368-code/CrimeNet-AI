package criminal_network_intelligence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import criminal_network_intelligence.model.Evidence;

public interface EvidenceRepository
        extends JpaRepository<Evidence, Long> {

    List<Evidence> findByCaseIdOrderByUploadedAtDesc(String caseId);

    List<Evidence> findAllByOrderByUploadedAtDesc();
}