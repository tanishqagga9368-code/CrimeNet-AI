package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.EvidenceChainService;

@RestController
@RequestMapping("/api/evidence-chain")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class EvidenceChainController {

    private final EvidenceChainService evidenceChainService;

    public EvidenceChainController(
            EvidenceChainService evidenceChainService
    ) {
        this.evidenceChainService =
                evidenceChainService;
    }

    /**
     * Get complete evidence intelligence chain
     * for a particular case.
     *
     * Example:
     * /api/evidence-chain/case/CR-2026-041
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<Map<String, Object>>
    getCaseEvidenceChain(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                evidenceChainService
                        .buildCaseEvidenceChain(caseId)
        );
    }

    /**
     * Get evidence summary grouped by case.
     */
    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>>
    getEvidenceSummary() {

        return ResponseEntity.ok(
                evidenceChainService
                        .getEvidenceSummary()
        );
    }
}