package criminal_network_intelligence.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.BlockchainAuditBlock;
import criminal_network_intelligence.model.Evidence;
import criminal_network_intelligence.repository.BlockchainAuditRepository;
import criminal_network_intelligence.repository.EvidenceRepository;

@Service
public class BlockchainAuditService {

    private static final String GENESIS_HASH =
            "0000000000000000000000000000000000000000000000000000000000000000";

    private final BlockchainAuditRepository blockchainAuditRepository;
    private final EvidenceRepository evidenceRepository;

    public BlockchainAuditService(
            BlockchainAuditRepository blockchainAuditRepository,
            EvidenceRepository evidenceRepository
    ) {
        this.blockchainAuditRepository =
                blockchainAuditRepository;

        this.evidenceRepository =
                evidenceRepository;
    }

    /**
     * Create an audit block for an evidence record.
     */
    public Map<String, Object> recordEvidenceEvent(
            Long evidenceId,
            String eventType,
            String performedBy
    ) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        if (evidenceId == null) {
            result.put("success", false);
            result.put(
                    "message",
                    "Evidence ID is required."
            );
            return result;
        }

        Optional<Evidence> evidenceOptional =
                evidenceRepository.findById(evidenceId);

        if (evidenceOptional.isEmpty()) {
            result.put("success", false);
            result.put(
                    "message",
                    "Evidence not found."
            );
            return result;
        }

        Evidence evidence =
                evidenceOptional.get();

        String evidenceHash =
                evidence.getSha256Hash();

        if (
                evidenceHash == null
                        || evidenceHash.isBlank()
        ) {
            result.put("success", false);
            result.put(
                    "message",
                    "Evidence does not contain a SHA-256 hash."
            );
            return result;
        }

        String previousHash =
                getLatestBlockHash();

        String safeEventType =
                eventType == null
                        || eventType.isBlank()
                        ? "EVIDENCE_EVENT"
                        : eventType;

        String safePerformedBy =
                performedBy == null
                        || performedBy.isBlank()
                        ? "SYSTEM"
                        : performedBy;

        LocalDateTime createdAt =
                LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        String blockData =
                buildBlockData(
                        evidence.getId(),
                        evidence.getCaseId(),
                        safeEventType,
                        evidenceHash,
                        previousHash,
                        safePerformedBy,
                        createdAt
                );

        String blockHash =
                sha256(blockData);

        BlockchainAuditBlock block =
                new BlockchainAuditBlock();

        block.setEvidenceId(
                evidence.getId()
        );

        block.setCaseId(
                evidence.getCaseId()
        );

        block.setEventType(
                safeEventType
        );

        block.setEvidenceHash(
                evidenceHash
        );

        block.setPreviousHash(
                previousHash
        );

        block.setBlockHash(
                blockHash
        );

        block.setPerformedBy(
                safePerformedBy
        );

        block.setCreatedAt(
                createdAt
        );

        BlockchainAuditBlock saved =
                blockchainAuditRepository.save(block);

        result.put("success", true);
        result.put(
                "blockId",
                saved.getId()
        );

        result.put(
                "evidenceId",
                saved.getEvidenceId()
        );

        result.put(
                "caseId",
                saved.getCaseId()
        );

        result.put(
                "eventType",
                saved.getEventType()
        );

        result.put(
                "evidenceHash",
                saved.getEvidenceHash()
        );

        result.put(
                "previousHash",
                saved.getPreviousHash()
        );

        result.put(
                "blockHash",
                saved.getBlockHash()
        );

        result.put(
                "performedBy",
                saved.getPerformedBy()
        );

        result.put(
                "createdAt",
                saved.getCreatedAt()
        );

        result.put(
                "ledgerStatus",
                "RECORDED"
        );

