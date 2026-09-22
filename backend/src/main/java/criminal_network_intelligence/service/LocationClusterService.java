package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class LocationClusterService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;

    public LocationClusterService(
            IntelligenceRecordRepository intelligenceRecordRepository
    ) {
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
    }

    /**
     * Returns location clusters across all intelligence records.
     */
    public List<Map<String, Object>> getLocationClusters() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        return buildLocationClusters(records);
    }

    /**
     * Returns location clusters for a particular case.
     */
    public List<Map<String, Object>> getCaseLocationClusters(
            String caseId
    ) {

        if (caseId == null || caseId.isBlank()) {
            return new ArrayList<>();
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(caseId);

        return buildLocationClusters(records);
    }

    /**
     * Returns detailed intelligence for one location.
     */
    public Map<String, Object> analyzeLocation(
            String location
    ) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        if (location == null || location.isBlank()) {

            result.put("success", false);
            result.put(
                    "message",
                    "Location is required."
            );

            return result;
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        String searchLocation =
                location.trim().toLowerCase();

        Set<String> entities =
                new LinkedHashSet<>();

        Set<String> cases =
                new LinkedHashSet<>();

        Set<String> recordTypes =
                new LinkedHashSet<>();

        List<Map<String, Object>> activity =
                new ArrayList<>();

        for (IntelligenceRecord record : records) {

            String recordLocation =
                    record.getLocation();

            if (recordLocation == null
                    || recordLocation.isBlank()) {
                continue;
            }

            if (!recordLocation
                    .toLowerCase()
                    .contains(searchLocation)) {
                continue;
            }

            if (record.getEntityName() != null
                    && !record.getEntityName().isBlank()) {

                entities.add(
                        record.getEntityName()
                );
            }

            if (record.getRelatedEntity() != null
                    && !record.getRelatedEntity().isBlank()) {

                entities.add(
                        record.getRelatedEntity()
                );
            }

            if (record.getCaseId() != null
                    && !record.getCaseId().isBlank()) {

                cases.add(
                        record.getCaseId()
                );
            }

            if (record.getRecordType() != null
                    && !record.getRecordType().isBlank()) {

                recordTypes.add(
                        record.getRecordType()
                );
            }

            Map<String, Object> event =
                    new LinkedHashMap<>();

            event.put(
                    "recordId",
                    record.getId()
            );

            event.put(
                    "caseId",
                    record.getCaseId()
            );

            event.put(
                    "entityName",
                    record.getEntityName()
            );

            event.put(
                    "relatedEntity",
                    record.getRelatedEntity()
            );

            event.put(
                    "relationshipType",
                    record.getRelationshipType()
            );

            event.put(
                    "recordType",
                    record.getRecordType()
            );

            event.put(
                    "location",
                    record.getLocation()
            );

            event.put(
                    "eventDate",
                    record.getEventDate()
            );

            event.put(
                    "description",
                    record.getDescription()
            );

            event.put(
                    "importance",
                    calculateImportance(record)
            );

            activity.add(event);
        }

        activity.sort(
                Comparator.comparing(
                        item -> safe(
                                item.get("eventDate")
                        ),
                        Comparator.reverseOrder()
                )
        );

        int activityCount =
                activity.size();

        int riskScore =
                calculateLocationRisk(
                        activityCount,
                        entities.size(),
                        cases.size()
                );

        result.put(
                "success",
                true
        );

        result.put(
                "location",
                location
        );

        result.put(
                "activityCount",
                activityCount
        );

        result.put(
                "uniqueEntities",
                entities.size()
        );

        result.put(
                "relatedCases",
                cases.size()
        );

        result.put(
                "recordTypes",
                recordTypes
        );

        result.put(
                "entities",
                new ArrayList<>(entities)
        );

        result.put(
                "cases",
                new ArrayList<>(cases)
        );

        result.put(
                "riskScore",
                riskScore
        );

        result.put(
                "riskLevel",
                getRiskLevel(riskScore)
        );

        result.put(
                "investigationLead",
                riskScore >= 70
                        ? "HIGH"
                        : riskScore >= 40
                        ? "MEDIUM"
                        : "LOW"
        );

        result.put(
                "activity",
                activity
        );

        result.put(
                "interpretation",
                "Location activity is an investigation-support indicator. Shared or frequently observed locations do not by themselves establish criminal involvement."
        );

        return result;
    }

    /**
     * Finds locations shared by multiple entities or cases.
     */
    public List<Map<String, Object>> getSharedLocations() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        Map<String, Set<String>> locationEntities =
                new TreeMap<>(
                        String.CASE_INSENSITIVE_ORDER
                );

        Map<String, Set<String>> locationCases =
                new TreeMap<>(
                        String.CASE_INSENSITIVE_ORDER
                );

        for (IntelligenceRecord record : records) {

            String location =
                    record.getLocation();

            if (location == null
                    || location.isBlank()) {
                continue;
            }

            String normalized =
                    location.trim();

            locationEntities
                    .computeIfAbsent(
                            normalized,
                            key -> new LinkedHashSet<>()
                    );

            locationCases
                    .computeIfAbsent(
                            normalized,
                            key -> new LinkedHashSet<>()
                    );

            if (record.getEntityName() != null
                    && !record.getEntityName().isBlank()) {

                locationEntities
                        .get(normalized)
                        .add(record.getEntityName());
            }

            if (record.getRelatedEntity() != null
                    && !record.getRelatedEntity().isBlank()) {

                locationEntities
                        .get(normalized)
                        .add(record.getRelatedEntity());
            }

            if (record.getCaseId() != null
                    && !record.getCaseId().isBlank()) {

                locationCases
                        .get(normalized)
                        .add(record.getCaseId());
            }
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (String location :
                locationEntities.keySet()) {

            Set<String> entities =
                    locationEntities.get(location);

            Set<String> cases =
                    locationCases.get(location);

            if (entities.size() < 2
                    && cases.size() < 2) {
                continue;
            }

            int score =
                    Math.min(
                            100,
                            entities.size() * 15
                                    + cases.size() * 10
                    );

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "location",
                    location
            );

            item.put(
                    "uniqueEntities",
                    entities.size()
            );

            item.put(
                    "relatedCases",
                    cases.size()
            );

            item.put(
                    "entities",
                    new ArrayList<>(entities)
            );

            item.put(
                    "cases",
                    new ArrayList<>(cases)
            );

            item.put(
                    "connectionScore",
                    score
            );

            item.put(
                    "investigationLead",
                    score >= 70
                            ? "HIGH"
                            : score >= 40
                            ? "MEDIUM"
                            : "LOW"
            );

            item.put(
                    "explanation",
                    "Multiple entities or cases share this location. Review the underlying events, dates and relationships before drawing conclusions."
            );

            item.put(
                    "interpretation",
                    "A shared location indicates a potential association only and is not proof of criminal involvement."
            );

            result.add(item);
        }

        result.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(
                                        item.get(
                                                "connectionScore"
                                        )
                                )
                ).reversed()
        );

        return result;
    }

    private List<Map<String, Object>> buildLocationClusters(
            List<IntelligenceRecord> records
    ) {

        Map<String, List<IntelligenceRecord>> grouped =
                new TreeMap<>(
                        String.CASE_INSENSITIVE_ORDER
                );

        for (IntelligenceRecord record : records) {

            String location =
                    record.getLocation();

            if (location == null
                    || location.isBlank()) {
                continue;
            }

            grouped
                    .computeIfAbsent(
                            location.trim(),
                            key -> new ArrayList<>()
                    )
                    .add(record);
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map.Entry<
                String,
                List<IntelligenceRecord>
                > entry : grouped.entrySet()) {

            String location =
                    entry.getKey();

            List<IntelligenceRecord> locationRecords =
                    entry.getValue();

            Set<String> entities =
                    new LinkedHashSet<>();

            Set<String> cases =
                    new LinkedHashSet<>();

            Set<String> recordTypes =
                    new LinkedHashSet<>();

            int highImportance = 0;

            int relationshipEvents = 0;

            for (IntelligenceRecord record :
                    locationRecords) {

                if (record.getEntityName() != null
                        && !record.getEntityName().isBlank()) {

                    entities.add(
                            record.getEntityName()
                    );
                }

                if (record.getRelatedEntity() != null
                        && !record.getRelatedEntity().isBlank()) {

                    entities.add(
                            record.getRelatedEntity()
                    );
                }

                if (record.getCaseId() != null
                        && !record.getCaseId().isBlank()) {

                    cases.add(
                            record.getCaseId()
                    );
                }

                if (record.getRecordType() != null
                        && !record.getRecordType().isBlank()) {

                    recordTypes.add(
                            record.getRecordType()
                    );
                }

                if (record.getRelationshipType() != null
                        && !record.getRelationshipType().isBlank()) {

                    relationshipEvents++;
                }

                if ("HIGH".equals(
                        calculateImportance(record)
                )) {
                    highImportance++;
                }
            }

            int activityCount =
                    locationRecords.size();

            int riskScore =
                    calculateLocationRisk(
                            activityCount,
                            entities.size(),
                            cases.size()
                    );

            riskScore =
                    Math.min(
                            100,
                            riskScore + highImportance * 5
                    );

            Map<String, Object> cluster =
                    new LinkedHashMap<>();

            cluster.put(
                    "location",
                    location
            );

            cluster.put(
                    "activityCount",
                    activityCount
            );

            cluster.put(
                    "uniqueEntities",
                    entities.size()
            );

            cluster.put(
                    "relatedCases",
                    cases.size()
            );

            cluster.put(
                    "relationshipEvents",
                    relationshipEvents
            );

            cluster.put(
                    "highImportanceEvents",
                    highImportance
            );

            cluster.put(
                    "recordTypes",
                    new ArrayList<>(recordTypes)
            );

            cluster.put(
                    "entities",
                    new ArrayList<>(entities)
            );

            cluster.put(
                    "cases",
                    new ArrayList<>(cases)
            );

            cluster.put(
                    "riskScore",
                    riskScore
            );

            cluster.put(
                    "riskLevel",
                    getRiskLevel(riskScore)
            );

            cluster.put(
                    "investigationLead",
                    riskScore >= 70
                            ? "HIGH"
                            : riskScore >= 40
                            ? "MEDIUM"
                            : "LOW"
            );

            cluster.put(
                    "explanation",
                    buildClusterExplanation(
                            activityCount,
                            entities.size(),
                            cases.size(),
                            relationshipEvents
                    )
            );

            cluster.put(
                    "interpretation",
                    "Location clustering identifies patterns for investigation. It does not establish guilt or criminal activity."
            );

            result.add(cluster);
        }

        result.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(
                                        item.get("riskScore")
                                )
                ).reversed()
        );

        return result;
    }

    private String buildClusterExplanation(
            int activities,
            int entities,
            int cases,
            int relationships
    ) {

        if (cases >= 2 && entities >= 3) {
            return "This location appears across multiple cases and involves multiple entities, making it a useful investigation lead.";
        }

        if (relationships >= 3) {
            return "Multiple relationship events were recorded at this location.";
        }

        if (entities >= 3) {
            return "Multiple entities are associated with intelligence activity at this location.";
        }

        if (activities >= 3) {
            return "Repeated intelligence activity was recorded at this location.";
        }

        return "Intelligence activity was recorded at this location.";
    }

    private int calculateLocationRisk(
            int activities,
            int entities,
            int cases
    ) {

        int score =
                activities * 8
                        + entities * 12
                        + cases * 15;

        return Math.min(
                100,
                score
        );
    }

    private String getRiskLevel(
            int score
    ) {

        if (score >= 75) {
            return "HIGH";
        }

        if (score >= 45) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String calculateImportance(
            IntelligenceRecord record
    ) {

        String text =
                (
                        safe(record.getDescription())
                                + " "
                                + safe(record.getRecordType())
                                + " "
                                + safe(record.getRelationshipType())
                ).toLowerCase();

        if (containsAny(
                text,
                "murder",
                "kidnap",
                "kidnapping",
                "weapon",
                "extortion",
                "ransom",
                "terror",
                "fraud",
                "large transfer",
                "high value",
                "suspicious"
        )) {
            return "HIGH";
        }

        if (containsAny(
                text,
                "call",
                "transfer",
                "payment",
                "meeting",
                "travel",
                "visited",
                "vehicle",
                "location",
                "account"
        )) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private boolean containsAny(
            String text,
            String... values
    ) {

        for (String value : values) {

            if (text.contains(
                    value.toLowerCase()
            )) {
                return true;
            }
        }

        return false;
    }

    private String safe(
            Object value
    ) {

        return value == null
                ? ""
                : String.valueOf(value);
    }

    private int getInteger(
            Object value
    ) {

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(
                    String.valueOf(value)
            );
        } catch (Exception exception) {
            return 0;
        }
    }
}