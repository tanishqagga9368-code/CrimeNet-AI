package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.RelationshipAnalysisService;

@RestController
@RequestMapping("/api/analytics/relationships")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class RelationshipAnalysisController {

    private final RelationshipAnalysisService relationshipAnalysisService;

    public RelationshipAnalysisController(
            RelationshipAnalysisService relationshipAnalysisService
    ) {
        this.relationshipAnalysisService =
                relationshipAnalysisService;
    }

    @GetMapping("/strength")
    public ResponseEntity<List<Map<String, Object>>>
    getRelationshipStrength() {

        return ResponseEntity.ok(
                relationshipAnalysisService
                        .getRelationshipStrength()
        );
    }

    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>>
    analyzeRelationship(
            @RequestParam String sourceId,
            @RequestParam String targetId
    ) {

        return ResponseEntity.ok(
                relationshipAnalysisService
                        .analyzeRelationship(
                                sourceId,
                                targetId
                        )
        );
    }

    @GetMapping("/path")
    public ResponseEntity<Map<String, Object>>
    findPath(
            @RequestParam String sourceId,
            @RequestParam String targetId,
            @RequestParam(defaultValue = "3")
            int maxHops
    ) {

        return ResponseEntity.ok(
                relationshipAnalysisService.findPath(
                        sourceId,
                        targetId,
                        maxHops
                )
        );
    }

    @GetMapping("/hidden")
    public ResponseEntity<List<Map<String, Object>>>
    getHiddenRelationships() {

        return ResponseEntity.ok(
                relationshipAnalysisService
                        .findHiddenRelationships()
        );
    }
}