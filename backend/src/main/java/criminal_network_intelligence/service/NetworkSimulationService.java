package criminal_network_intelligence.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.neo4j.Neo4jService;

@Service
public class NetworkSimulationService {

    private final Neo4jService neo4jService;

    public NetworkSimulationService(
            Neo4jService neo4jService
    ) {
        this.neo4jService = neo4jService;
    }

    public Map<String, Object> simulateNodeRemoval(
            String entityId
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        if (entityId == null || entityId.isBlank()) {
            response.put("success", false);
            response.put(
                    "message",
                    "Entity ID is required."
            );
            return response;
        }

        Map<String, Object> network =
                safeNetwork();

        GraphData graph =
                buildGraph(network);

        if (!graph.nodes.contains(entityId)) {

            response.put("success", false);
            response.put(
                    "message",
                    "Entity was not found in the current graph."
            );
            response.put(
                    "entityId",
                    entityId
            );

            return response;
        }

        int beforeComponents =
                countComponents(
                        graph.nodes,
                        graph.adjacency,
                        null
                );

        int beforeNodes =
                graph.nodes.size();

        int beforeEdges =
                countEdges(
                        graph.adjacency
                );

        int directConnections =
                graph.adjacency
                        .getOrDefault(
                                entityId,
                                new HashSet<>()
                        )
                        .size();

        int afterComponents =
                countComponents(
                        graph.nodes,
                        graph.adjacency,
                        entityId
                );

        int afterNodes =
                beforeNodes - 1;

        int afterEdges =
                Math.max(
                        0,
                        beforeEdges - directConnections
                );

        int fragmentationIncrease =
                Math.max(
                        0,
                        afterComponents - beforeComponents
                );

        double fragmentationImpact =
                calculateFragmentationImpact(
                        beforeComponents,
                        afterComponents
                );

        double connectivityImpact =
                calculateConnectivityImpact(
                        beforeNodes,
                        directConnections
                );

        double criticalityScore =
                Math.min(
                        100.0,
                        fragmentationImpact * 0.60
                                + connectivityImpact * 0.40
                );

        Map<String, Object> entity =
                findNode(
                        network,
                        entityId
                );

        response.put("success", true);
        response.put("simulation", "NODE_REMOVAL");
        response.put("entityId", entityId);
        response.put(
                "entity",
                entity
        );

        response.put(
                "before",
                Map.of(
                        "nodes",
                        beforeNodes,
                        "edges",
                        beforeEdges,
                        "components",
                        beforeComponents
                )
        );

        response.put(
                "after",
                Map.of(
                        "nodes",
                        afterNodes,
                        "edges",
                        afterEdges,
                        "components",
                        afterComponents
                )
        );

        response.put(
                "directConnectionsRemoved",
                directConnections
        );

        response.put(
                "fragmentationIncrease",
                fragmentationIncrease
        );

        response.put(
                "fragmentationImpact",
                round(fragmentationImpact)
        );

        response.put(
                "connectivityImpact",
                round(connectivityImpact)
        );

        response.put(
                "criticalityScore",
                round(criticalityScore)
        );

        response.put(
                "criticalityLevel",
                criticalityLevel(
                        criticalityScore
                )
        );

        response.put(
                "interpretation",
                buildInterpretation(
                        fragmentationIncrease,
                        directConnections,
                        criticalityScore
                )
        );

        response.put(
                "disclaimer",
                "Network criticality is an analytical indicator "
                        + "and does not imply criminal responsibility."
        );

        return response;
    }

