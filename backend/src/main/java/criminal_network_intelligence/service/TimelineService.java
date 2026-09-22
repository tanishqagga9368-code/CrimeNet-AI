package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class TimelineService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;

    public TimelineService(
            IntelligenceRecordRepository intelligenceRecordRepository
    ) {
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
    }

    /**
     * Get timeline for a specific case.
     */
    public List<Map<String, Object>> getCaseTimeline(
            String caseId
    ) {

        if (caseId == null || caseId.isBlank()) {
            return new ArrayList<>();
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(caseId);

        return convertToTimeline(records);
    }

    /**
     * Get complete intelligence timeline.
     */
    public List<Map<String, Object>> getAllTimeline() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        return convertToTimeline(records);
    }

    /**
     * Get timeline filtered by record type.
     */
    public List<Map<String, Object>> getTimelineByType(
            String recordType
    ) {

        if (recordType == null || recordType.isBlank()) {
            return new ArrayList<>();
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByRecordTypeOrderByCreatedAtDesc(
                                recordType
                        );

        return convertToTimeline(records);
    }

    /**
     * Convert database records into frontend-friendly timeline objects.
     */
    private List<Map<String, Object>> convertToTimeline(
            List<IntelligenceRecord> records
    ) {

        List<Map<String, Object>> timeline =
                new ArrayList<>();

        if (records == null) {
            return timeline;
        }

        for (IntelligenceRecord record : records) {

            if (record == null) {
                continue;
            }

            Map<String, Object> event =
                    new LinkedHashMap<>();

            event.put("id", record.getId());
            event.put("caseId", record.getCaseId());
            event.put("recordType", record.getRecordType());
            event.put("source", record.getSource());

            event.put(
                    "entityName",
                    record.getEntityName()
            );

            event.put(
                    "entityType",
                    record.getEntityType()
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
                    "confidence",
                    record.getConfidence()
            );

            event.put(
                    "createdAt",
                    record.getCreatedAt()
            );

            /*
             * Investigation-support classification.
             * This does NOT determine guilt.
             */
            event.put(
                    "importance",
                    calculateImportance(record)
            );

            timeline.add(event);
        }

        /*
         * Sort by event date when available.
         * Latest events appear first.
         */
        timeline.sort(
                Comparator.comparing(
                        (Map<String, Object> item) ->
                                getSortableDate(
                                        item.get("eventDate")
                                ),
                        Comparator.reverseOrder()
                )
        );

        return timeline;
    }

    /**
     * Calculate an investigation-support importance level.
     */
    private String calculateImportance(
            IntelligenceRecord record
    ) {

        String description =
                record.getDescription() == null
                        ? ""
                        : record.getDescription().toLowerCase();

        String relationship =
                record.getRelationshipType() == null
                        ? ""
                        : record.getRelationshipType().toLowerCase();

        if (
                description.contains("weapon")
                        || description.contains("threat")
                        || description.contains("money")
                        || description.contains("transfer")
                        || description.contains("fraud")
                        || relationship.contains("transfer")
        ) {
            return "HIGH";
        }

        if (
                description.contains("call")
                        || description.contains("meeting")
                        || description.contains("location")
                        || description.contains("travel")
                        || relationship.contains("call")
                        || relationship.contains("visited")
        ) {
            return "MEDIUM";
        }

        return "LOW";
    }

    /**
     * Converts different date representations into
     * a comparable string.
     */
    private String getSortableDate(Object value) {

        if (value == null) {
            return "";
        }

        return value.toString();
    }
}