package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.BlockchainAuditService;

@RestController
@RequestMapping("/api/blockchain")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class BlockchainAuditController {

    private final BlockchainAuditService blockchainAuditService;

    public BlockchainAuditController(
            BlockchainAuditService blockchainAuditService
    ) {
        this.blockchainAuditService =
                blockchainAuditService;
    }

    /**
     * Record an evidence event in the private ledger.
     *
     * Example:
     * POST /api/blockchain/evidence/1/record
     */
    @PostMapping("/evidence/{evidenceId}/record")
    public ResponseEntity<Map<String, Object>>
    recordEvidenceEvent(
            @PathVariable Long evidenceId,
            @RequestParam(
                    defaultValue = "EVIDENCE_RECORDED"
            )
            String eventType,
            @RequestParam(
                    defaultValue = "SYSTEM"
            )
            String performedBy
    ) {

        return ResponseEntity.ok(
                blockchainAuditService.recordEvidenceEvent(
                        evidenceId,
                        eventType,
                        performedBy
                )
        );
    }

    /**
     * Get complete private blockchain ledger.
     */
    @GetMapping("/ledger")
    public ResponseEntity<List<Map<String, Object>>>
    getLedger() {

        return ResponseEntity.ok(
                blockchainAuditService.getLedger()
        );
    }

    /**
     * Get blockchain ledger for a case.
     */
    @GetMapping("/ledger/case/{caseId}")
    public ResponseEntity<List<Map<String, Object>>>
    getCaseLedger(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                blockchainAuditService.getCaseLedger(
                        caseId
                )
        );
    }

    /**
     * Get blockchain ledger for evidence.
     */
    @GetMapping("/ledger/evidence/{evidenceId}")
    public ResponseEntity<List<Map<String, Object>>>
    getEvidenceLedger(
            @PathVariable Long evidenceId
    ) {

        return ResponseEntity.ok(
                blockchainAuditService.getEvidenceLedger(
                        evidenceId
                )
        );
    }

    /**
     * Verify the entire hash chain.
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>>
    verifyLedger() {

        return ResponseEntity.ok(
                blockchainAuditService.verifyLedger()
        );
    }
}