        return result;
    }

    /**
     * Get complete private ledger.
     */
    public List<Map<String, Object>> getLedger() {

        List<BlockchainAuditBlock> blocks =
                blockchainAuditRepository
                        .findAllByOrderByIdAsc();

        return convertBlocks(blocks);
    }

    /**
     * Get ledger entries for a case.
     */
    public List<Map<String, Object>> getCaseLedger(
            String caseId
    ) {

        if (
                caseId == null
                        || caseId.isBlank()
        ) {
            return new ArrayList<>();
        }

        List<BlockchainAuditBlock> blocks =
                blockchainAuditRepository
                        .findByCaseIdOrderByIdAsc(
                                caseId
                        );

        return convertBlocks(blocks);
    }

    /**
     * Get ledger entries for an evidence item.
     */
    public List<Map<String, Object>> getEvidenceLedger(
            Long evidenceId
    ) {

        if (evidenceId == null) {
            return new ArrayList<>();
        }

        List<BlockchainAuditBlock> blocks =
                blockchainAuditRepository
                        .findByEvidenceIdOrderByIdAsc(
                                evidenceId
                        );

        return convertBlocks(blocks);
    }

    /**
     * Verify complete hash chain.
     */
    public Map<String, Object> verifyLedger() {

        Map<String, Object> result =
                new LinkedHashMap<>();

        List<BlockchainAuditBlock> blocks =
                blockchainAuditRepository
                        .findAllByOrderByIdAsc();

        if (blocks.isEmpty()) {
            result.put("success", true);
            result.put(
                    "ledgerStatus",
                    "EMPTY"
            );
            result.put(
                    "totalBlocks",
                    0
            );
            result.put(
                    "verifiedBlocks",
                    0
            );
            result.put(
                    "tamperedBlocks",
                    0
            );

            return result;
        }

        int verifiedBlocks = 0;
        int tamperedBlocks = 0;

        String expectedPreviousHash =
                GENESIS_HASH;

        List<Long> tamperedBlockIds =
                new ArrayList<>();

        for (BlockchainAuditBlock block : blocks) {

            boolean previousHashValid =
                    expectedPreviousHash.equals(
                            block.getPreviousHash()
                    );

            String recalculatedData =
                    buildBlockData(
                            block.getEvidenceId(),
                            block.getCaseId(),
                            block.getEventType(),
                            block.getEvidenceHash(),
                            block.getPreviousHash(),
                            block.getPerformedBy(),
                            block.getCreatedAt()
                    );

            String recalculatedHash =
                    sha256(recalculatedData);

            boolean blockHashValid =
                    recalculatedHash.equals(
                            block.getBlockHash()
                    );

            if (
                    previousHashValid
                            && blockHashValid
            ) {
                verifiedBlocks++;
            } else {
                tamperedBlocks++;

                if (block.getId() != null) {
                    tamperedBlockIds.add(
                            block.getId()
                    );
                }
            }

            expectedPreviousHash =
                    block.getBlockHash();
        }

        result.put(
                "success",
                tamperedBlocks == 0
        );

        result.put(
                "ledgerStatus",
                tamperedBlocks == 0
                        ? "VERIFIED"
                        : "INTEGRITY_VIOLATION"
        );

        result.put(
                "totalBlocks",
                blocks.size()
        );

        result.put(
                "verifiedBlocks",
                verifiedBlocks
        );

        result.put(
                "tamperedBlocks",
                tamperedBlocks
        );

        result.put(
                "tamperedBlockIds",
                tamperedBlockIds
        );

        result.put(
                "verificationTime",
                LocalDateTime.now()
        );

        return result;
    }

    /**
     * Get the latest block hash.
     */
    private String getLatestBlockHash() {

        Optional<BlockchainAuditBlock> latest =
                blockchainAuditRepository
                        .findTopByOrderByIdDesc();

        return latest
                .map(
                        BlockchainAuditBlock::getBlockHash
                )
                .orElse(GENESIS_HASH);
    }

    /**
     * Build deterministic block content.
     */
    private String buildBlockData(
            Long evidenceId,
            String caseId,
            String eventType,
            String evidenceHash,
            String previousHash,
            String performedBy,
            LocalDateTime createdAt
    ) {
        String ts = createdAt != null ? createdAt.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString() : "";
        return String.join(
                "|",
                safe(evidenceId),
                safe(caseId),
                safe(eventType),
                safe(evidenceHash),
                safe(previousHash),
                safe(performedBy),
                ts
        );
    }

    /**
     * SHA-256 utility.
     */
    private String sha256(String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hash) {

                String hexValue =
                        Integer.toHexString(
                                0xff & b
                        );

                if (hexValue.length() == 1) {
                    hex.append('0');
                }

                hex.append(hexValue);
            }

            return hex.toString();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to calculate SHA-256 hash.",
                    exception
            );
        }
    }

    /**
     * Safe string conversion.
     */
    private String safe(Object value) {

        return value == null
                ? ""
                : value.toString();
    }

    /**
     * Convert database blocks into API response objects.
     */
    private List<Map<String, Object>> convertBlocks(
            List<BlockchainAuditBlock> blocks
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (blocks == null) {
            return result;
        }

        for (BlockchainAuditBlock block : blocks) {

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "blockId",
                    block.getId()
            );

            item.put(
                    "evidenceId",
                    block.getEvidenceId()
            );

            item.put(
                    "caseId",
                    block.getCaseId()
            );

            item.put(
                    "eventType",
                    block.getEventType()
            );

            item.put(
                    "evidenceHash",
                    block.getEvidenceHash()
            );

            item.put(
                    "previousHash",
                    block.getPreviousHash()
            );

            item.put(
                    "blockHash",
                    block.getBlockHash()
            );

            item.put(
                    "performedBy",
                    block.getPerformedBy()
            );

            item.put(
                    "createdAt",
                    block.getCreatedAt()
            );

            result.add(item);
        }

        return result;
    }
}