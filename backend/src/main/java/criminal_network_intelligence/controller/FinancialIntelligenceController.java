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

import criminal_network_intelligence.service.FinancialIntelligenceService;

@RestController
@RequestMapping("/api/analytics/financial")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class FinancialIntelligenceController {

    private final FinancialIntelligenceService financialIntelligenceService;

    public FinancialIntelligenceController(
            FinancialIntelligenceService financialIntelligenceService
    ) {
        this.financialIntelligenceService =
                financialIntelligenceService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>>
    getFinancialIntelligence() {

        return ResponseEntity.ok(
                financialIntelligenceService
                        .getFinancialIntelligence()
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>>
    getFinancialSummary() {

        return ResponseEntity.ok(
                financialIntelligenceService
                        .getFinancialSummary()
        );
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Map<String, Object>>>
    getCaseFinancialIntelligence(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                financialIntelligenceService
                        .getCaseFinancialIntelligence(
                                caseId
                        )
        );
    }

    @GetMapping("/repeated")
    public ResponseEntity<List<Map<String, Object>>>
    getRepeatedTransfers() {

        return ResponseEntity.ok(
                financialIntelligenceService
                        .getRepeatedTransfers()
        );
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<Map<String, Object>>>
    getHighValueTransactions(
            @RequestParam(
                    defaultValue = "100000"
            )
            double threshold
    ) {

        return ResponseEntity.ok(
                financialIntelligenceService
                        .getHighValueTransactions(
                                threshold
                        )
        );
    }
}