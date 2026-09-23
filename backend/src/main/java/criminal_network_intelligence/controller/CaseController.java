package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.model.Case;
import criminal_network_intelligence.service.CaseService;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    /**
     * Get all investigation cases.
     *
     * GET /api/cases
     */
    @GetMapping
    public ResponseEntity<List<Case>> getCases() {

        return ResponseEntity.ok(
                caseService.getAllCases()
        );
    }

    /**
     * Get a single case.
     *
     * GET /api/cases/{caseId}
     */
    @GetMapping("/{caseId}")
    public ResponseEntity<?> getCase(
            @PathVariable String caseId) {

        Case caseData = caseService.getCaseById(caseId);

        if (caseData == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Case not found",
                            "caseId", caseId
                    ));
        }

        return ResponseEntity.ok(caseData);
    }

    /**
     * Create a new investigation case.
     *
     * POST /api/cases
     */
    @PostMapping
    public ResponseEntity<?> createCase(
            @RequestBody Case caseData) {

        try {

            Case createdCase =
                    caseService.createCase(caseData);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdCase);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * Update an existing case.
     *
     * PUT /api/cases/{caseId}
     */
    @PutMapping("/{caseId}")
    public ResponseEntity<?> updateCase(
            @PathVariable String caseId,
            @RequestBody Case updatedCase) {

        Case caseData =
                caseService.updateCase(caseId, updatedCase);

        if (caseData == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Case not found",
                            "caseId", caseId
                    ));
        }

        return ResponseEntity.ok(caseData);
    }

    /**
     * Delete a case.
     *
     * DELETE /api/cases/{caseId}
     */
    @DeleteMapping("/{caseId}")
    public ResponseEntity<?> deleteCase(
            @PathVariable String caseId) {

        boolean deleted =
                caseService.deleteCase(caseId);

        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Case not found",
                            "caseId", caseId
                    ));
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Case deleted successfully",
                        "caseId", caseId
                )
        );
    }

    /**
     * Case statistics.
     *
     * GET /api/cases/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCaseStats() {

        List<Case> allCases =
                caseService.getAllCases();

        long activeCases =
                caseService.getActiveCaseCount();

        return ResponseEntity.ok(
                Map.of(
                        "totalCases", allCases.size(),
                        "activeCases", activeCases
                )
        );
    }
}