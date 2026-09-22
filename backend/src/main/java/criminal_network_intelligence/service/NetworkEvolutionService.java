package criminal_network_intelligence.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class NetworkEvolutionService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;

    public NetworkEvolutionService(
            IntelligenceRecordRepository intelligenceRecordRepository
    ) {
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
    }

    /**
     * Returns complete network activity grouped by date.
     */
    public List<Map<String, Object>> getNetworkEvolution() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        return buildEvolution(records);
    }

    /**
     * Returns network evolution for a specific case.
     */
    public List<Map<String, Object>> getCaseEvolution(
            String caseId
    ) {

        if (caseId == null || caseId.isBlank()) {
            return new ArrayList<>();
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(caseId);

        return buildEvolution(records);
    }

    /**
     * Returns all intelligence activity associated
     * with a particular entity.
     */
    public List<Map<String, Object>> getEntityEvolution(
            String entityName
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (entityName == null || entityName.isBlank()) {
            return result;
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        String searchName =
                entityName.trim().toLowerCase();

        for (IntelligenceRecord record : records) {

            boolean matchesEntity =
                    containsIgnoreCase(
                            record.getEntityName(),
                            searchName
                    )
                    ||
                    containsIgnoreCase(
                            record.getRelatedEntity(),
                            searchName
                    );

            if (!matchesEntity) {
                continue;
            }

            Map<String, Object> event =
                    new LinkedHashMap<>();

            event.put("recordId", record.getId());
            event.put("caseId", record.getCaseId());
            event.put("recordType", record.getRecordType());
            event.put("source", record.getSource());
            event.put("entityName", record.getEntityName());
            event.put("relatedEntity", record.getRelatedEntity());
            event.put("relationshipType", record.getRelationshipType());
            event.put("location", record.getLocation());
            event.put("eventDate", record.getEventDate());
            event.put("description", record.getDescription());
            event.put("confidence", record.getConfidence());
            event.put(
                    "importance",
                    calculateImportance(record)
            );

            result.add(event);
        }

        result.sort(
                Comparator.comparing(
                        this::getEventDateTime,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        return result;
    }

    /**
     * Provides a compact summary of network evolution.
     */
    public Map<String, Object> getEvolutionSummary() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> evolution =
                buildEvolution(records);

        Map<String, Object> summary =
                new LinkedHashMap<>();

        int totalRecords = records.size();

        int totalRelationships = 0;

        for (IntelligenceRecord record : records) {

            if (record.getRelationshipType() != null
                    && !record.getRelationshipType().isBlank()) {

                totalRelationships++;
            }
        }

        summary.put(
                "totalRecords",
                totalRecords
        );

        summary.put(
                "totalRelationshipEvents",
                totalRelationships
        );

        summary.put(
                "timePeriods",
                evolution.size()
        );

        if (evolution.isEmpty()) {

            summary.put(
                    "peakActivityDate",
                    null
            );

            summary.put(
                    "peakActivityRecords",
                    0
            );

            summary.put(
                    "currentActivityLevel",
                    "NO_DATA"
            );

        } else {

            Map<String, Object> peak =
                    evolution.stream()
                            .max(
                                    Comparator.comparingInt(
                                            item ->
                                                    getInteger(
                                                            item.get(
                                                                    "totalRecords"
                                                            )
                                                    )
                                    )
                            )
                            .orElse(
                                    new LinkedHashMap<>()
                            );

            summary.put(
                    "peakActivityDate",
                    peak.get("date")
            );

            summary.put(
                    "peakActivityRecords",
                    peak.get("totalRecords")
            );

            Map<String, Object> latest =
                    evolution.get(0);

            summary.put(
                    "currentActivityLevel",
                    latest.get("activityLevel")
            );
        }

        summary.put(
                "interpretation",
                "Time-based activity is an investigation-support indicator. Increased activity does not by itself establish criminal involvement."
        );

        return summary;
    }

    /**
     * Groups intelligence records by event date.
     */
    private List<Map<String, Object>> buildEvolution(
            List<IntelligenceRecord> records
    ) {

        Map<LocalDate, List<IntelligenceRecord>> grouped =
                new TreeMap<>(Comparator.reverseOrder());

        for (IntelligenceRecord record : records) {

            LocalDate date =
                    extractDate(record);

            if (date == null) {
                continue;
            }

            grouped
                    .computeIfAbsent(
                            date,
                            key -> new ArrayList<>()
                    )
                    .add(record);
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map.Entry<
                LocalDate,
                List<IntelligenceRecord>
                > entry : grouped.entrySet()) {

            LocalDate date = entry.getKey();

            List<IntelligenceRecord> dailyRecords =
                    entry.getValue();

            Map<String, Object> item =
                    new LinkedHashMap<>();

            int relationshipCount = 0;
            int highImportanceCount = 0;
            int mediumImportanceCount = 0;

            List<String> entities =
                    new ArrayList<>();

            List<String> locations =
                    new ArrayList<>();

            List<String> relationshipTypes =
                    new ArrayList<>();

            for (IntelligenceRecord record :
                    dailyRecords) {

                if (record.getRelationshipType() != null
                        && !record.getRelationshipType().isBlank()) {

                    relationshipCount++;

                    if (!relationshipTypes.contains(
                            record.getRelationshipType()
                    )) {

                        relationshipTypes.add(
                                record.getRelationshipType()
                        );
                    }
                }

                if (record.getEntityName() != null
                        && !record.getEntityName().isBlank()
                        && !entities.contains(
                                record.getEntityName()
                        )) {

                    entities.add(
                            record.getEntityName()
                    );
                }

                if (record.getRelatedEntity() != null
                        && !record.getRelatedEntity().isBlank()
                        && !entities.contains(
                                record.getRelatedEntity()
                        )) {

                    entities.add(
                            record.getRelatedEntity()
                    );
                }

                if (record.getLocation() != null
                        && !record.getLocation().isBlank()
                        && !locations.contains(
                                record.getLocation()
                        )) {

                    locations.add(
                            record.getLocation()
                    );
                }

                String importance =
                        calculateImportance(record);

                if ("HIGH".equals(importance)) {
                    highImportanceCount++;
                } else if ("MEDIUM".equals(importance)) {
                    mediumImportanceCount++;
                }
            }

            int totalRecords =
                    dailyRecords.size();

            item.put(
                    "date",
                    date.toString()
            );

            item.put(
                    "totalRecords",
                    totalRecords
            );

            item.put(
                    "uniqueEntities",
                    entities.size()
            );

            item.put(
                    "relationshipEvents",
                    relationshipCount
            );

            item.put(
                    "highImportanceEvents",
                    highImportanceCount
            );

            item.put(
                    "mediumImportanceEvents",
                    mediumImportanceCount
            );

            item.put(
                    "locations",
                    locations
            );

            item.put(
                    "relationshipTypes",
                    relationshipTypes
            );

            item.put(
                    "activityLevel",
                    calculateActivityLevel(
                            totalRecords,
                            relationshipCount,
                            highImportanceCount
                    )
            );

            item.put(
                    "investigationLead",
                    calculateInvestigationLead(
                            totalRecords,
                            relationshipCount,
                            highImportanceCount
                    )
            );

            item.put(
                    "explanation",
                    buildDailyExplanation(
                            totalRecords,
                            relationshipCount,
                            highImportanceCount,
                            entities.size()
                    )
            );

            result.add(item);
        }

        return result;
    }

    private String buildDailyExplanation(
            int records,
            int relationships,
            int highImportance,
            int entities
    ) {

        if (highImportance >= 3) {
            return "This period contains multiple high-importance intelligence events and should receive investigative review.";
        }

        if (relationships >= 3) {
            return "This period contains several relationship events connecting multiple entities.";
        }

        if (entities >= 3) {
            return "Multiple entities were observed in intelligence records during this period.";
        }

        if (records > 0) {
            return "Intelligence activity was recorded during this period.";
        }

        return "No significant activity was recorded.";
    }

    private String calculateActivityLevel(
            int records,
            int relationships,
            int highImportance
    ) {

        int score =
                records * 10
                        + relationships * 8
                        + highImportance * 15;

        if (score >= 100) {
            return "VERY_HIGH";
        }

        if (score >= 60) {
            return "HIGH";
        }

        if (score >= 30) {
            return "MEDIUM";
        }

        if (score > 0) {
            return "LOW";
        }

        return "NO_ACTIVITY";
    }

    private String calculateInvestigationLead(
            int records,
            int relationships,
            int highImportance
    ) {

        int score =
                records * 8
                        + relationships * 10
                        + highImportance * 15;

        if (score >= 100) {
            return "HIGH";
        }

        if (score >= 50) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String calculateImportance(
            IntelligenceRecord record
    ) {

        String text =
                buildSearchText(record);

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

    private String buildSearchText(
            IntelligenceRecord record
    ) {

        return (
                safe(record.getDescription())
                        + " "
                        + safe(record.getRecordType())
                        + " "
                        + safe(record.getRelationshipType())
        ).toLowerCase();
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

    private boolean containsIgnoreCase(
            String value,
            String search
    ) {

        if (value == null || search == null) {
            return false;
        }

        return value
                .toLowerCase()
                .contains(search);
    }

    /**
     * IntelligenceRecord.eventDate is LocalDateTime.
     * Extracts the LocalDate directly.
     */
    private LocalDate extractDate(
            IntelligenceRecord record
    ) {

        if (record == null) {
            return null;
        }

        LocalDateTime eventDate =
                record.getEventDate();

        if (eventDate != null) {
            return eventDate.toLocalDate();
        }

        if (record.getCreatedAt() != null) {
            return record
                    .getCreatedAt()
                    .toLocalDate();
        }

        return null;
    }

    /**
     * Converts eventDate into LocalDateTime
     * for sorting entity activity.
     */
    private LocalDateTime getEventDateTime(
            Map<String, Object> event
    ) {

        Object value =
                event.get("eventDate");

        if (value == null) {
            return null;
        }

        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }

        if (value instanceof LocalDate) {
            return ((LocalDate) value)
                    .atStartOfDay();
        }

        /*
         * Defensive fallback in case a future API
         * returns a date as text.
         */
        String text =
                value.toString().trim();

        if (text.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(text);

        } catch (Exception ignored) {
            // Try date-only format.
        }

        try {
            return LocalDate
                    .parse(text)
                    .atStartOfDay();

        } catch (Exception ignored) {
            return null;
        }
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }

    private int getInteger(Object value) {

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