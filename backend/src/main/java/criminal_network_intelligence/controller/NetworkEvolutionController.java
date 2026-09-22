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

import criminal_network_intelligence.service.NetworkEvolutionService;

@RestController
@RequestMapping("/api/analytics/evolution")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class NetworkEvolutionController {

    private final NetworkEvolutionService networkEvolutionService;

    public NetworkEvolutionController(
            NetworkEvolutionService networkEvolutionService
    ) {
        this.networkEvolutionService =
                networkEvolutionService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>>
    getNetworkEvolution() {

        return ResponseEntity.ok(
                networkEvolutionService
                        .getNetworkEvolution()
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>>
    getEvolutionSummary() {

        return ResponseEntity.ok(
                networkEvolutionService
                        .getEvolutionSummary()
        );
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Map<String, Object>>>
    getCaseEvolution(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                networkEvolutionService
                        .getCaseEvolution(caseId)
        );
    }

    @GetMapping("/entity")
    public ResponseEntity<List<Map<String, Object>>>
    getEntityEvolution(
            @RequestParam String entityName
    ) {

        return ResponseEntity.ok(
                networkEvolutionService
                        .getEntityEvolution(entityName)
        );
    }
}