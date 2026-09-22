package criminal_network_intelligence.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import criminal_network_intelligence.model.Evidence;
import criminal_network_intelligence.service.EvidenceService;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    /**
     * Get all evidence.
     *
     * GET /api/evidence
     */
    @GetMapping
    public ResponseEntity<List<Evidence>> getAllEvidence() {

        return ResponseEntity.ok(
                evidenceService.getAllEvidence()
        );
    }

    /**
     * Get evidence for a case.
     *
     * GET /api/evidence/case/{caseId}
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Evidence>> getCaseEvidence(
            @PathVariable String caseId) {

        return ResponseEntity.ok(
                evidenceService.getEvidenceByCase(caseId)
        );
    }

    /**
     * Get one evidence record.
     *
     * GET /api/evidence/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEvidence(
            @PathVariable Long id) {

        Optional<Evidence> evidence =
                evidenceService.getEvidenceById(id);

        if (evidence.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Evidence not found",
                            "evidenceId", id
                    ));
        }

        return ResponseEntity.ok(
                evidence.get()
        );
    }

    /**
     * Upload evidence.
     *
     * POST /api/evidence/upload
     *
     * Multipart fields:
     * caseId
     * evidenceType
     * uploadedBy
     * description
     * file
     */
    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadEvidence(

            @RequestParam String caseId,

            @RequestParam(required = false)
            String evidenceType,

            @RequestParam(required = false)
            String uploadedBy,

            @RequestParam(required = false)
            String description,

            @RequestParam("file")
            MultipartFile file) {

        try {

            Evidence evidence =
                    evidenceService.uploadEvidence(
                            caseId,
                            evidenceType,
                            uploadedBy,
                            description,
                            file
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(evidence);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", e.getMessage()
                    ));

        } catch (IOException e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Unable to process evidence file"
                    ));
        }
    }

    /**
     * Verify evidence integrity.
     *
     * POST /api/evidence/{id}/verify
     */
    @PostMapping(
            value = "/{id}/verify",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> verifyEvidence(

            @PathVariable Long id,

            @RequestParam("file")
            MultipartFile file) {

        try {

            Evidence evidence =
                    evidenceService.verifyEvidence(
                            id,
                            file
                    );

            boolean verified =
                    "VERIFIED".equals(
                            evidence.getIntegrityStatus()
                    );

            if (verified) {

                return ResponseEntity.ok(
                        Map.of(
                                "verified", true,
                                "status", "VERIFIED",
                                "message",
                                "Evidence integrity verified successfully",
                                "evidence", evidence
                        )
                );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "verified", false,
                            "status",
                            "INTEGRITY_VIOLATION",
                            "message",
                            "Evidence integrity violation detected",
                            "evidence", evidence
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", e.getMessage()
                    ));

        } catch (IOException e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Unable to verify evidence file"
                    ));
        }
    }

    /**
     * Calculate SHA-256 for a file without creating
     * an evidence record.
     *
     * POST /api/evidence/hash
     */
    @PostMapping(
            value = "/hash",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> calculateHash(
            @RequestParam("file")
            MultipartFile file) {

        try {

            if (file == null || file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error",
                                "File cannot be empty"
                        ));
            }

            String hash =
                    evidenceService.calculateSha256(
                            file.getBytes()
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "fileName",
                            file.getOriginalFilename(),
                            "sha256Hash",
                            hash,
                            "algorithm",
                            "SHA-256"
                    )
            );

        } catch (IOException e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Unable to calculate file hash"
                    ));
        }
    }
}