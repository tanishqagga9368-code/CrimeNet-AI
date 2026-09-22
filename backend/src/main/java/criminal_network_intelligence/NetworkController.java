package criminal_network_intelligence;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.neo4j.Neo4jService;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequestMapping("/api/network")
public class NetworkController {

    private final Neo4jService neo4jService;

    public NetworkController(Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
    }

    /**
     * Complete investigation network.
     *
     * GET /api/network
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNetwork() {

        return ResponseEntity.ok(
                neo4jService.getNetwork()
        );
    }

    /**
     * Case-specific investigation network.
     *
     * GET /api/network/case/{caseId}
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<Map<String, Object>> getCaseNetwork(@PathVariable String caseId) {

        return ResponseEntity.ok(
                neo4jService.getCaseNetwork(caseId)
        );
    }

    /**
     * Get direct connections of an entity.
     *
     * GET /api/network/entity/{entityId}/connections
     */
    @GetMapping(
            "/entity/{entityId}/connections"
    )
    public ResponseEntity<List<Map<String, Object>>>
    getEntityConnections(
            @PathVariable String entityId) {

        return ResponseEntity.ok(
                neo4jService.getEntityConnections(
                        entityId
                )
        );
    }

    /**
     * Multi-hop entity discovery.
     *
     * GET /api/network/entity/{entityId}/connected?hops=3
     */
    @GetMapping(
            "/entity/{entityId}/connected"
    )
    public ResponseEntity<List<Map<String, Object>>>
    getConnectedEntities(

            @PathVariable String entityId,

            @RequestParam(
                    defaultValue = "2"
            )
            int hops) {

        return ResponseEntity.ok(
                neo4jService.findConnectedEntities(
                        entityId,
                        hops
                )
        );
    }

    /**
     * Degree centrality.
     *
     * GET /api/network/analytics/degree-centrality
     */
    @GetMapping(
            "/analytics/degree-centrality"
    )
    public ResponseEntity<List<Map<String, Object>>>
    getDegreeCentrality() {

        return ResponseEntity.ok(
                neo4jService.getDegreeCentrality()
        );
    }

    /**
     * Key person detection.
     *
     * GET /api/network/analytics/key-persons
     */
    @GetMapping(
            "/analytics/key-persons"
    )
    public ResponseEntity<List<Map<String, Object>>>
    getKeyPersons() {

        return ResponseEntity.ok(
                neo4jService.getKeyPersons()
        );
    }

    /**
     * Graph statistics.
     *
     * GET /api/network/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>>
    getStatistics() {

        return ResponseEntity.ok(
                neo4jService.getGraphStatistics()
        );
    }

    /**
     * Create/update graph entity.
     *
     * POST /api/network/node
     */
    @PostMapping("/node")
    public ResponseEntity<?> createNode(
            @RequestBody Map<String, Object> request) {

        String id =
                String.valueOf(
                        request.getOrDefault(
                                "id",
                                ""
                        )
                );

        String name =
                String.valueOf(
                        request.getOrDefault(
                                "name",
                                "Unknown Entity"
                        )
                );

        String type =
                String.valueOf(
                        request.getOrDefault(
                                "type",
                                "ENTITY"
                        )
                );

        String risk =
                String.valueOf(
                        request.getOrDefault(
                                "risk",
                                "LOW"
                        )
                );

        String phone =
                String.valueOf(
                        request.getOrDefault(
                                "phone",
                                ""
                        )
                );

        String location =
                String.valueOf(
                        request.getOrDefault(
                                "location",
                                ""
                        )
                );

        if (id.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Entity ID is required"
                            )
                    );
        }

        return ResponseEntity.ok(
                neo4jService.createNode(
                        id,
                        name,
                        type,
                        risk,
                        phone,
                        location
                )
        );
    }

    /**
     * Create relationship between graph entities.
     *
     * POST /api/network/relationship
     */
    @PostMapping("/relationship")
    public ResponseEntity<?> createRelationship(
            @RequestBody Map<String, Object> request) {

        String sourceId =
                String.valueOf(
                        request.getOrDefault(
                                "sourceId",
                                ""
                        )
                );

        String targetId =
                String.valueOf(
                        request.getOrDefault(
                                "targetId",
                                ""
                        )
                );

        String relationshipType =
                String.valueOf(
                        request.getOrDefault(
                                "relationshipType",
                                "ASSOCIATED_WITH"
                        )
                );

        String description =
                String.valueOf(
                        request.getOrDefault(
                                "description",
                                ""
                        )
                );

        if (sourceId.isBlank()
                || targetId.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "sourceId and targetId are required"
                            )
                    );
        }

        return ResponseEntity.ok(
                neo4jService.createRelationship(
                        sourceId,
                        targetId,
                        relationshipType,
                        description
                )
        );
    }
}