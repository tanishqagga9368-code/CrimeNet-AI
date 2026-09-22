package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class CrossCaseConnectionService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;

    public CrossCaseConnectionService(
            IntelligenceRecordRepository intelligenceRecordRepository
    ) {
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
    }

    /**
     * Find entities appearing in multiple cases.
     */
    public List<Map<String, Object>> findCrossCaseConnections() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, EntityCaseData> entityMap =
                new LinkedHashMap<>();

        for (IntelligenceRecord record : records) {

            if (record == null) {
                continue;
            }

            addEntity(
                    entityMap,
                    record.getEntityName(),
                    record.getEntityType(),
                    record.getCaseId(),
                    record.getRecordType(),
                    record.getLocation()
            );

            addEntity(
                    entityMap,
                    record.getRelatedEntity(),
                    "RELATED_ENTITY",
                    record.getCaseId(),
                    record.getRecordType(),
                    record.getLocation()
            );
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (EntityCaseData data : entityMap.values()) {

            if (data.caseIds.size() < 2) {
                continue;
            }

            Map<String, Object> connection =
                    new LinkedHashMap<>();

            connection.put(
                    "entityName",
                    data.displayName
            );

            connection.put(
                    "entityType",
                    data.entityType
            );

            connection.put(
                    "caseCount",
                    data.caseIds.size()
            );

            connection.put(
                    "caseIds",
                    new ArrayList<>(data.caseIds)
            );

            connection.put(
                    "recordTypes",
                    new ArrayList<>(data.recordTypes)
            );

            connection.put(
                    "locations",
                    new ArrayList<>(data.locations)
            );

            connection.put(
                    "connectionStrength",
                    calculateConnectionStrength(
                            data.caseIds.size(),
                            data.recordTypes.size()
                    )
            );

            connection.put(
                    "investigationLead",
                    buildInvestigationLead(
                            data.displayName,
                            data.caseIds
                    )
            );

            connection.put(
                    "interpretation",
                    "Entity appears across multiple cases and "
                            + "may represent a potential investigative link. "
                            + "This does not establish guilt or criminal responsibility."
            );

            result.add(connection);
        }

        result.sort(
                (a, b) ->
                        Integer.compare(
                                getInteger(
                                        b.get("connectionStrength")
                                ),
                                getInteger(
                                        a.get("connectionStrength")
                                )
                        )
        );

        return result;
    }

    /**
     * Find cross-case connections for a particular case.
     */
    public List<Map<String, Object>> findConnectionsForCase(
            String caseId
    ) {

        List<Map<String, Object>> allConnections =
                findCrossCaseConnections();

        if (caseId == null || caseId.isBlank()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map<String, Object> connection : allConnections) {

            Object caseIdsObject =
                    connection.get("caseIds");

            if (!(caseIdsObject instanceof List<?>)) {
                continue;
            }

            List<?> caseIds =
                    (List<?>) caseIdsObject;

            boolean containsCase = false;

            for (Object id : caseIds) {

                if (
                        id != null
                                && caseId.equalsIgnoreCase(
                                        id.toString()
                                )
                ) {
                    containsCase = true;
                    break;
                }
            }

            if (containsCase) {
                result.add(connection);
            }
        }

        return result;
    }

    /**
     * Find connections for a specific entity name.
     */
    public List<Map<String, Object>> findEntityConnections(
            String entityName
    ) {

        if (entityName == null || entityName.isBlank()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> allConnections =
                findCrossCaseConnections();

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map<String, Object> connection : allConnections) {

            Object name =
                    connection.get("entityName");

            if (
                    name != null
                            && entityName.equalsIgnoreCase(
                                    name.toString()
                            )
            ) {
                result.add(connection);
            }
        }

        return result;
    }

    /**
     * Add an entity occurrence to the aggregation map.
     */
    private void addEntity(
            Map<String, EntityCaseData> entityMap,
            String entityName,
            String entityType,
            String caseId,
            String recordType,
            String location
    ) {

        if (
                entityName == null
                        || entityName.isBlank()
                        || caseId == null
                        || caseId.isBlank()
        ) {
            return;
        }

        String normalizedName =
                normalize(entityName);

        if (normalizedName.isBlank()) {
            return;
        }

        EntityCaseData data =
                entityMap.computeIfAbsent(
                        normalizedName,
                        key -> new EntityCaseData()
                );

        if (data.displayName == null) {
            data.displayName =
                    entityName.trim();
        }

        if (
                data.entityType == null
                        || data.entityType.isBlank()
        ) {
            data.entityType =
                    entityType == null
                            ? "UNKNOWN"
                            : entityType;
        }

        data.caseIds.add(caseId);

        if (
                recordType != null
                        && !recordType.isBlank()
        ) {
            data.recordTypes.add(recordType);
        }

        if (
                location != null
                        && !location.isBlank()
        ) {
            data.locations.add(location);
        }
    }

    /**
     * Calculate a simple cross-case connection strength.
     */
    private int calculateConnectionStrength(
            int caseCount,
            int recordTypeCount
    ) {

        int score =
                caseCount * 25
                        + recordTypeCount * 10;

        return Math.min(score, 100);
    }

    /**
     * Generate an investigation-support lead message.
     */
    private String buildInvestigationLead(
            String entityName,
            Set<String> caseIds
    ) {

        return entityName
                + " appears in "
                + caseIds.size()
                + " separate cases. "
                + "Review the shared evidence, relationships, "
                + "and timeline before drawing conclusions.";
    }

    /**
     * Normalize entity names for matching.
     */
    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }

    /**
     * Safely convert value to integer.
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

    /**
     * Internal aggregation structure.
     */
    private static class EntityCaseData {

        private String displayName;

        private String entityType;

        private final Set<String> caseIds =
                new HashSet<>();

        private final Set<String> recordTypes =
                new HashSet<>();

        private final Set<String> locations =
                new HashSet<>();
    }
}