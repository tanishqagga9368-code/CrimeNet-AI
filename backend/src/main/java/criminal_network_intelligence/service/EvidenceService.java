package criminal_network_intelligence.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import criminal_network_intelligence.model.Evidence;
import criminal_network_intelligence.repository.EvidenceRepository;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final BlockchainAuditService blockchainAuditService;
    private final AuditLogService auditLogService;

    public EvidenceService(
            EvidenceRepository evidenceRepository,
            BlockchainAuditService blockchainAuditService,
            AuditLogService auditLogService
    ) {
        this.evidenceRepository = evidenceRepository;
        this.blockchainAuditService = blockchainAuditService;
        this.auditLogService = auditLogService;
    }

    /**
     * Get all evidence records.
     */
    public List<Evidence> getAllEvidence() {

        return evidenceRepository
                .findAllByOrderByUploadedAtDesc();
    }

    /**
     * Get evidence belonging to a particular case.
     */
    public List<Evidence> getEvidenceByCase(String caseId) {

        return evidenceRepository
                .findByCaseIdOrderByUploadedAtDesc(caseId);
    }

    /**
     * Get evidence by ID.
     */
    public Optional<Evidence> getEvidenceById(Long id) {

        return evidenceRepository.findById(id);
    }

    /**
     * Upload evidence and generate SHA-256 hash.
     *
     * The actual confidential file is NOT stored in the database.
     * Only evidence metadata and cryptographic hash are stored.
     */
    public Evidence uploadEvidence(
            String caseId,
            String evidenceType,
            String uploadedBy,
            String description,
            MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Evidence file cannot be empty"
            );
        }

        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException(
                    "Case ID is required"
            );
        }

        String hash = calculateSha256(file.getBytes());

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "unknown-file";
        }

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        String type = evidenceType;

        if (type == null || type.isBlank()) {
            type = detectEvidenceType(fileName);
        }

        String officer = uploadedBy;

        if (officer == null || officer.isBlank()) {
            officer = "Investigation Officer";
        }

        Evidence evidence = new Evidence();

        evidence.setCaseId(caseId);
        evidence.setOriginalFileName(fileName);
        evidence.setEvidenceType(type);
        evidence.setContentType(contentType);
        evidence.setSizeBytes(file.getSize());
        evidence.setSha256Hash(hash);
        evidence.setIntegrityStatus("VERIFIED");
        evidence.setUploadedBy(officer);
        evidence.setDescription(description);
        evidence.setUploadedAt(LocalDateTime.now());
        evidence.setVerifiedAt(LocalDateTime.now());

        Evidence saved = evidenceRepository.save(evidence);
        try {
            blockchainAuditService.recordEvidenceEvent(saved.getId(), "EVIDENCE_UPLOADED", officer);
            auditLogService.record(
                    officer,
                    "OFFICER",
                    "UPLOAD_EVIDENCE",
                    "EVIDENCE",
                    String.valueOf(saved.getId()),
                    caseId,
                    "Uploaded seized evidence file '" + fileName + "' (" + type + ") with SHA-256 hash " + hash.substring(0, 12) + "...",
                    "127.0.0.1"
            );
        } catch (Exception ignored) {}

        return saved;
    }

    /**
     * Verify an uploaded file against the original SHA-256 hash.
     */
    public Evidence verifyEvidence(
            Long evidenceId,
            MultipartFile currentFile) throws IOException {

        if (currentFile == null || currentFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "File for verification cannot be empty"
            );
        }

        Evidence evidence = evidenceRepository
                .findById(evidenceId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Evidence record not found"
                        )
                );

        String currentHash =
                calculateSha256(currentFile.getBytes());

        boolean valid = MessageDigest.isEqual(
                evidence.getSha256Hash()
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8),
                currentHash
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        evidence.setVerifiedAt(LocalDateTime.now());

        if (valid) {
            evidence.setIntegrityStatus("VERIFIED");
        } else {
            evidence.setIntegrityStatus("INTEGRITY_VIOLATION");
        }

        return evidenceRepository.save(evidence);
    }

    /**
     * Calculate SHA-256 hash.
     */
    public String calculateSha256(byte[] data) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(data);

            StringBuilder hexString =
                    new StringBuilder();

            for (byte hashByte : hashBytes) {

                String hex =
                        Integer.toHexString(
                                0xff & hashByte
                        );

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }

    /**
     * Automatically determine evidence type from extension.
     */
    private String detectEvidenceType(String fileName) {

        String lowerName =
                fileName.toLowerCase();

        if (lowerName.endsWith(".pdf")) {
            return "FIR / PDF Document";
        }

        if (lowerName.endsWith(".csv")) {
            return "Structured Dataset";
        }

        if (lowerName.endsWith(".json")) {
            return "JSON Intelligence Data";
        }

        if (lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")) {

            return "Image Evidence";
        }

        if (lowerName.endsWith(".mp4")
                || lowerName.endsWith(".avi")
                || lowerName.endsWith(".mov")) {

            return "Surveillance Video";
        }

        if (lowerName.endsWith(".txt")) {
            return "Intelligence Report";
        }

        return "Digital Evidence";
    }
}