    public List<Map<String, Object>> getCriticalNodes(
            int limit
    ) {

        int safeLimit =
                Math.max(
                        1,
                        Math.min(
                                limit,
                                20
                        )
                );

        Map<String, Object> network =
                safeNetwork();

        GraphData graph =
                buildGraph(network);

        List<Map<String, Object>> result =
                new ArrayList<>();

        int baseComponents =
                countComponents(
                        graph.nodes,
                        graph.adjacency,
                        null
                );

        int totalNodes =
                graph.nodes.size();

        for (String nodeId : graph.nodes) {

            int degree =
                    graph.adjacency
                            .getOrDefault(
                                    nodeId,
                                    new HashSet<>()
                            )
                            .size();

            int afterComponents =
                    countComponents(
                            graph.nodes,
                            graph.adjacency,
                            nodeId
                    );

            int fragmentationIncrease =
                    Math.max(
                            0,
                            afterComponents
                                    - baseComponents
                    );

            double fragmentationImpact =
                    calculateFragmentationImpact(
                            baseComponents,
                            afterComponents
                    );

            double connectivityImpact =
                    calculateConnectivityImpact(
                            totalNodes,
                            degree
                    );

            double score =
                    Math.min(
                            100.0,
                            fragmentationImpact * 0.60
                                    + connectivityImpact * 0.40
                    );

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "entityId",
                    nodeId
            );

            item.put(
                    "entity",
                    findNode(
                            network,
                            nodeId
                    )
            );

            item.put(
                    "degree",
                    degree
            );

            item.put(
                    "componentsBefore",
                    baseComponents
            );

            item.put(
                    "componentsAfter",
                    afterComponents
            );

            item.put(
                    "fragmentationIncrease",
                    fragmentationIncrease
            );

            item.put(
                    "criticalityScore",
                    round(score)
            );

            item.put(
                    "criticalityLevel",
                    criticalityLevel(score)
            );

            item.put(
                    "investigationLead",
                    score >= 60
            );

            result.add(item);
        }

        result.sort(
                Comparator.comparingDouble(
                        (Map<String, Object> item) ->
                                getDouble(
                                        item.get(
                                                "criticalityScore"
                                        )
                                )
                ).reversed()
        );

        if (result.size() > safeLimit) {
            return new ArrayList<>(
                    result.subList(
                            0,
                            safeLimit
                    )
            );
        }

