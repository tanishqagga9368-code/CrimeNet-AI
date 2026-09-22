package criminal_network_intelligence.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.neo4j.Neo4jService;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class GraphSyncService {

    private final IntelligenceRecordRepository recordRepository;
    private final Neo4jService neo4jService;

    public GraphSyncService(
            IntelligenceRecordRepository recordRepository,
            Neo4jService neo4jService
    ) {
        this.recordRepository = recordRepository;
        this.neo4jService = neo4jService;
    }

    /**
     * Sync all intelligence records belonging to a case
     * into the Neo4j knowledge graph.
     */
    public Map<String, Object> syncCase(String caseId) {

        if (caseId == null || caseId.isBlank()) {

            return Map.of(
                    "success", false,
                    "message", "Case ID is required"
            );
        }

        List<IntelligenceRecord> records =
                recordRepository.findByCaseIdOrderByCreatedAtDesc(caseId);

        int recordsProcessed = 0;
        int nodesCreated = 0;
        int relationshipsCreated = 0;

        Set<String> processedNodes = new HashSet<>();
        Set<String> processedRelationships = new HashSet<>();

        for (IntelligenceRecord record : records) {

            if (record == null) {
                continue;
            }

            /*
             * Main entity
             */
            String entityName =
                    clean(record.getEntityName());

            String entityType =
                    normalizeEntityType(record.getEntityType());

            if (!entityName.isBlank()) {

                String entityId =
                        generateEntityId(
                                entityType,
                                entityName
                        );

                neo4jService.createNode(
                        entityId,
                        entityName,
                        entityType,
                        calculateRisk(record),
                        null,
                        record.getLocation()
                );

                if (processedNodes.add(entityId)) {
                    nodesCreated++;
                }
            }

            /*
             * Related entity
             */
            String relatedEntityName =
                    clean(record.getRelatedEntity());

            if (!entityName.isBlank()
                    && !relatedEntityName.isBlank()) {

                String sourceId =
                        generateEntityId(
                                entityType,
                                entityName
                        );

                String relatedType =
                        inferRelatedEntityType(
                                record.getRelationshipType()
                        );

                String targetId =
                        generateEntityId(
                                relatedType,
                                relatedEntityName
                        );

                neo4jService.createNode(
                        targetId,
                        relatedEntityName,
                        relatedType,
                        "LOW",
                        null,
                        record.getLocation()
                );

                if (processedNodes.add(targetId)) {
                    nodesCreated++;
                }

                String relationshipType =
                        normalizeRelationshipType(
                                record.getRelationshipType()
                        );

                String relationshipKey =
                        sourceId
                                + "|"
                                + relationshipType
                                + "|"
                                + targetId;

                if (processedRelationships.add(
                        relationshipKey
                )) {

                    neo4jService.createRelationship(
                            sourceId,
                            targetId,
                            relationshipType,
                            buildRelationshipDescription(record)
                    );

                    relationshipsCreated++;
                }
            }

            /*
             * Location relationship
             */
            String location =
                    clean(record.getLocation());

            if (!entityName.isBlank()
                    && !location.isBlank()) {

                String sourceId =
                        generateEntityId(
                                entityType,
                                entityName
                        );

                String locationId =
                        generateEntityId(
                                "LOCATION",
                                location
                        );

                neo4jService.createNode(
                        locationId,
                        location,
                        "LOCATION",
                        "LOW",
                        null,
                        location
                );

                if (processedNodes.add(locationId)) {
                    nodesCreated++;
                }

                String relationshipKey =
                        sourceId
                                + "|VISITED|"
                                + locationId;

                if (processedRelationships.add(
                        relationshipKey
                )) {

                    neo4jService.createRelationship(
                            sourceId,
                            locationId,
                            "VISITED",
                            "Location associated with intelligence record "
                                    + record.getId()
                    );

                    relationshipsCreated++;
                }
            }

            recordsProcessed++;
        }

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("success", true);
        result.put("caseId", caseId);
        result.put("recordsProcessed", recordsProcessed);
        result.put("nodesCreated", nodesCreated);
        result.put(
                "relationshipsCreated",
                relationshipsCreated
        );
        result.put(
                "message",
                "Intelligence data synchronized with Neo4j"
        );

        return result;
    }

    /**
     * Sync every intelligence record in PostgreSQL.
     */
    public Map<String, Object> syncAll() {

        List<IntelligenceRecord> records =
                recordRepository.findAllByOrderByCreatedAtDesc();

        int recordsProcessed = 0;
        int nodesCreated = 0;
        int relationshipsCreated = 0;

        Set<String> processedNodes = new HashSet<>();
        Set<String> processedRelationships = new HashSet<>();

        for (IntelligenceRecord record : records) {

            if (record == null) {
                continue;
            }

            String entityName =
                    clean(record.getEntityName());

            if (entityName.isBlank()) {
                continue;
            }

            String entityType =
                    normalizeEntityType(
                            record.getEntityType()
                    );

            String sourceId =
                    generateEntityId(
                            entityType,
                            entityName
                    );

            neo4jService.createNode(
                    sourceId,
                    entityName,
                    entityType,
                    calculateRisk(record),
                    null,
                    record.getLocation()
            );

            if (processedNodes.add(sourceId)) {
                nodesCreated++;
            }

            String relatedName =
                    clean(record.getRelatedEntity());

            if (!relatedName.isBlank()) {

                String targetType =
                        inferRelatedEntityType(
                                record.getRelationshipType()
                        );

                String targetId =
                        generateEntityId(
                                targetType,
                                relatedName
                        );

                neo4jService.createNode(
                        targetId,
                        relatedName,
                        targetType,
                        "LOW",
                        null,
                        record.getLocation()
                );

                if (processedNodes.add(targetId)) {
                    nodesCreated++;
                }

                String relationshipType =
                        normalizeRelationshipType(
                                record.getRelationshipType()
                        );

                String relationshipKey =
                        sourceId
                                + "|"
                                + relationshipType
                                + "|"
                                + targetId;

                if (processedRelationships.add(
                        relationshipKey
                )) {

                    neo4jService.createRelationship(
                            sourceId,
                            targetId,
                            relationshipType,
                            buildRelationshipDescription(record)
                    );

                    relationshipsCreated++;
                }
            }

            recordsProcessed++;
        }

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("success", true);
        result.put(
                "recordsProcessed",
                recordsProcessed
        );
        result.put("nodesCreated", nodesCreated);
        result.put(
                "relationshipsCreated",
                relationshipsCreated
        );
        result.put(
                "message",
                "All intelligence records synchronized with Neo4j"
        );

        return result;
    }

    /**
     * Converts entity name/type into a deterministic Neo4j ID.
     */
    private String generateEntityId(
            String type,
            String name
    ) {

        String value =
                type.toUpperCase()
                        + ":"
                        + clean(name).toLowerCase();

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {

                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return "ENT-" +
                    result.substring(0, 16).toUpperCase();

        } catch (Exception exception) {

            return "ENT-" +
                    Math.abs(
                            value.hashCode()
                    );
        }
    }

    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private String normalizeEntityType(
            String type
    ) {

        if (type == null
                || type.isBlank()) {

            return "PERSON";
        }

        String normalized =
                type.trim()
                        .toUpperCase()
                        .replace(" ", "_")
                        .replace("-", "_");

        return switch (normalized) {

            case "PERSON",
                 "PEOPLE",
                 "INDIVIDUAL" ->
                    "PERSON";

            case "PHONE",
                 "PHONE_NUMBER",
                 "MOBILE" ->
                    "PHONE";

            case "VEHICLE",
                 "CAR",
                 "VEHICLE_NUMBER" ->
                    "VEHICLE";

            case "BANK",
                 "BANK_ACCOUNT",
                 "ACCOUNT" ->
                    "BANK_ACCOUNT";

            case "LOCATION",
                 "PLACE",
                 "CITY" ->
                    "LOCATION";

            case "ORGANIZATION",
                 "ORG",
                 "COMPANY" ->
                    "ORGANIZATION";

            case "FIR" ->
                    "FIR";

            case "CRIME" ->
                    "CRIME";

            case "SOCIAL_MEDIA",
                 "SOCIAL_MEDIA_ACCOUNT" ->
                    "SOCIAL_MEDIA_ACCOUNT";

            default ->
                    normalized;
        };
    }

    private String normalizeRelationshipType(
            String relationship
    ) {

        if (relationship == null
                || relationship.isBlank()) {

            return "CONNECTED_TO";
        }

        String normalized =
                relationship.trim()
                        .toUpperCase()
                        .replace(" ", "_")
                        .replace("-", "_");

        return switch (normalized) {

            case "CALL",
                 "CALLED",
                 "PHONE_CALL",
                 "CONTACT" ->
                    "CALLS";

            case "TRANSFER",
                 "MONEY_TRANSFER",
                 "TRANSFERRED",
                 "TRANSACTION",
                 "PAYMENT" ->
                    "TRANSFERRED_MONEY";

            case "OWN",
                 "OWNER",
                 "OWNS" ->
                    "OWNS";

            case "WORK",
                 "WORKS",
                 "EMPLOYEE" ->
                    "WORKS_FOR";

            case "VISIT",
                 "VISITED",
                 "TRAVEL",
                 "TRAVELLED",
                 "TRAVELED" ->
                    "VISITED";

            case "MENTION",
                 "MENTIONED" ->
                    "MENTIONED_IN";

            case "ASSOCIATED",
                 "ASSOCIATION",
                 "LINKED" ->
                    "ASSOCIATED_WITH";

            default ->
                    normalized;
        };
    }

    private String inferRelatedEntityType(
            String relationship
    ) {

        String normalized =
                normalizeRelationshipType(
                        relationship
                );

        return switch (normalized) {

            case "CALLS" ->
                    "PERSON";

            case "TRANSFERRED_MONEY" ->
                    "BANK_ACCOUNT";

            case "OWNS" ->
                    "VEHICLE";

            case "WORKS_FOR" ->
                    "ORGANIZATION";

            case "VISITED" ->
                    "LOCATION";

            default ->
                    "PERSON";
        };
    }

    private String calculateRisk(
            IntelligenceRecord record
    ) {

        String description =
                clean(record.getDescription())
                        .toLowerCase();

        if (description.contains("suspicious")
                || description.contains("fraud")
                || description.contains("hawala")
                || description.contains("extortion")
                || description.contains("trafficking")
                || description.contains("money laundering")) {

            return "HIGH";
        }

        if (description.contains("unknown")
                || description.contains("linked")
                || description.contains("association")) {

            return "MEDIUM";
        }

        return "LOW";
    }

    private String buildRelationshipDescription(
            IntelligenceRecord record
    ) {

        StringBuilder description =
                new StringBuilder();

        if (record.getDescription() != null
                && !record.getDescription().isBlank()) {

            description.append(
                    record.getDescription()
            );

        } else {

            description.append(
                    "Relationship extracted from intelligence record"
            );
        }

        if (record.getSource() != null
                && !record.getSource().isBlank()) {

            description.append(
                    " | Source: "
            );

            description.append(
                    record.getSource()
            );
        }

        /*
         * IntelligenceRecord.eventDate is LocalDateTime,
         * so it cannot use isBlank().
         */
        if (record.getEventDate() != null) {

            description.append(
                    " | Date: "
            );

            description.append(
                    record.getEventDate()
            );
        }

        return description.toString();
    }
}