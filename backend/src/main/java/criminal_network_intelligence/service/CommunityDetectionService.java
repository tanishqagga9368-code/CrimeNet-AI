package criminal_network_intelligence.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.neo4j.Neo4jService;

@Service
public class CommunityDetectionService {

    private final Neo4jService neo4jService;

    public CommunityDetectionService(Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
    }

    public Map<String, Object> detectCommunities() {

        Map<String, Object> network = neo4jService.getNetwork();

        Graph graph = buildGraph(network);

        List<Map<String, Object>> communities =
                buildCommunities(graph);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("success", true);
        response.put("totalNodes", graph.nodes.size());
        response.put("totalEdges", countEdges(graph));
        response.put("totalCommunities", communities.size());
        response.put("communities", communities);
        response.put(
                "interpretation",
                "Community detection identifies groups of closely "
                        + "connected entities for investigation support. "
                        + "A network cluster does not establish criminal "
                        + "association or guilt."
        );

        return response;
    }

    public Map<String, Object> getEntityCommunity(
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
                neo4jService.getNetwork();

        Graph graph =
                buildGraph(network);

        List<Set<String>> components =
                connectedComponents(graph);

        for (int i = 0; i < components.size(); i++) {

            Set<String> community =
                    components.get(i);

            if (community.contains(entityId)) {

                Map<String, Object> result =
                        buildCommunity(
                                i + 1,
                                community,
                                graph
                        );

                response.put("success", true);
                response.put("community", result);

                return response;
            }
        }

        response.put("success", false);
        response.put(
                "message",
                "Entity was not found in the current graph."
        );

        return response;
    }

    private List<Map<String, Object>> buildCommunities(
            Graph graph
    ) {

        List<Set<String>> components =
                connectedComponents(graph);

        List<Map<String, Object>> result =
                new ArrayList<>();

        int communityId = 1;

        for (Set<String> component : components) {

            result.add(
                    buildCommunity(
                            communityId++,
                            component,
                            graph
                    )
            );
        }

        result.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(
                                        item.get("size")
                                )
                ).reversed()
        );

        return result;
    }

    private Map<String, Object> buildCommunity(
            int communityId,
            Set<String> members,
            Graph graph
    ) {

        int internalEdges = 0;

        int possibleEdges =
                members.size() > 1
                        ? members.size()
                        * (members.size() - 1)
                        / 2
                        : 0;

        for (String member : members) {

            for (String neighbour :
                    graph.adjacency.getOrDefault(
                            member,
                            Collections.emptySet()
                    )) {

                if (members.contains(neighbour)) {
                    internalEdges++;
                }
            }
        }

        internalEdges /= 2;

        double density =
                possibleEdges == 0
                        ? 0.0
                        : (double) internalEdges
                        / possibleEdges;

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "communityId",
                "COMMUNITY-" + communityId
        );

        result.put(
                "size",
                members.size()
        );

        result.put(
                "members",
                new ArrayList<>(members)
        );

        result.put(
                "internalConnections",
                internalEdges
        );

        result.put(
                "possibleConnections",
                possibleEdges
        );

        result.put(
                "density",
                round(density)
        );

        result.put(
                "clusterStrength",
                clusterStrength(density)
        );

        result.put(
                "investigationLead",
                members.size() >= 3
                        && internalEdges >= 2
        );

        result.put(
                "explanation",
                buildExplanation(
                        members.size(),
                        internalEdges,
                        density
                )
        );

        return result;
    }

    private List<Set<String>> connectedComponents(
            Graph graph
    ) {

        List<Set<String>> components =
                new ArrayList<>();

        Set<String> visited =
                new HashSet<>();

        for (String node : graph.nodes) {

            if (visited.contains(node)) {
                continue;
            }

            Set<String> component =
                    new LinkedHashSet<>();

            Queue<String> queue =
                    new ArrayDeque<>();

            queue.add(node);
            visited.add(node);

            while (!queue.isEmpty()) {

                String current =
                        queue.poll();

                component.add(current);

                for (String neighbour :
                        graph.adjacency.getOrDefault(
                                current,
                                Collections.emptySet()
                        )) {

                    if (visited.add(neighbour)) {
                        queue.add(neighbour);
                    }
                }
            }

            components.add(component);
        }

        return components;
    }

    private Graph buildGraph(
            Map<String, Object> network
    ) {

        Graph graph = new Graph();

        if (network == null) {
            return graph;
        }

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
                                key -> new LinkedHashSet<>()
                        )
                        .add(target);

                graph.adjacency
                        .computeIfAbsent(
                                target,
                                key -> new LinkedHashSet<>()
                        )
                        .add(source);
            }
        }

        for (String node : graph.nodes) {

            graph.adjacency
                    .computeIfAbsent(
                            node,
                            key -> new LinkedHashSet<>()
                    );
        }

        return graph;
    }

    private int countEdges(Graph graph) {

        int total = 0;

        for (Set<String> neighbours :
                graph.adjacency.values()) {

            total += neighbours.size();
        }

        return total / 2;
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

    private String clusterStrength(
            double density
    ) {

        if (density >= 0.70) {
            return "VERY_STRONG";
        }

        if (density >= 0.45) {
            return "STRONG";
        }

        if (density >= 0.20) {
            return "MODERATE";
        }

        return "WEAK";
    }

    private String buildExplanation(
            int size,
            int connections,
            double density
    ) {

        if (size >= 5 && density >= 0.50) {

            return "This group contains several entities with "
                    + "strong internal connectivity and may "
                    + "warrant detailed relationship review.";
        }

        if (size >= 3 && connections >= 2) {

            return "Multiple connected entities form a "
                    + "potential investigation cluster.";
        }

        return "The group represents a connected component "
                + "in the current intelligence graph.";
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

    private double round(double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private static class Graph {

        private final Set<String> nodes =
                new LinkedHashSet<>();

        private final Map<String, Set<String>> adjacency =
                new LinkedHashMap<>();
    }
}