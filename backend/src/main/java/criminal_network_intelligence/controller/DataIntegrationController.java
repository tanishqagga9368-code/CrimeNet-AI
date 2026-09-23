package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.integration.DataIntegrationService;
import criminal_network_intelligence.integration.DataSourceType;
import criminal_network_intelligence.integration.ExternalDataResponse;

@RestController
@RequestMapping("/api/integration")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class DataIntegrationController {

    private final DataIntegrationService dataIntegrationService;

    public DataIntegrationController(
            DataIntegrationService dataIntegrationService
    ) {
        this.dataIntegrationService = dataIntegrationService;
    }

    @GetMapping("/sources")
    public ResponseEntity<List<Map<String, Object>>> getSources() {
        return ResponseEntity.ok(
                dataIntegrationService.getAvailableSources()
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(
                dataIntegrationService.getIntegrationSummary()
        );
    }

    @GetMapping("/fetch")
    public ResponseEntity<ExternalDataResponse> fetch(
            @RequestParam DataSourceType type,
            @RequestParam String identifier
    ) {
        return ResponseEntity.ok(
                dataIntegrationService.fetchFromSource(
                        type,
                        identifier
                )
        );
    }

    @GetMapping("/fetch-all")
    public ResponseEntity<List<ExternalDataResponse>> fetchAll(
            @RequestParam String identifier
    ) {
        return ResponseEntity.ok(
                dataIntegrationService.fetchFromAllSources(
                        identifier
                )
        );
    }

    @GetMapping("/source")
    public ResponseEntity<ExternalDataResponse> fetchSource(
            @RequestParam DataSourceType type,
            @RequestParam String identifier
    ) {
        return ResponseEntity.ok(
                dataIntegrationService.fetchFromSource(
                        type,
                        identifier
                )
        );
    }

    @PostMapping("/query")
    public ResponseEntity<?> queryDemoConnector(@RequestBody Map<String, Object> request) {
        String sourceType = String.valueOf(request.getOrDefault("sourceType", "FIR"));
        String query = String.valueOf(request.getOrDefault("query", ""));
        return ResponseEntity.ok(dataIntegrationService.queryDemoConnector(sourceType, query));
    }

    @PostMapping("/import")
    public ResponseEntity<?> importDemoRecord(@RequestBody Map<String, Object> request) {
        String caseId = String.valueOf(request.getOrDefault("caseId", "CR-2026-041"));
        String sourceType = String.valueOf(request.getOrDefault("sourceType", "FIR"));
        String title = String.valueOf(request.getOrDefault("title", "Authorized Registry Import"));
        String narrative = String.valueOf(request.getOrDefault("narrative", ""));
        String officer = String.valueOf(request.getOrDefault("officer", "Investigating Officer"));

        Map<String, Object> result = dataIntegrationService.importDemoRecord(caseId, sourceType, title, narrative, officer);
        return ResponseEntity.ok(result);
    }
}