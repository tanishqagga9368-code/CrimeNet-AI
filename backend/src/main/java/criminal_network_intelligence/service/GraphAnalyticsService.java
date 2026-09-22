package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.neo4j.Neo4jService;

@Service
public class GraphAnalyticsService {

    private final Neo4jService neo4jService;

    public GraphAnalyticsService(Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
    }

    /**
     * Get degree centrality for all entities.
     */
    public List<Map<String, Object>> getDegreeCentrality() {

        List<Map<String, Object>> result =
                neo4jService.getDegreeCentrality();

        if (result == null) {
            return new ArrayList<>();
        }

        return result;
    }

    /**
     * Get key persons / important investigation leads.
     *
     * This is based on graph connectivity only.
     * It does NOT determine guilt.
     */
    public List<Map<String, Object>> getKeyPersons() {

        List<Map<String, Object>> result =
                neo4jService.getKeyPersons();

        if (result == null) {
            return new ArrayList<>();
        }

        result.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(item.get("degree"))
                ).reversed()
        );

        return result;
    }

    /**
     * Get complete graph statistics.
     */
    public Map<String, Object> getStatistics() {

        Map<String, Object> result =
                neo4jService.getGraphStatistics();

        if (result == null) {
            return new LinkedHashMap<>();
        }

        return result;
    }

    /**
     * Analyze one entity and its direct connections.
     */
    public Map<String, Object> analyzeEntity(String entityId) {

        Map<String, Object> result = new LinkedHashMap<>();

        if (entityId == null || entityId.isBlank()) {
            result.put("success", false);
            result.put("message", "Entity ID is required.");
            return result;
        }

        /*
         * IMPORTANT:
         * getEntityConnections() returns a LIST.
         */
        List<Map<String, Object>> connections =
                neo4jService.getEntityConnections(entityId);

        if (connections == null) {
            connections = new ArrayList<>();
        }

        result.put("success", true);
        result.put("entityId", entityId);
        result.put("connections", connections);
        result.put("degree", connections.size());

        int leadScore =
                calculateInvestigationLeadScore(connections.size());

        result.put(
                "investigationLeadScore",
                leadScore
        );

        result.put(
                "interpretation",
                "Graph connectivity is an investigation-support "
                        + "indicator and does not determine guilt."
        );

        return result;
    }

    /**
     * Find entities connected within N hops.
     */
    public List<Map<String, Object>> findConnectedEntities(
            String entityId,
            int hops
    ) {

        if (entityId == null || entityId.isBlank()) {
            return new ArrayList<>();
        }

        int safeHops = Math.max(1, Math.min(hops, 5));

        List<Map<String, Object>> connected =
                neo4jService.findConnectedEntities(
                        entityId,
                        safeHops
                );

        if (connected == null) {
            return new ArrayList<>();
        }

        return connected;
    }

    /**
     * Calculate an investigation lead score from
     * graph connectivity.
     */
    private int calculateInvestigationLeadScore(int degree) {

        if (degree >= 10) {
            return 95;
        }

        if (degree >= 8) {
            return 85;
        }

        if (degree >= 6) {
            return 75;
        }

        if (degree >= 4) {
            return 65;
        }

        if (degree >= 2) {
            return 45;
        }

        if (degree == 1) {
            return 25;
        }

        return 10;
    }

    public List<Map<String, Object>> getBetweennessCentrality() {
        return neo4jService.getBetweennessCentrality();
    }

    public List<Map<String, Object>> getPageRank() {
        return neo4jService.getPageRank();
    }

    public List<Map<String, Object>> getBridgePersons() {
        return neo4jService.getBridgePersons();
    }

    /**
     * Safely convert any value to integer.
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