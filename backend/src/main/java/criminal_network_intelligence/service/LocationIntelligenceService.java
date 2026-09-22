package criminal_network_intelligence.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class LocationIntelligenceService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;

    public LocationIntelligenceService(
            IntelligenceRecordRepository intelligenceRecordRepository
    ) {
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
    }

    public Map<String, Object> getLocationIntelligence() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        Map<String, LocationBucket> buckets =
                new LinkedHashMap<>();

        for (IntelligenceRecord record : records) {

            String location =
                    normalizeLocation(record.getLocation());

            if (location.isBlank()) {
                continue;
            }

            LocationBucket bucket =
                    buckets.computeIfAbsent(
                            location,
                            key -> new LocationBucket(
                                    location
                            )
                    );

            bucket.addRecord(record);
        }

        List<Map<String, Object>> locations =
                new ArrayList<>();

        for (LocationBucket bucket : buckets.values()) {
            locations.add(
                    bucket.toMap()
            );
        }

        locations.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(
                                        item.get("activityCount")
                                )
                ).reversed()
        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", true);
        response.put(
                "totalLocations",
                locations.size()
        );
        response.put(
                "locations",
                locations
        );
        response.put(
                "interpretation",
                "Location intelligence identifies recurring "
                        + "locations and movement patterns for "
                        + "investigation support. Location association "
                        + "does not establish guilt."
        );

        return response;
    }

    public Map<String, Object> getCaseLocations(
            String caseId
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        if (caseId == null || caseId.isBlank()) {

            response.put("success", false);
            response.put(
                    "message",
                    "Case ID is required."
            );

            return response;
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(
                                caseId
                        );

        Map<String, LocationBucket> buckets =
                new LinkedHashMap<>();

        for (IntelligenceRecord record : records) {

            String location =
                    normalizeLocation(
                            record.getLocation()
                    );

            if (location.isBlank()) {
                continue;
            }

            LocationBucket bucket =
                    buckets.computeIfAbsent(
                            location,
                            key -> new LocationBucket(
                                    location
                            )
                    );

            bucket.addRecord(record);
        }

        List<Map<String, Object>> locations =
                new ArrayList<>();

        for (LocationBucket bucket : buckets.values()) {
            locations.add(
                    bucket.toMap()
            );
        }

        locations.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(
                                        item.get("activityCount")
                                )
                ).reversed()
        );

        response.put("success", true);
        response.put("caseId", caseId);
        response.put(
                "totalLocations",
                locations.size()
        );
        response.put(
                "locations",
                locations
        );

        return response;
    }

    public List<Map<String, Object>> getLocationActivity(
            String location
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (location == null || location.isBlank()) {
            return result;
        }

        String requestedLocation =
                normalizeLocation(location);

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        for (IntelligenceRecord record : records) {

            String recordLocation =
                    normalizeLocation(
                            record.getLocation()
                    );

            if (!recordLocation.equals(
                    requestedLocation
            )) {
                continue;
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
                    "recordType",
                    record.getRecordType()
            );

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
                    recordLocation
            );

            event.put(
                    "eventDate",
                    record.getEventDate()
            );

            event.put(
                    "description",
                    record.getDescription()
            );

            result.add(event);
        }

        return result;
    }

    public List<Map<String, Object>> getMovementTimeline(
            String entityName
    ) {

        List<Map<String, Object>> movement =
                new ArrayList<>();

        if (entityName == null
                || entityName.isBlank()) {

            return movement;
        }

        String requestedEntity =
                normalizeEntityName(entityName);

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        for (IntelligenceRecord record : records) {

            String currentEntity =
                    normalizeEntityName(
                            record.getEntityName()
                    );

            String relatedEntity =
                    normalizeEntityName(
                            record.getRelatedEntity()
                    );

            if (!requestedEntity.equals(currentEntity)
                    && !requestedEntity.equals(
                            relatedEntity
                    )) {
                continue;
            }

            String location =
                    normalizeLocation(
                            record.getLocation()
                    );

            if (location.isBlank()) {
                continue;
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
                    "location",
                    location
            );

            event.put(
                    "eventDate",
                    record.getEventDate()
            );

            event.put(
                    "recordType",
                    record.getRecordType()
            );

            event.put(
                    "relationshipType",
                    record.getRelationshipType()
            );

            event.put(
                    "description",
                    record.getDescription()
            );

            movement.add(event);
        }

        movement.sort(
                Comparator.comparing(
                        item -> parseDate(
                                item.get("eventDate")
                        ),
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        return movement;
    }

    public List<Map<String, Object>> getSharedLocations() {

        List<Map<String, Object>> result =
                new ArrayList<>();

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        Map<String, Set<String>> locationEntities =
                new LinkedHashMap<>();

        Map<String, Set<String>> locationCases =
                new LinkedHashMap<>();

        for (IntelligenceRecord record : records) {

            String location =
                    normalizeLocation(
                            record.getLocation()
                    );

            if (location.isBlank()) {
                continue;
            }

            locationEntities
                    .computeIfAbsent(
                            location,
                            key -> new LinkedHashSet<>()
                    )
                    .add(
                            safe(record.getEntityName())
                    );

            locationCases
                    .computeIfAbsent(
                            location,
                            key -> new LinkedHashSet<>()
                    )
                    .add(
                            safe(record.getCaseId())
                    );
        }

        for (String location :
                locationEntities.keySet()) {

            Set<String> entities =
                    locationEntities.get(location);

            Set<String> cases =
                    locationCases.get(location);

            entities.remove("");

            cases.remove("");

            if (entities.size() < 2
                    && cases.size() < 2) {
                continue;
            }

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "location",
                    location
            );

            item.put(
                    "entities",
                    new ArrayList<>(entities)
            );

            item.put(
                    "caseIds",
                    new ArrayList<>(cases)
            );

            item.put(
                    "entityCount",
                    entities.size()
            );

            item.put(
                    "caseCount",
                    cases.size()
            );

            item.put(
                    "investigationLead",
                    true
            );

            item.put(
                    "explanation",
                    "Multiple entities or cases share this "
                            + "location and may warrant review."
            );

            result.add(item);
        }

        result.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(
                                        item.get("entityCount")
                                )
                ).reversed()
        );

        return result;
    }

    private String normalizeLocation(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private String normalizeEntityName(
            String value
    ) {

        return safe(value)
                .toLowerCase()
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private LocalDate parseDate(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        try {
            return LocalDate.parse(
                    value.toString()
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private int getInteger(Object value) {

        if (value == null) {
            return 0;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(
                    value.toString()
            );
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static class LocationBucket {

        private final String location;

        private final Set<String> entities =
                new LinkedHashSet<>();

        private final Set<String> caseIds =
                new LinkedHashSet<>();

        private final Set<String> recordTypes =
                new LinkedHashSet<>();

        private int activityCount;

        private LocationBucket(
                String location
        ) {
            this.location = location;
        }

        private void addRecord(
                IntelligenceRecord record
        ) {

            activityCount++;

            if (record.getEntityName() != null
                    && !record.getEntityName().isBlank()) {

                entities.add(
                        record.getEntityName().trim()
                );
            }

            if (record.getCaseId() != null
                    && !record.getCaseId().isBlank()) {

                caseIds.add(
                        record.getCaseId().trim()
                );
            }

            if (record.getRecordType() != null
                    && !record.getRecordType().isBlank()) {

                recordTypes.add(
                        record.getRecordType().trim()
                );
            }
        }

        private Map<String, Object> toMap() {

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put(
                    "location",
                    location
            );

            result.put(
                    "activityCount",
                    activityCount
            );

            result.put(
                    "entityCount",
                    entities.size()
            );

            result.put(
                    "caseCount",
                    caseIds.size()
            );

            result.put(
                    "entities",
                    new ArrayList<>(entities)
            );

            result.put(
                    "caseIds",
                    new ArrayList<>(caseIds)
            );

            result.put(
                    "recordTypes",
                    new ArrayList<>(recordTypes)
            );

            result.put(
                    "locationRisk",
                    calculateRisk(
                            activityCount,
                            entities.size(),
                            caseIds.size()
                    )
            );

            result.put(
                    "investigationLead",
                    activityCount >= 2
                            || entities.size() >= 2
                            || caseIds.size() >= 2
            );

            Map<String, Object> coordinates =
                    getCoordinates(location);

            result.put(
                    "latitude",
                    coordinates.get("latitude")
            );

            result.put(
                    "longitude",
                    coordinates.get("longitude")
            );

            result.put(
                    "coordinatesAvailable",
                    coordinates.get(
                            "available"
                    )
            );

            return result;
        }

        private String calculateRisk(
                int activities,
                int entityCount,
                int caseCount
        ) {

            int score =
                    activities * 10
                            + entityCount * 12
                            + caseCount * 18;

            if (score >= 80) {
                return "HIGH";
            }

            if (score >= 45) {
                return "MEDIUM";
            }

            return "LOW";
        }

        private Map<String, Object> getCoordinates(
                String location
        ) {

            String value =
                    location
                            .toLowerCase()
                            .replaceAll(
                                    "[^a-z ]",
                                    " "
                            )
                            .replaceAll(
                                    "\\s+",
                                    " "
                            )
                            .trim();

            Map<String, Object> result =
                    new LinkedHashMap<>();

            Double latitude = null;
            Double longitude = null;

            if (value.contains("delhi")
                    || value.contains("new delhi")) {

                latitude = 28.6139;
                longitude = 77.2090;

            } else if (value.contains("gurugram")
                    || value.contains("gurgaon")) {

                latitude = 28.4595;
                longitude = 77.0266;

            } else if (value.contains("panipat")) {

                latitude = 29.3909;
                longitude = 76.9635;

            } else if (value.contains("chandigarh")) {

                latitude = 30.7333;
                longitude = 76.7794;

            } else if (value.contains("noida")) {

                latitude = 28.5355;
                longitude = 77.3910;

            } else if (value.contains("mumbai")) {

                latitude = 19.0760;
                longitude = 72.8777;

            } else if (value.contains("jaipur")) {

                latitude = 26.9124;
                longitude = 75.7873;

            } else if (value.contains("lucknow")) {

                latitude = 26.8467;
                longitude = 80.9462;
            }

            result.put(
                    "latitude",
                    latitude
            );

            result.put(
                    "longitude",
                    longitude
            );

            result.put(
                    "available",
                    latitude != null
                            && longitude != null
            );

            return result;
        }
    }
}