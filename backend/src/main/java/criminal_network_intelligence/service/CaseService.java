package criminal_network_intelligence.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import criminal_network_intelligence.model.Case;
import criminal_network_intelligence.repository.CaseRepository;

@Service
@Transactional
public class CaseService {

    private final CaseRepository caseRepository;

    public CaseService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    @Transactional(readOnly = true)
    public List<Case> getAllCases() {
        return caseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Case getCaseById(String caseId) {

        return caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException("Case not found: " + caseId));
    }

    public Case createCase(Case caseData) {

        if (caseData == null) {
            throw new IllegalArgumentException(
                    "Case data cannot be null");
        }

        if (caseData.getCaseId() == null ||
                caseData.getCaseId().isBlank()) {

            throw new IllegalArgumentException(
                    "Case ID is required");
        }

        if (caseRepository.existsById(caseData.getCaseId())) {

            throw new IllegalArgumentException(
                    "Case already exists: " + caseData.getCaseId());
        }

        if (caseData.getCreatedAt() == null || caseData.getCreatedAt().isBlank()) {
            caseData.setCreatedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return caseRepository.save(caseData);
    }

    public Case updateCase(
            String caseId,
            Case updatedCase) {

        if (updatedCase == null) {
            throw new IllegalArgumentException(
                    "Updated case data cannot be null");
        }

        Case existingCase = getCaseById(caseId);

        if (updatedCase.getTitle() != null) {
            existingCase.setTitle(
                    updatedCase.getTitle());
        }

        if (updatedCase.getDescription() != null) {
            existingCase.setDescription(
                    updatedCase.getDescription());
        }

        if (updatedCase.getRiskLevel() != null) {
            existingCase.setRiskLevel(
                    updatedCase.getRiskLevel());
        }

        if (updatedCase.getEntities() > 0) {
            existingCase.setEntities(
                    updatedCase.getEntities());
        }

        if (updatedCase.getStatus() != null) {
            existingCase.setStatus(
                    updatedCase.getStatus());
        }

        if (updatedCase.getInvestigatingOfficer() != null) {
            existingCase.setInvestigatingOfficer(
                    updatedCase.getInvestigatingOfficer());
        }

        if (updatedCase.getCreatedAt() != null) {
            existingCase.setCreatedAt(
                    updatedCase.getCreatedAt());
        }

        if (updatedCase.getUpdatedAt() != null) {
            existingCase.setUpdatedAt(
                    updatedCase.getUpdatedAt());
        }

        if (updatedCase.getLocation() != null) {
            existingCase.setLocation(
                    updatedCase.getLocation());
        }

        return caseRepository.save(existingCase);
    }

    public boolean deleteCase(String caseId) {

        if (!caseRepository.existsById(caseId)) {
            return false;
        }

        caseRepository.deleteById(caseId);

        return true;
    }

    @Transactional(readOnly = true)
    public long getActiveCaseCount() {

        return caseRepository.findAll()
                .stream()
                .filter(caseItem ->
                        caseItem.getStatus() != null &&
                        (
                                caseItem.getStatus()
                                        .equalsIgnoreCase("ACTIVE")
                                ||
                                caseItem.getStatus()
                                        .equalsIgnoreCase("INVESTIGATING")
                                ||
                                caseItem.getStatus()
                                        .equalsIgnoreCase("OPEN")
                        )
                )
                .count();
    }
}