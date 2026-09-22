package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.neo4j.Neo4jService;

@Service
public class SuspiciousPatternService {

    private final Neo4jService neo4jService;

    public SuspiciousPatternService(Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
    }

    public Map<String, Object> detectPatterns() {
        List<Map<String, Object>> keyPersons = neo4jService.getKeyPersons();
        List<Map<String, Object>> degreeData = neo4jService.getDegreeCentrality();
        Map<String, Object> networkData = neo4jService.getNetwork();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> connections = (List<Map<String, Object>>) networkData.getOrDefault("connections", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) networkData.getOrDefault("nodes", List.of());

        List<Map<String, Object>> patterns = new ArrayList<>();
        Map<String, Integer> degreeMap = new HashMap<>();

        for (Map<String, Object> item : degreeData) {
            String id = String.valueOf(item.getOrDefault("id", ""));
            degreeMap.put(id, getInteger(item.get("degree")));
        }

        // Pattern 1: High Connectivity Hubs (Key Persons)
        for (Map<String, Object> person : keyPersons) {
            String id = String.valueOf(person.getOrDefault("id", ""));
            int degree = degreeMap.getOrDefault(id, 0);
            String risk = String.valueOf(person.getOrDefault("risk", "LOW")).toUpperCase();

            if (degree >= 3) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("patternId", "PAT-HUB-" + id);
                p.put("patternType", "HIGH_NETWORK_CONNECTIVITY");
                p.put("title", "Central Network Hub Detected: " + person.get("name"));
                p.put("entityId", id);
                p.put("entityName", person.get("name"));
                p.put("entityType", person.get("type"));
                p.put("degree", degree);
                p.put("severity", "CRITICAL".equals(risk) || degree >= 5 ? "CRITICAL" : "HIGH");
                p.put("suspicionScore", Math.min(98, 50 + degree * 8 + ("CRITICAL".equals(risk) ? 15 : 10)));
                p.put("evidence", "Suspect maintains " + degree + " direct links spanning transport, telecommunications, shell entities, and financial nodes.");
                p.put("explanation", String.valueOf(person.getOrDefault("explanation", "High centrality index indicates key operational coordination role.")));
                p.put("recommendedAction", "Prioritize surveillance on direct neighbors and request CDR intercepts.");
                p.put("name", p.get("title"));
                p.put("pattern", p.get("patternType"));
                p.put("risk", p.get("severity"));
                p.put("score", p.get("suspicionScore"));
                p.put("description", p.get("evidence"));
                p.put("involvedEntities", java.util.List.of(String.valueOf(person.get("name"))));
                patterns.add(p);
            }
        }

        // Pattern 2: Financial Laundering & Hawala Pipeline
        for (Map<String, Object> conn : connections) {
            String rel = String.valueOf(conn.getOrDefault("relationship", ""));
            if ("FINANCIAL".equalsIgnoreCase(rel) || "TRANSFERRED_MONEY".equalsIgnoreCase(rel)) {
                String src = String.valueOf(conn.get("source"));
                String tgt = String.valueOf(conn.get("target"));
                String srcName = getNodeName(nodes, src);
                String tgtName = getNodeName(nodes, tgt);

                Map<String, Object> p = new LinkedHashMap<>();
                p.put("patternId", "PAT-FIN-" + src + "-" + tgt);
                p.put("patternType", "RAPID_MONEY_MOVEMENT");
                p.put("title", "High-Volume Illicit Transaction Pipeline: " + srcName + " -> " + tgtName);
                p.put("sourceEntity", srcName);
                p.put("targetEntity", tgtName);
                p.put("severity", "CRITICAL");
                p.put("suspicionScore", 92);
                p.put("evidence", conn.getOrDefault("description", "Large undeclared funds transfer identified between operational syndicate and financial facilitator."));
                p.put("explanation", "Direct capital injection from suspect " + srcName + " to suspected laundering nexus " + tgtName + " without commercial justification.");
                p.put("recommendedAction", "Issue formal notice under PMLA / FIU-IND for account freeze.");
                p.put("name", p.get("title"));
                p.put("pattern", p.get("patternType"));
                p.put("risk", p.get("severity"));
                p.put("score", p.get("suspicionScore"));
                p.put("description", p.get("evidence"));
                p.put("involvedEntities", java.util.List.of(srcName, tgtName));
                patterns.add(p);
            }
        }

