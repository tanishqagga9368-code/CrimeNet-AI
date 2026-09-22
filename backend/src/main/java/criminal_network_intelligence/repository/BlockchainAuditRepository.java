package criminal_network_intelligence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import criminal_network_intelligence.model.BlockchainAuditBlock;

public interface BlockchainAuditRepository
        extends JpaRepository<BlockchainAuditBlock, Long> {

    Optional<BlockchainAuditBlock>
    findTopByOrderByIdDesc();

    List<BlockchainAuditBlock>
    findByCaseIdOrderByIdAsc(String caseId);

    List<BlockchainAuditBlock>
    findByEvidenceIdOrderByIdAsc(Long evidenceId);

    List<BlockchainAuditBlock>
    findAllByOrderByIdAsc();
}