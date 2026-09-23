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

import criminal_network_intelligence.service.CrossCaseConnectionService;

@RestController
@RequestMapping("/api/cross-case")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class CrossCaseConnectionController {

    private final CrossCaseConnectionService
            crossCaseConnectionService;

    public CrossCaseConnectionController(
            CrossCaseConnectionService crossCaseConnectionService
    ) {
        this.crossCaseConnectionService =
                crossCaseConnectionService;
    }

    /**
     * Get all cross-case connections.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>>
    getCrossCaseConnections() {

        return ResponseEntity.ok(
                crossCaseConnectionService
                        .findCrossCaseConnections()
        );
    }

    /**
     * Get cross-case connections related to a case.
     *
     * Example:
     * /api/cross-case/case/CR-2026-041
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Map<String, Object>>>
    getConnectionsForCase(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                crossCaseConnectionService
                        .findConnectionsForCase(caseId)
        );
    }

    /**
     * Search cross-case connections by entity name.
     *
     * Example:
     * /api/cross-case/entity?name=Ravi%20Kumar
     */
    @GetMapping("/entity")
    public ResponseEntity<List<Map<String, Object>>>
    getEntityConnections(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                crossCaseConnectionService
                        .findEntityConnections(name)
        );
    }
}