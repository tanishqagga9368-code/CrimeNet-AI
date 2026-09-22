package criminal_network_intelligence.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.GraphSyncService;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class GraphSyncController {

    private final GraphSyncService graphSyncService;

    public GraphSyncController(
            GraphSyncService graphSyncService
    ) {
        this.graphSyncService = graphSyncService;
    }

    /**
     * Sync intelligence records of a specific case.
     *
     * POST /api/graph/sync/{caseId}
     */
    @PostMapping("/sync/{caseId}")
    public ResponseEntity<Map<String, Object>> syncCase(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                graphSyncService.syncCase(caseId)
        );
    }

    /**
     * Sync all intelligence records.
     *
     * POST /api/graph/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncAll() {

        return ResponseEntity.ok(
                graphSyncService.syncAll()
        );
    }

    /**
     * Simple graph synchronization status endpoint.
     */
    @GetMapping("/sync/status")
    public ResponseEntity<Map<String, Object>> status() {

        return ResponseEntity.ok(
                Map.of(
                        "service",
                        "Graph Synchronization Engine",

                        "status",
                        "READY",

                        "source",
                        "PostgreSQL Intelligence Records",

                        "target",
                        "Neo4j Knowledge Graph"
                )
        );
    }
}