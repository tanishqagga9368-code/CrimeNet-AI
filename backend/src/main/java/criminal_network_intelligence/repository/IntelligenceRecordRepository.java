package criminal_network_intelligence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import criminal_network_intelligence.model.IntelligenceRecord;

public interface IntelligenceRecordRepository
        extends JpaRepository<IntelligenceRecord, Long> {

    List<IntelligenceRecord> findByCaseId(String caseId);

    List<IntelligenceRecord> findBySourceType(String sourceType);

    List<IntelligenceRecord> findAllByOrderByCreatedAtDesc();

    List<IntelligenceRecord> findByCaseIdOrderByCreatedAtDesc(String caseId);

    List<IntelligenceRecord> findByEntityNameIgnoreCase(String entityName);

    List<IntelligenceRecord> findByRecordTypeIgnoreCase(String recordType);

    List<IntelligenceRecord> findByRelationshipTypeIgnoreCase(String relationshipType);

    List<IntelligenceRecord> findByRecordTypeOrderByCreatedAtDesc(String recordType);
}