        // Pattern 3: Repeated Intercepted Communications
        for (Map<String, Object> conn : connections) {
            String desc = String.valueOf(conn.getOrDefault("description", ""));
            String rel = String.valueOf(conn.getOrDefault("relationship", ""));
            if (desc.toLowerCase().contains("intercepted") || desc.toLowerCase().contains("calls") || "CALLS".equalsIgnoreCase(rel)) {
                String src = String.valueOf(conn.get("source"));
                String tgt = String.valueOf(conn.get("target"));
                String srcName = getNodeName(nodes, src);
                String tgtName = getNodeName(nodes, tgt);

                Map<String, Object> p = new LinkedHashMap<>();
                p.put("patternId", "PAT-COMM-" + src + "-" + tgt);
                p.put("patternType", "REPEATED_COMMUNICATION_CLUSTER");
                p.put("title", "High-Frequency Co-Conspirator Communication: " + srcName + " <-> " + tgtName);
                p.put("sourceEntity", srcName);
                p.put("targetEntity", tgtName);
                p.put("severity", "HIGH");
                p.put("suspicionScore", 86);
                p.put("evidence", desc.isBlank() ? "Repeated communication frequency significantly exceeds standard behavioral baseline." : desc);
                p.put("explanation", "Sustained high-volume exchanges between " + srcName + " and " + tgtName + " coinciding with operational crime windows.");
                p.put("recommendedAction", "Correlate cell tower location logs with physical incident timeline.");
                p.put("name", p.get("title"));
                p.put("pattern", p.get("patternType"));
                p.put("risk", p.get("severity"));
                p.put("score", p.get("suspicionScore"));
                p.put("description", p.get("evidence"));
                p.put("involvedEntities", java.util.List.of(srcName, tgtName));
                patterns.add(p);
            }
        }

        // Pattern 4: Shared Physical Infrastructure (Common Locations & Vehicles)
        Map<String, List<String>> locationVisitors = new HashMap<>();
        for (Map<String, Object> conn : connections) {
            String rel = String.valueOf(conn.getOrDefault("relationship", ""));
            if ("VISITED".equalsIgnoreCase(rel) || "LOCATED_AT".equalsIgnoreCase(rel) || "PARKED_AT".equalsIgnoreCase(rel)) {
                String loc = String.valueOf(conn.get("target"));
                String visitor = String.valueOf(conn.get("source"));
                locationVisitors.computeIfAbsent(loc, k -> new ArrayList<>()).add(visitor);
            }
        }

        for (Map.Entry<String, List<String>> entry : locationVisitors.entrySet()) {
            if (entry.getValue().size() >= 2) {
                String locId = entry.getKey();
                String locName = getNodeName(nodes, locId);
                List<String> visitorNames = entry.getValue().stream().map(v -> getNodeName(nodes, v)).toList();

                Map<String, Object> p = new LinkedHashMap<>();
                p.put("patternId", "PAT-INFRA-" + locId);
                p.put("patternType", "COMMON_LOCATION_MEETING_POINT");
                p.put("title", "Shared Operational Hotspot: " + locName);
                p.put("entityId", locId);
                p.put("entityName", locName);
                p.put("severity", "HIGH");
                p.put("suspicionScore", 84);
                p.put("evidence", visitorNames.size() + " separate criminal entities convergent on " + locName + ": " + String.join(", ", visitorNames));
                p.put("explanation", "Premises acts as joint logistical assembly point and vehicle staging ground for organized group.");
                p.put("recommendedAction", "Deploy static surveillance and request CCTV footage preservation.");
                p.put("name", p.get("title"));
                p.put("pattern", p.get("patternType"));
                p.put("risk", p.get("severity"));
                p.put("score", p.get("suspicionScore"));
                p.put("description", p.get("evidence"));
                p.put("involvedEntities", visitorNames);
                patterns.add(p);
            }
        }

