package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.model.AuditLog;
import criminal_network_intelligence.repository.AuditLogRepository;
import java.time.LocalDateTime;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class EntityResolutionService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;
    private final AuditLogRepository auditLogRepository;

    public EntityResolutionService(
            IntelligenceRecordRepository intelligenceRecordRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.intelligenceRecordRepository = intelligenceRecordRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public Map<String, Object> resolveEntities() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> matches = findMatches(records);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("success", true);
        response.put("totalRecords", records.size());
        response.put("totalPotentialMatches", matches.size());
        response.put("matches", matches);
        response.put(
                "interpretation",
                "Entity resolution identifies potential duplicate or matching "
                        + "records for investigation support. A match does not "
                        + "confirm that two records belong to the same real-world person."
        );

        return response;
    }

    public List<Map<String, Object>> resolveCase(String caseId) {

        if (caseId == null || caseId.isBlank()) {
            return new ArrayList<>();
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(caseId);

        return findMatches(records);
    }

    public List<Map<String, Object>> resolveEntity(String entityName) {

        List<Map<String, Object>> matches = new ArrayList<>();

        if (entityName == null || entityName.isBlank()) {
            return matches;
        }

        String requestedEntity = normalize(entityName);

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        for (IntelligenceRecord record : records) {

            String recordEntity =
                    safe(record.getEntityName());

            if (recordEntity.isBlank()) {
                continue;
            }

            double score =
                    similarity(
                            requestedEntity,
                            recordEntity
                    );

            if (score < 0.70) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("recordId", record.getId());
            item.put("caseId", record.getCaseId());
            item.put("entityName", recordEntity);
            item.put("relatedEntity", record.getRelatedEntity());
            item.put("entityType", record.getEntityType());
            item.put("matchScore", round(score));
            item.put("matchLevel", matchLevel(score));
            item.put(
                    "reason",
                    buildReason(
                            entityName,
                            recordEntity,
                            score
                    )
            );

            matches.add(item);
        }

        matches.sort(
                Comparator.comparingDouble(
                        (Map<String, Object> item) ->
                                getDouble(item.get("matchScore"))
                ).reversed()
        );

        return matches;
    }

    private List<Map<String, Object>> findMatches(
            List<IntelligenceRecord> records
    ) {

        List<Map<String, Object>> matches = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {

            for (int j = i + 1; j < records.size(); j++) {

                Map<String, Object> match =
                        compareRecords(
                                records.get(i),
                                records.get(j)
                        );

                if (match != null) {
                    matches.add(match);
                }
            }
        }

        matches.sort(
                Comparator.comparingDouble(
                        (Map<String, Object> item) ->
                                getDouble(item.get("matchScore"))
                ).reversed()
        );

        return matches;
    }

    private Map<String, Object> compareRecords(
            IntelligenceRecord first,
            IntelligenceRecord second
    ) {

        String firstName =
                safe(first.getEntityName());

        String secondName =
                safe(second.getEntityName());

        if (firstName.isBlank()
                || secondName.isBlank()) {
            return null;
        }

        double nameScore =
                similarity(
                        firstName,
                        secondName
                );

        double phoneScore =
                exactSimilarity(
                        extractPhone(first),
                        extractPhone(second)
                );

        double locationScore =
                exactSimilarity(
                        first.getLocation(),
                        second.getLocation()
                );

        double relationshipScore =
                exactSimilarity(
                        first.getRelationshipType(),
                        second.getRelationshipType()
                );

        double bestScore =
                Math.max(
                        nameScore,
                        Math.max(
                                phoneScore,
                                locationScore
                        )
                );

        if (nameScore >= 0.90) {
            bestScore = Math.max(
                    bestScore,
                    0.95
            );
        }

        if (phoneScore == 1.0) {
            bestScore = 0.99;
        }

        if (bestScore < 0.70) {
            return null;
        }

        Map<String, Object> match =
                new LinkedHashMap<>();

        match.put(
                "firstRecordId",
                first.getId()
        );

        match.put(
                "secondRecordId",
                second.getId()
        );

        match.put(
                "firstCaseId",
                first.getCaseId()
        );

        match.put(
                "secondCaseId",
                second.getCaseId()
        );

        match.put(
                "firstEntity",
                firstName
        );

        match.put(
                "secondEntity",
                secondName
        );

        match.put(
                "entityType",
                first.getEntityType()
        );

        match.put(
                "matchScore",
                round(bestScore)
        );

        match.put(
                "matchLevel",
                matchLevel(bestScore)
        );

        match.put(
                "nameSimilarity",
                round(nameScore)
        );

        match.put(
                "phoneMatch",
                phoneScore == 1.0
        );

        match.put(
                "locationMatch",
                locationScore == 1.0
        );

        match.put(
                "relationshipMatch",
                relationshipScore == 1.0
        );

        match.put(
                "reason",
                buildMatchReason(
                        nameScore,
                        phoneScore,
                        locationScore,
                        relationshipScore
                )
        );

        match.put(
                "requiresReview",
                true
        );

        return match;
    }

    private String extractPhone(
            IntelligenceRecord record
    ) {

        String text =
                safe(record.getDescription())
                        + " "
                        + safe(record.getEntityName())
                        + " "
                        + safe(record.getRelatedEntity());

        String digits =
                text.replaceAll("[^0-9]", "");

        if (digits.length() >= 10) {
            return digits.substring(
                    digits.length() - 10
            );
        }

        return "";
    }

    private double similarity(
            String first,
            String second
    ) {

        String a = normalize(first);
        String b = normalize(second);

        if (a.isBlank() || b.isBlank()) {
            return 0.0;
        }

        if (a.equals(b)) {
            return 1.0;
        }

        if (a.contains(b) || b.contains(a)) {
            return 0.90;
        }

        String[] firstWords = a.split(" ");
        String[] secondWords = b.split(" ");

        int commonWords = 0;

        for (String firstWord : firstWords) {

            if (firstWord.length() < 2) {
                continue;
            }

            for (String secondWord : secondWords) {

                if (firstWord.equals(secondWord)) {
                    commonWords++;
                    break;
                }
            }
        }

        int totalWords =
                Math.max(
                        firstWords.length,
                        secondWords.length
                );

        double wordScore =
                totalWords == 0
                        ? 0.0
                        : (double) commonWords / totalWords;

        int distance =
                levenshteinDistance(a, b);

        int maxLength =
                Math.max(
                        a.length(),
                        b.length()
                );

        double editScore =
                maxLength == 0
                        ? 0.0
                        : 1.0
                        - ((double) distance / maxLength);

        return Math.max(
                wordScore,
                editScore
        );
    }

    private double exactSimilarity(
            String first,
            String second
    ) {

        String a = normalize(first);
        String b = normalize(second);

        if (a.isBlank() || b.isBlank()) {
            return 0.0;
        }

        return a.equals(b) ? 1.0 : 0.0;
    }

    private String normalize(String value) {

        return safe(value)
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9 ]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private int levenshteinDistance(
            String a,
            String b
    ) {

        int[] previous =
                new int[b.length() + 1];

        int[] current =
                new int[b.length() + 1];

        for (int j = 0;
             j <= b.length();
             j++) {

            previous[j] = j;
        }

        for (int i = 1;
             i <= a.length();
             i++) {

            current[0] = i;

            for (int j = 1;
                 j <= b.length();
                 j++) {

                int cost =
                        a.charAt(i - 1)
                                == b.charAt(j - 1)
                                ? 0
                                : 1;

                current[j] =
                        Math.min(
                                Math.min(
                                        current[j - 1] + 1,
                                        previous[j] + 1
                                ),
                                previous[j - 1] + cost
                        );
            }

            int[] temp = previous;
            previous = current;
            current = temp;
        }

        return previous[b.length()];
    }

    private String matchLevel(double score) {

        if (score >= 0.90) {
            return "HIGH";
        }

        if (score >= 0.80) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String buildReason(
            String requested,
            String matched,
            double score
    ) {

        if (normalize(requested)
                .equals(normalize(matched))) {

            return "Exact normalized entity-name match.";
        }

        if (score >= 0.90) {
            return "Very high similarity between entity names.";
        }

        return "Potential entity-name similarity requires investigator review.";
    }

    private String buildMatchReason(
            double nameScore,
            double phoneScore,
            double locationScore,
            double relationshipScore
    ) {

        List<String> reasons = new ArrayList<>();

        if (nameScore >= 0.90) {
            reasons.add("high name similarity");
        }

        if (phoneScore == 1.0) {
            reasons.add("same phone identifier");
        }

        if (locationScore == 1.0) {
            reasons.add("same location");
        }

        if (relationshipScore == 1.0) {
            reasons.add("same relationship type");
        }

        if (reasons.isEmpty()) {
            return "Potential similarity detected.";
        }

        return String.join(
                ", ",
                reasons
        ) + ".";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double getDouble(Object value) {

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(
                    value.toString()
            );
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }
    public Map<String, Object> mergeEntities(String primaryEntity, String duplicateEntity, String notes, String officer) {
        String actor = (officer != null && !officer.isBlank()) ? officer : "Investigating Officer";
        String memo = (notes != null && !notes.isBlank()) ? notes : "Confirmed matching identity via biometric/telecom cross-reference";

        List<IntelligenceRecord> records = intelligenceRecordRepository.findAll();
        int affected = 0;
        for (IntelligenceRecord r : records) {
            boolean changed = false;
            if (duplicateEntity.equalsIgnoreCase(r.getEntityName())) {
                r.setEntityName(primaryEntity);
                changed = true;
            }
            if (duplicateEntity.equalsIgnoreCase(r.getRelatedEntity())) {
                r.setRelatedEntity(primaryEntity);
                changed = true;
            }
            if (changed) {
                intelligenceRecordRepository.save(r);
                affected++;
            }
        }

        AuditLog log = new AuditLog(
                actor,
                "INVESTIGATOR",
                "ENTITY_MERGE",
                "INTELLIGENCE_RECORD",
                duplicateEntity,
                "GLOBAL",
                "Merged duplicate entity [" + duplicateEntity + "] into primary entity [" + primaryEntity + "]. Reason: " + memo,
                "127.0.0.1"
        );
        auditLogRepository.save(log);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("message", "Entities merged successfully. " + affected + " intelligence record(s) consolidated.");
        res.put("primaryEntity", primaryEntity);
        res.put("mergedEntity", duplicateEntity);
        res.put("affectedRecords", affected);
        res.put("timestamp", LocalDateTime.now().toString());
        return res;
    }

}
