package criminal_network_intelligence.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.neo4j.Neo4jService;

@Service
public class RelationshipAnalysisService {

    private final Neo4jService neo4jService;

    public RelationshipAnalysisService(Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
    }

    public List<Map<String, Object>> getRelationshipStrength() {

        Map<String, Object> network = neo4jService.getNetwork();

        List<Map<String, Object>> nodes = extractList(network, "nodes");
        List<Map<String, Object>> edges = extractList(network, "edges");

        if (edges.isEmpty()) {
            edges = extractList(network, "relationships");
        }

        Map<String, String> nodeNames = new LinkedHashMap<>();

        for (Map<String, Object> node : nodes) {
            String id = getString(node, "id");

            if (id.isBlank()) {
                id = getString(node, "entityId");
            }

            String name = getString(node, "name");

            if (!id.isBlank()) {
                nodeNames.put(id, name.isBlank() ? id : name);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> edge : edges) {

            String source = getFirstString(edge,
                    "source", "sourceId", "from");

            String target = getFirstString(edge,
                    "target", "targetId", "to");

            String relationshipType = getFirstString(edge,
                    "type", "relationshipType", "label");

            if (source.isBlank() || target.isBlank()) {
                continue;
            }

            int strength = calculateRelationshipStrength(
                    relationshipType,
                    source,
                    target,
                    edges
            );

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("sourceId", source);
            item.put("sourceName",
                    nodeNames.getOrDefault(source, source));

            item.put("targetId", target);
            item.put("targetName",
                    nodeNames.getOrDefault(target, target));

            item.put("relationshipType",
                    relationshipType.isBlank()
                            ? "CONNECTED_TO"
                            : relationshipType);

            item.put("strengthScore", strength);
            item.put("strengthLevel", getStrengthLevel(strength));
            item.put(
                    "investigationLead",
                    strength >= 75
                            ? "HIGH"
                            : strength >= 50
                            ? "MEDIUM"
                            : "LOW"
            );

            item.put(
                    "interpretation",
                    "Relationship strength is an analytical indicator based on available graph evidence; it does not establish guilt."
            );

            result.add(item);
        }

        result.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(item.get("strengthScore"))
                ).reversed()
        );