        // Pattern 5: Shared Phone / Telecommunication Identifier
        Map<String, List<String>> phoneUsers = new HashMap<>();
        for (Map<String, Object> conn : connections) {
            String rel = String.valueOf(conn.getOrDefault("relationship", "")).toUpperCase();
            if ("USES".equals(rel) || "CALLS".equals(rel) || "CALL".equals(rel) || "CONTACTED".equals(rel)) {
                String src = String.valueOf(conn.get("source"));
                String tgt = String.valueOf(conn.get("target"));
                String tgtType = getNodeType(nodes, tgt);
                String srcType = getNodeType(nodes, src);
                if ("PHONE".equalsIgnoreCase(tgtType)) {
                    phoneUsers.computeIfAbsent(tgt, k -> new ArrayList<>()).add(src);
                } else if ("PHONE".equalsIgnoreCase(srcType)) {
                    phoneUsers.computeIfAbsent(src, k -> new ArrayList<>()).add(tgt);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : phoneUsers.entrySet()) {
            List<String> distinctUsers = entry.getValue().stream().distinct().toList();
            if (distinctUsers.size() >= 2) {
                String phoneId = entry.getKey();
                String phoneName = getNodeName(nodes, phoneId);
                List<String> userNames = distinctUsers.stream().map(u -> getNodeName(nodes, u)).toList();

                Map<String, Object> p = new LinkedHashMap<>();
                p.put("patternId", "PAT-PHONE-" + phoneId);
                p.put("patternType", "SHARED_PHONE_IDENTIFIER");
                p.put("title", "Shared Telecommunication Identifier: " + phoneName);
                p.put("entityId", phoneId);
                p.put("entityName", phoneName);
                p.put("severity", "CRITICAL");
                p.put("suspicionScore", 90);
                p.put("evidence", distinctUsers.size() + " separate operational subjects linked to phone " + phoneName + ": " + String.join(", ", userNames));
                p.put("explanation", "Multiple distinct subjects share or communicate through a common telecommunication device.");
                p.put("recommendedAction", "Obtain urgent tower dump records and subscriber CDR binding logs.");
                p.put("name", p.get("title"));
                p.put("pattern", p.get("patternType"));
                p.put("risk", p.get("severity"));
                p.put("score", p.get("suspicionScore"));
                p.put("description", p.get("evidence"));
                p.put("involvedEntities", userNames);
                patterns.add(p);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("analysis", "SUSPICIOUS_PATTERN_DETECTION");
        result.put("totalIndicators", patterns.size());
        result.put("indicators", patterns);
        result.put("patterns", patterns);
        result.put("averageRiskScore", patterns.stream().mapToInt(p -> (Integer) p.getOrDefault("suspicionScore", 50)).average().orElse(75.0));
        result.put("message", patterns.isEmpty()
                ? "No high-risk pattern indicators detected."
                : patterns.size() + " critical intelligence patterns detected across the criminal network.");

        return result;
    }

    private String getNodeType(List<Map<String, Object>> nodes, String id) {
        for (Map<String, Object> node : nodes) {
            if (id.equalsIgnoreCase(String.valueOf(node.get("id")))) {
                return String.valueOf(node.getOrDefault("type", ""));
            }
        }
        return "";
    }

    private String getNodeName(List<Map<String, Object>> nodes, String id) {
        for (Map<String, Object> node : nodes) {
            if (id.equalsIgnoreCase(String.valueOf(node.get("id")))) {
                return String.valueOf(node.getOrDefault("name", id));
            }
        }
        return id;
    }

    private int getInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