        return result;
    }

    private Map<String, Object> safeNetwork() {

        try {

            Map<String, Object> network =
                    neo4jService.getNetwork();

            if (network != null) {
                return network;
            }

        } catch (Exception ignored) {
        }

        return new LinkedHashMap<>();
    }

    private GraphData buildGraph(
            Map<String, Object> network
    ) {

        GraphData graph =
                new GraphData();

        Object nodesObject =
                network.get("nodes");

        if (nodesObject instanceof List<?> nodes) {

            for (Object value : nodes) {

                if (!(value instanceof Map<?, ?> node)) {
                    continue;
                }

                String id =
                        firstString(
                                node,
                                "id",
                                "entityId",
                                "key"
                        );

                if (!id.isBlank()) {
                    graph.nodes.add(id);
                }
            }
        }

        Object edgesObject =
                network.get("edges");

        if (!(edgesObject instanceof List<?>)) {
            edgesObject =
                    network.get("connections");
        }

        if (!(edgesObject instanceof List<?>)) {
            edgesObject =
                    network.get("relationships");
        }

        if (edgesObject instanceof List<?> edges) {

            for (Object value : edges) {

                if (!(value instanceof Map<?, ?> edge)) {
                    continue;
                }

                String source =
                        firstString(
                                edge,
                                "source",
                                "sourceId",
                                "from"
                        );

                String target =
                        firstString(
                                edge,
                                "target",
                                "targetId",
                                "to"
                        );

                if (source.isBlank()
                        || target.isBlank()) {
                    continue;
                }

                graph.nodes.add(source);
                graph.nodes.add(target);

                graph.adjacency
                        .computeIfAbsent(
                                source,
                                key -> new HashSet<>()
                        )
                        .add(target);

                graph.adjacency
                        .computeIfAbsent(
                                target,
                                key -> new HashSet<>()
                        )
                        .add(source);
            }
        }

        for (String nodeId : graph.nodes) {

            graph.adjacency
                    .computeIfAbsent(
                            nodeId,
                            key -> new HashSet<>()
                    );
        }

        return graph;
    }

    private int countComponents(
            Set<String> nodes,
            Map<String, Set<String>> adjacency,
            String removedNode
    ) {

        Set<String> visited =
                new HashSet<>();

        int components = 0;

        for (String node : nodes) {

            if (node.equals(removedNode)
                    || visited.contains(node)) {
                continue;
            }

            components++;

            Queue<String> queue =
                    new ArrayDeque<>();

            queue.add(node);
            visited.add(node);

            while (!queue.isEmpty()) {

                String current =
                        queue.poll();

                for (String neighbour :
                        adjacency.getOrDefault(
                                current,
                                new HashSet<>()
                        )) {

                    if (neighbour.equals(
                            removedNode
                    )) {
                        continue;
                    }

                    if (visited.add(neighbour)) {
                        queue.add(neighbour);
                    }
                }
            }
        }

        return components;
    }

    private int countEdges(
            Map<String, Set<String>> adjacency
    ) {

        int total = 0;

        for (Set<String> neighbours :
                adjacency.values()) {

            total += neighbours.size();
        }

        return total / 2;
    }

    private double calculateFragmentationImpact(
            int before,
            int after
    ) {

        if (before <= 0) {
            return 0.0;
        }

        if (after <= before) {
            return 0.0;
        }

        return Math.min(
                100.0,
                ((double) (after - before)
                        / Math.max(1, before))
                        * 100.0
        );
    }

    private double calculateConnectivityImpact(
            int totalNodes,
            int degree
    ) {

        if (totalNodes <= 1) {
            return 0.0;
        }

        return Math.min(
                100.0,
                ((double) degree
                        / (totalNodes - 1))
                        * 100.0
        );
    }

    private Map<String, Object> findNode(
            Map<String, Object> network,
            String entityId
    ) {

        Object nodesObject =
                network.get("nodes");

        if (nodesObject instanceof List<?> nodes) {

            for (Object value : nodes) {

                if (!(value instanceof Map<?, ?> node)) {
                    continue;
                }

                String id =
                        firstString(
                                node,
                                "id",
                                "entityId",
                                "key"
                        );

                if (entityId.equals(id)) {

                    Map<String, Object> result =
                            new LinkedHashMap<>();

                    for (Map.Entry<?, ?> entry :
                            node.entrySet()) {

                        if (entry.getKey() != null) {

                            result.put(
                                    entry.getKey().toString(),
                                    entry.getValue()
                            );
                        }
                    }

                    return result;
                }
            }
        }

        return Map.of(
                "id",
                entityId
        );
    }

    private String firstString(
            Map<?, ?> map,
            String... keys
    ) {

        for (String key : keys) {

            Object value =
                    map.get(key);

            if (value != null
                    && !value.toString().isBlank()) {

                return value.toString();
            }
        }

        return "";
    }

    private String criticalityLevel(
            double score
    ) {

        if (score >= 80) {
            return "CRITICAL";
        }

        if (score >= 60) {
            return "HIGH";
        }

        if (score >= 40) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String buildInterpretation(
            int fragmentationIncrease,
            int directConnections,
            double score
    ) {

        if (fragmentationIncrease > 0) {

            return "Removing this entity increases network "
                    + "fragmentation, indicating that the entity "
                    + "may act as a structural bridge in the "
                    + "current graph.";
        }

        if (directConnections >= 5) {

            return "Removing this entity would eliminate "
                    + directConnections
                    + " direct network connections. "
                    + "The entity may be structurally important "
                    + "even though the graph remains connected.";
        }

        if (score >= 40) {

            return "The entity has measurable structural "
                    + "importance in the current network.";
        }

        return "The entity has limited structural impact "
                + "in the current graph.";
    }

    private double getDouble(
            Object value
    ) {

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

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private static class GraphData {

        private final Set<String> nodes =
                new HashSet<>();

        private final Map<String, Set<String>> adjacency =
                new HashMap<>();
    }
}