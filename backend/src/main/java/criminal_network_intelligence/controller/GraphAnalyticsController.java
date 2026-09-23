package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.GraphAnalyticsService;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class GraphAnalyticsController {

    private final GraphAnalyticsService graphAnalyticsService;

    public GraphAnalyticsController(
            GraphAnalyticsService graphAnalyticsService
    ) {
        this.graphAnalyticsService = graphAnalyticsService;
    }

    /**
     * Degree centrality of all graph entities.
     */
    @GetMapping("/degree-centrality")
    public ResponseEntity<List<Map<String, Object>>> getDegreeCentrality() {

        return ResponseEntity.ok(
                graphAnalyticsService.getDegreeCentrality()
        );
    }

    /**
     * Key persons / highly connected investigation leads.
     */
    @GetMapping("/key-persons")
    public ResponseEntity<List<Map<String, Object>>> getKeyPersons() {

        return ResponseEntity.ok(
                graphAnalyticsService.getKeyPersons()
        );
    }

    /**
     * Overall graph statistics.
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {

        return ResponseEntity.ok(
                graphAnalyticsService.getStatistics()
        );
    }

    /**
     * Analyze a specific entity.
     */
    @GetMapping("/entity/{entityId}")
    public ResponseEntity<Map<String, Object>> analyzeEntity(
            @PathVariable String entityId
    ) {

        return ResponseEntity.ok(
                graphAnalyticsService.analyzeEntity(entityId)
        );
    }

    /**
     * Find connected entities within requested hops.
     *
     * Example:
     * /api/analytics/entity/C001/connected?hops=2
     */
    @GetMapping("/entity/{entityId}/connected")
    public ResponseEntity<List<Map<String, Object>>> findConnectedEntities(
            @PathVariable String entityId,
            @RequestParam(defaultValue = "2") int hops
    ) {

        return ResponseEntity.ok(
                graphAnalyticsService.findConnectedEntities(
                        entityId,
                        hops
                )
        );
    }

    @GetMapping("/betweenness-centrality")
    public ResponseEntity<List<Map<String, Object>>> getBetweennessCentrality() {
        return ResponseEntity.ok(graphAnalyticsService.getBetweennessCentrality());
    }

    @GetMapping("/pagerank")
    public ResponseEntity<List<Map<String, Object>>> getPageRank() {
        return ResponseEntity.ok(graphAnalyticsService.getPageRank());
    }

    @GetMapping("/bridge-persons")
    public ResponseEntity<List<Map<String, Object>>> getBridgePersons() {
        return ResponseEntity.ok(graphAnalyticsService.getBridgePersons());
    }
}