        return result;
    }

    public Map<String, Object> findPath(
            String sourceId,
            String targetId,
            int maxHops
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (sourceId == null || sourceId.isBlank()
                || targetId == null || targetId.isBlank()) {
            result.put("success", false);
            result.put("message", "Source and target entity IDs are required.");
            return result;
        }

        int safeHops = Math.max(1, Math.min(maxHops, 5));

        Map<String, Object> network = neo4jService.getNetwork();
        List<Map<String, Object>> nodes = extractList(network, "nodes");
        List<Map<String, Object>> edges = extractList(network, "edges");
        if (edges.isEmpty()) {
            edges = extractList(network, "connections");
        }
        if (edges.isEmpty()) {
            edges = extractList(network, "relationships");
        }

        Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (Map<String, Object> node : nodes) {
            String id = getFirstString(node, "id", "entityId", "neo4jId");
            if (!id.isBlank()) {
                nodeMap.put(id, node);
            }
        }

        // Build adjacency list with EdgeInfo objects
        Map<String, List<EdgeInfo>> adj = new LinkedHashMap<>();
        for (Map<String, Object> edge : edges) {
            String s = getFirstString(edge, "source", "sourceId", "from");
            String t = getFirstString(edge, "target", "targetId", "to");
            String type = getFirstString(edge, "relationship", "type", "relationshipType", "label");
            if (type.isBlank()) {
                type = "CONNECTED_TO";
            }
            String desc = getString(edge, "description");

            if (!s.isBlank() && !t.isBlank()) {
                adj.computeIfAbsent(s, k -> new ArrayList<>()).add(new EdgeInfo(s, t, type, desc, edge));
                adj.computeIfAbsent(t, k -> new ArrayList<>()).add(new EdgeInfo(t, s, type, desc, edge));
            }
        }

        // BFS traversal for shortest path
        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> parentNode = new HashMap<>();
        Map<String, EdgeInfo> parentEdge = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(sourceId);
        visited.add(sourceId);

        boolean targetFound = false;

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            if (curr.equalsIgnoreCase(targetId)) {
                targetFound = true;
                break;
            }

            List<EdgeInfo> neighbors = adj.getOrDefault(curr, Collections.emptyList());
            for (EdgeInfo ei : neighbors) {
                if (!visited.contains(ei.to)) {
                    visited.add(ei.to);
                    parentNode.put(ei.to, curr);
                    parentEdge.put(ei.to, ei);
                    queue.add(ei.to);
                }
            }
        }

        List<String> pathNodeIds = new ArrayList<>();
        List<Map<String, Object>> pathNodes = new ArrayList<>();
        List<Map<String, Object>> pathEdges = new ArrayList<>();
        StringBuilder pathChain = new StringBuilder();

        if (targetFound) {
            String curr = targetId;
            LinkedList<String> revNodeIds = new LinkedList<>();
            LinkedList<EdgeInfo> revEdges = new LinkedList<>();

            while (curr != null && !curr.equalsIgnoreCase(sourceId)) {
                revNodeIds.addFirst(curr);
                EdgeInfo edge = parentEdge.get(curr);
                if (edge != null) {
                    revEdges.addFirst(edge);
                    curr = parentNode.get(curr);
                } else {
                    break;
                }
            }
            revNodeIds.addFirst(sourceId);

            pathNodeIds = new ArrayList<>(revNodeIds);
            for (int i = 0; i < revNodeIds.size(); i++) {
                String nId = revNodeIds.get(i);
                Map<String, Object> nodeObj = nodeMap.getOrDefault(nId, Map.of("id", nId, "name", nId));
                pathNodes.add(nodeObj);
                String name = getString(nodeObj, "name");
                if (name.isBlank()) {
                    name = nId;
                }
                pathChain.append(name);

                if (i < revEdges.size()) {
                    EdgeInfo ei = revEdges.get(i);
                    pathEdges.add(ei.rawEdge);
                    pathChain.append(" ➔ [").append(ei.type).append("] ➔ ");
                }
            }
        }

        int hopCount = pathEdges.size();

        result.put("success", true);
        result.put("sourceId", sourceId);
        result.put("targetId", targetId);
        result.put("maxHops", safeHops);
        result.put("targetFound", targetFound && (hopCount <= safeHops));
        result.put("pathFound", targetFound && (hopCount <= safeHops));
        result.put("hopCount", hopCount);
        result.put("pathNodeIds", pathNodeIds);
        result.put("pathNodes", pathNodes);
        result.put("pathEdges", pathEdges);
        result.put("pathChain", pathChain.toString());
        result.put("connectedEntities", neo4jService.findConnectedEntities(sourceId, safeHops));

        if (targetFound && hopCount <= safeHops) {
            result.put("pathStatus", "PATH_FOUND");
            result.put("investigationLead", hopCount <= 2 ? "HIGH" : "MEDIUM");
            result.put("explanation", "Shortest path established with " + hopCount + " hop(s): " + pathChain);
        } else {
            result.put("pathStatus", "NO_PATH_WITHIN_LIMIT");
            result.put("investigationLead", "LOW");
            result.put("explanation", "No graph connection to the target entity was found within " + safeHops + " hops.");
        }

        result.put("interpretation", "A graph path represents an analytical association and is not proof of criminal involvement.");
        return result;
    }

    public List<Map<String, Object>> findHiddenRelationships() {
        Map<String, Object> network = neo4jService.getNetwork();
        List<Map<String, Object>> nodes = extractList(network, "nodes");
        List<Map<String, Object>> edges = extractList(network, "edges");
        if (edges.isEmpty()) {
            edges = extractList(network, "connections");
        }
        if (edges.isEmpty()) {
            edges = extractList(network, "relationships");
        }

        Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (Map<String, Object> n : nodes) {
            String id = getFirstString(n, "id", "entityId", "neo4jId");
            if (!id.isBlank()) {
                nodeMap.put(id, n);
            }
        }

        Set<String> directPairs = new HashSet<>();
        Map<String, Set<String>> neighbors = new HashMap<>();
        Map<String, Map<String, String>> edgeTypes = new HashMap<>();

        for (Map<String, Object> edge : edges) {
            String s = getFirstString(edge, "source", "sourceId", "from");
            String t = getFirstString(edge, "target", "targetId", "to");
            String rel = getFirstString(edge, "relationship", "type", "relationshipType", "label");
            if (rel.isBlank()) {
                rel = "CONNECTED";
            }

            if (!s.isBlank() && !t.isBlank()) {
                directPairs.add(s + "::" + t);
                directPairs.add(t + "::" + s);
                neighbors.computeIfAbsent(s, k -> new HashSet<>()).add(t);
                neighbors.computeIfAbsent(t, k -> new HashSet<>()).add(s);
                edgeTypes.computeIfAbsent(s, k -> new HashMap<>()).put(t, rel);
                edgeTypes.computeIfAbsent(t, k -> new HashMap<>()).put(s, rel);
            }
        }

        List<Map<String, Object>> hidden = new ArrayList<>();
        Set<String> reportedPairs = new HashSet<>();

        for (String m : neighbors.keySet()) {
            Map<String, Object> mNode = nodeMap.get(m);
            String mType = mNode != null ? getString(mNode, "type").toUpperCase() : "ENTITY";
            String mName = mNode != null ? getString(mNode, "name") : m;

            List<String> mNeighbors = new ArrayList<>(neighbors.get(m));
            for (int i = 0; i < mNeighbors.size(); i++) {
                for (int j = i + 1; j < mNeighbors.size(); j++) {
                    String a = mNeighbors.get(i);
                    String b = mNeighbors.get(j);

                    if (a.equalsIgnoreCase(b)) {
                        continue;
                    }
                    if (directPairs.contains(a + "::" + b)) {
                        continue;
                    }

                    String pairKey = a.compareTo(b) < 0 ? a + "::" + b : b + "::" + a;
                    if (reportedPairs.contains(pairKey)) {
                        continue;
                    }
                    reportedPairs.add(pairKey);

                    Map<String, Object> aNode = nodeMap.get(a);
                    Map<String, Object> bNode = nodeMap.get(b);
                    String aName = aNode != null ? getString(aNode, "name") : a;
                    String bName = bNode != null ? getString(bNode, "name") : b;
                    String aType = aNode != null ? getString(aNode, "type") : "ENTITY";
                    String bType = bNode != null ? getString(bNode, "type") : "ENTITY";

                    String relAM = edgeTypes.getOrDefault(a, Collections.emptyMap()).getOrDefault(m, "CONNECTED");
                    String relMB = edgeTypes.getOrDefault(m, Collections.emptyMap()).getOrDefault(b, "CONNECTED");

                    String inferredType;
                    int confidence;
                    if ("ORGANIZATION".equals(mType)) {
                        inferredType = "SHELL_COMPANY_NEXUS";
                        confidence = 92;
                    } else if ("PHONE".equals(mType)) {
                        inferredType = "COMMON_COMMUNICATION_RELAY";
                        confidence = 88;
                    } else if ("LOCATION".equals(mType)) {
                        inferredType = "SHARED_MEETING_HOTSPOT";
                        confidence = 84;
                    } else if ("VEHICLE".equals(mType)) {
                        inferredType = "SHARED_LOGISTICS_VEHICLE";
                        confidence = 80;
                    } else {
                        inferredType = "INDIRECT_ASSOCIATION";
                        confidence = 75;
                    }

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("sourceId", a);
                    item.put("sourceName", aName);
                    item.put("sourceType", aType);
                    item.put("targetId", b);
                    item.put("targetName", bName);
                    item.put("targetType", bType);
                    item.put("intermediaryId", m);
                    item.put("intermediaryName", mName);
                    item.put("intermediaryType", mType);
                    item.put("relAM", relAM);
                    item.put("relMB", relMB);
                    item.put("inferredRelationship", inferredType);
                    item.put("confidence", confidence);
                    item.put("hops", 2);
                    item.put("pathChain", aName + " ➔ [" + relAM + "] ➔ " + mName + " (" + mType + ") ➔ [" + relMB + "] ➔ " + bName);
                    item.put("description", aName + " is indirectly connected to " + bName + " through " + mType.toLowerCase() + " intermediary '" + mName + "'.");
                    hidden.add(item);
                }
            }
        }

        hidden.sort((x, y) -> Integer.compare((Integer) y.get("confidence"), (Integer) x.get("confidence")));
        return hidden;
    }

    public Map<String, Object> analyzeRelationship(
            String sourceId,
            String targetId
    ) {

        Map<String, Object> result = new LinkedHashMap<>();

        if (sourceId == null || sourceId.isBlank()
                || targetId == null || targetId.isBlank()) {

            result.put("success", false);
            result.put("message",
                    "Source and target entity IDs are required.");
            return result;
        }

        List<Map<String, Object>> relationships =
                getRelationshipStrength();

        for (Map<String, Object> relationship : relationships) {

            String source =
                    getString(relationship, "sourceId");

            String target =
                    getString(relationship, "targetId");

            boolean directMatch =
                    (sourceId.equals(source)
                            && targetId.equals(target))
                    ||
                    (sourceId.equals(target)
                            && targetId.equals(source));

            if (directMatch) {
                result.put("success", true);
                result.putAll(relationship);

                result.put(
                        "explanation",
                        buildRelationshipExplanation(
                                relationship
                        )
                );

                return result;
            }
        }

        result.put("success", false);
        result.put(
                "message",
                "No direct relationship was found between the supplied entities."
        );

        return result;
    }

    private String buildRelationshipExplanation(
            Map<String, Object> relationship
    ) {

        int score =
                getInteger(relationship.get("strengthScore"));

        String level =
                getString(relationship, "strengthLevel");

        String type =
                getString(relationship, "relationshipType");

        return "The "
                + type
                + " relationship has a calculated strength of "
                + score
                + "/100 and is classified as "
                + level
                + ". This is an investigation-support indicator based on available graph evidence.";
    }

    private int calculateRelationshipStrength(
            String relationshipType,
            String source,
            String target,
            List<Map<String, Object>> edges
    ) {

        int score = 40;

        String type =
                relationshipType == null
                        ? ""
                        : relationshipType.toUpperCase();

        if (type.contains("CALL")) {
            score += 15;
        }

        if (type.contains("TRANSFER")
                || type.contains("MONEY")
                || type.contains("FINANCIAL")) {
            score += 20;
        }

        if (type.contains("OWNS")
                || type.contains("WORKS_FOR")) {
            score += 10;
        }

        if (type.contains("VISITED")
                || type.contains("TRAVEL")) {
            score += 8;
        }

        int repeatedConnections = 0;

        for (Map<String, Object> edge : edges) {

            String edgeSource =
                    getFirstString(
                            edge,
                            "source",
                            "sourceId",
                            "from"
                    );

            String edgeTarget =
                    getFirstString(
                            edge,
                            "target",
                            "targetId",
                            "to"
                    );

            if ((source.equals(edgeSource)
                    && target.equals(edgeTarget))
                    ||
                    (source.equals(edgeTarget)
                            && target.equals(edgeSource))) {

                repeatedConnections++;
            }
        }

        score += Math.min(
                20,
                Math.max(0, repeatedConnections - 1) * 5
        );

        return Math.min(100, score);
    }

    private String getStrengthLevel(int score) {

        if (score >= 80) {
            return "VERY_STRONG";
        }

        if (score >= 65) {
            return "STRONG";
        }

        if (score >= 45) {
            return "MODERATE";
        }

        return "WEAK";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(
            Map<String, Object> data,
            String key
    ) {

        if (data == null) {
            return new ArrayList<>();
        }

        Object value = data.get(key);

        if (value instanceof List<?>) {
            return (List<Map<String, Object>>) value;
        }

        return new ArrayList<>();
    }

    private String getFirstString(
            Map<String, Object> map,
            String... keys
    ) {

        for (String key : keys) {

            String value = getString(map, key);

            if (!value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private String getString(
            Map<String, Object> map,
            String key
    ) {

        if (map == null || map.get(key) == null) {
            return "";
        }

        return String.valueOf(map.get(key));
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

    private static class EdgeInfo {
        final String from;
        final String to;
        final String type;
        final String description;
        final Map<String, Object> rawEdge;

        EdgeInfo(String from, String to, String type, String description, Map<String, Object> rawEdge) {
            this.from = from;
            this.to = to;
            this.type = type;
            this.description = description;
            this.rawEdge = rawEdge;
        }
    }
}