package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.Evidence;
import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.EvidenceRepository;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class EvidenceChainService {

    private final EvidenceRepository evidenceRepository;
    private final IntelligenceRecordRepository intelligenceRecordRepository;

    public EvidenceChainService(
            EvidenceRepository evidenceRepository,
            IntelligenceRecordRepository intelligenceRecordRepository
    ) {
        this.evidenceRepository = evidenceRepository;
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
    }

    /**
     * Build complete evidence intelligence chain for a case.
     */
    public Map<String, Object> buildCaseEvidenceChain(
            String caseId
    ) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        if (caseId == null || caseId.isBlank()) {
            result.put("success", false);
            result.put("message", "Case ID is required.");
            return result;
        }

        List<Evidence> evidenceList =
                evidenceRepository
                        .findByCaseIdOrderByUploadedAtDesc(caseId);

        List<IntelligenceRecord> intelligenceRecords =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(caseId);

        List<Map<String, Object>> evidenceItems =
                new ArrayList<>();

        int verifiedCount = 0;
        int violationCount = 0;

        for (Evidence evidence : evidenceList) {

            if (evidence == null) {
                continue;
            }

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put("evidenceId", evidence.getId());
            item.put(
                    "fileName",
                    evidence.getOriginalFileName()
            );
            item.put(
                    "evidenceType",
                    evidence.getEvidenceType()
            );
            item.put(
                    "contentType",
                    evidence.getContentType()
            );
            item.put(
                    "sizeBytes",
                    evidence.getSizeBytes()
            );
            item.put(
                    "sha256Hash",
                    evidence.getSha256Hash()
            );
            item.put(
                    "integrityStatus",
                    evidence.getIntegrityStatus()
            );
            item.put(
                    "uploadedBy",
                    evidence.getUploadedBy()
            );
            item.put(
                    "description",
                    evidence.getDescription()
            );
            item.put(
                    "uploadedAt",
                    evidence.getUploadedAt()
            );
            item.put(
                    "verifiedAt",
                    evidence.getVerifiedAt()
            );

            String integrity =
                    evidence.getIntegrityStatus();

            if ("VERIFIED".equalsIgnoreCase(integrity)) {
                verifiedCount++;
            }

            if (
                    "INTEGRITY_VIOLATION"
                            .equalsIgnoreCase(integrity)
            ) {
                violationCount++;
            }

            /*
             * Link intelligence records to this evidence
             * through the case.
             */
            List<Map<String, Object>> linkedRecords =
                    buildLinkedRecords(
                            intelligenceRecords,
                            evidence
                    );

            item.put(
                    "linkedIntelligence",
                    linkedRecords
            );

            item.put(
                    "linkedRecordCount",
                    linkedRecords.size()
            );

            evidenceItems.add(item);
        }

        List<Map<String, Object>> intelligenceItems =
                new ArrayList<>();

        for (
                IntelligenceRecord record :
                intelligenceRecords
        ) {

            if (record == null) {
                continue;
            }

            Map<String, Object> intelligence =
                    new LinkedHashMap<>();

            intelligence.put(
                    "recordId",
                    record.getId()
            );

            intelligence.put(
                    "recordType",
                    record.getRecordType()
            );

            intelligence.put(
                    "source",
                    record.getSource()
            );

            intelligence.put(
                    "entityName",
                    record.getEntityName()
            );

            intelligence.put(
                    "entityType",
                    record.getEntityType()
            );

            intelligence.put(
                    "relatedEntity",
                    record.getRelatedEntity()
            );

            intelligence.put(
                    "relationshipType",
                    record.getRelationshipType()
            );

            intelligence.put(
                    "location",
                    record.getLocation()
            );

            intelligence.put(
                    "eventDate",
                    record.getEventDate()
            );

            intelligence.put(
                    "description",
                    record.getDescription()
            );

            intelligence.put(
                    "confidence",
                    record.getConfidence()
            );

            intelligence.put(
                    "createdAt",
                    record.getCreatedAt()
            );

            intelligenceItems.add(intelligence);
        }

        result.put("success", true);
        result.put("caseId", caseId);

        result.put(
                "evidenceCount",
                evidenceItems.size()
        );

        result.put(
                "verifiedEvidenceCount",
                verifiedCount
        );

        result.put(
                "integrityViolationCount",
                violationCount
        );

        result.put(
                "intelligenceRecordCount",
                intelligenceItems.size()
        );

        result.put(
                "evidence",
                evidenceItems
        );

        result.put(
                "intelligence",
                intelligenceItems
        );

        result.put(
                "chainStatus",
                determineChainStatus(
                        evidenceItems.size(),
                        verifiedCount,
                        violationCount
                )
        );

        result.put(
                "integrityNote",
                "SHA-256 hashes provide tamper-evident "
                        + "integrity verification. Evidence content "
                        + "itself is not stored on the blockchain."
        );

        return result;
    }

    /**
     * Get evidence chain for every case.
     */
    public List<Map<String, Object>> getEvidenceSummary() {

        List<Evidence> evidenceList =
                evidenceRepository
                        .findAllByOrderByUploadedAtDesc();

        Map<String, Map<String, Object>> caseSummary =
                new LinkedHashMap<>();

        for (Evidence evidence : evidenceList) {

            if (evidence == null) {
                continue;
            }

            String caseId =
                    evidence.getCaseId();

            if (
                    caseId == null
                            || caseId.isBlank()
            ) {
                continue;
            }

            Map<String, Object> summary =
                    caseSummary.computeIfAbsent(
                            caseId,
                            key -> {
                                Map<String, Object> map =
                                        new LinkedHashMap<>();

                                map.put("caseId", key);
                                map.put("evidenceCount", 0);
                                map.put("verifiedCount", 0);
                                map.put(
                                        "integrityViolationCount",
                                        0
                                );

                                return map;
                            }
                    );

            int evidenceCount =
                    getInteger(
                            summary.get("evidenceCount")
                    );

            summary.put(
                    "evidenceCount",
                    evidenceCount + 1
            );

            if (
                    "VERIFIED".equalsIgnoreCase(
                            evidence.getIntegrityStatus()
                    )
            ) {

                int verifiedCount =
                        getInteger(
                                summary.get("verifiedCount")
                        );

                summary.put(
                        "verifiedCount",
                        verifiedCount + 1
                );
            }

            if (
                    "INTEGRITY_VIOLATION"
                            .equalsIgnoreCase(
                                    evidence.getIntegrityStatus()
                            )
            ) {

                int violationCount =
                        getInteger(
                                summary.get(
                                        "integrityViolationCount"
                                )
                        );

                summary.put(
                        "integrityViolationCount",
                        violationCount + 1
                );
            }
        }

        return new ArrayList<>(
                caseSummary.values()
        );
    }

    /**
     * Link intelligence records to evidence.
     *
     * Since both belong to the same case, they form
     * an investigation chain.
     */
    private List<Map<String, Object>> buildLinkedRecords(
            List<IntelligenceRecord> records,
            Evidence evidence
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (records == null || evidence == null) {
            return result;
        }

        for (IntelligenceRecord record : records) {

            if (record == null) {
                continue;
            }

            Map<String, Object> linked =
                    new LinkedHashMap<>();

            linked.put(
                    "recordId",
                    record.getId()
            );

            linked.put(
                    "recordType",
                    record.getRecordType()
            );

            linked.put(
                    "entityName",
                    record.getEntityName()
            );

            linked.put(
                    "entityType",
                    record.getEntityType()
            );

            linked.put(
                    "relatedEntity",
                    record.getRelatedEntity()
            );

            linked.put(
                    "relationshipType",
                    record.getRelationshipType()
            );

            linked.put(
                    "location",
                    record.getLocation()
            );

            linked.put(
                    "eventDate",
                    record.getEventDate()
            );

            linked.put(
                    "confidence",
                    record.getConfidence()
            );

            result.add(linked);
        }

        return result;
    }

    /**
     * Determine overall evidence-chain status.
     */
    private String determineChainStatus(
            int evidenceCount,
            int verifiedCount,
            int violationCount
    ) {

        if (violationCount > 0) {
            return "INTEGRITY_REVIEW_REQUIRED";
        }

        if (evidenceCount == 0) {
            return "NO_EVIDENCE";
        }

        if (verifiedCount == evidenceCount) {
            return "VERIFIED";
        }

        return "PARTIALLY_VERIFIED";
    }

    /**
     * Safe integer conversion.
     */
    private int getInteger(Object value) {

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(
                    value.toString()
            );
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}