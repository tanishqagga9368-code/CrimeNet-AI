package criminal_network_intelligence.controller;

import criminal_network_intelligence.service.InvestigationReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class InvestigationReportController {

    private final InvestigationReportService
            investigationReportService;

    public InvestigationReportController(
            InvestigationReportService investigationReportService
    ) {
        this.investigationReportService =
                investigationReportService;
    }

    /**
     * Generate complete investigation report.
     *
     * Example:
     * /api/reports/case/CR-2026-041
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<Map<String, Object>>
    generateReport(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                investigationReportService
                        .generateReport(caseId)
        );
    }

    /**
     * Get lightweight report summary.
     *
     * Example:
     * /api/reports/case/CR-2026-041/summary
     */
    @GetMapping("/case/{caseId}/summary")
    public ResponseEntity<Map<String, Object>>
    getReportSummary(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                investigationReportService
                        .getReportSummary(caseId)
        );
    }
}