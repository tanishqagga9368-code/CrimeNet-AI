package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.AnomalyDetectionService;

@RestController
@RequestMapping("/api/analytics/anomalies")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class AnomalyDetectionController {

    private final AnomalyDetectionService anomalyDetectionService;

    public AnomalyDetectionController(
            AnomalyDetectionService anomalyDetectionService
    ) {
        this.anomalyDetectionService =
                anomalyDetectionService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>>
    detectAnomalies() {

        return ResponseEntity.ok(
                anomalyDetectionService.detectAnomalies()
        );
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Map<String, Object>>>
    detectCaseAnomalies(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                anomalyDetectionService
                        .detectCaseAnomalies(caseId)
        );
    }
}