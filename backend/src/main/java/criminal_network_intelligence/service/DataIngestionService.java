package criminal_network_intelligence.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import criminal_network_intelligence.ai.AiAnalysisRequest;
import criminal_network_intelligence.ai.AiAnalysisResponse;
import criminal_network_intelligence.model.Evidence;
import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.neo4j.Neo4jService;
import criminal_network_intelligence.repository.EvidenceRepository;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class DataIngestionService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;
    private final EvidenceRepository evidenceRepository;
    private final BlockchainAuditService blockchainAuditService;
    private final Neo4jService neo4jService;
    private final AuditLogService auditLogService;
    private final AiAnalysisService aiAnalysisService;

    public DataIngestionService(
            IntelligenceRecordRepository intelligenceRecordRepository,
            EvidenceRepository evidenceRepository,
            BlockchainAuditService blockchainAuditService,
            Neo4jService neo4jService,
            AuditLogService auditLogService,
            AiAnalysisService aiAnalysisService) {

        this.intelligenceRecordRepository = intelligenceRecordRepository;
        this.evidenceRepository = evidenceRepository;
        this.blockchainAuditService = blockchainAuditService;
        this.neo4jService = neo4jService;
        this.auditLogService = auditLogService;
        this.aiAnalysisService = aiAnalysisService;
    }

    public Map<String, Object> ingestTextData(
            String caseId,
            String recordType,
            String source,
            String title,
            String text,
            String officer) {

        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("Case ID is required");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Intelligence text content cannot be empty");
        }

        String safeType = recordType != null && !recordType.isBlank() ? recordType : "INTELLIGENCE_REPORT";
        String safeSource = source != null && !source.isBlank() ? source : "Field Intelligence";
        String safeTitle = title != null && !title.isBlank() ? title : "Intelligence Feed - " + caseId;
        String safeOfficer = officer != null && !officer.isBlank() ? officer : "Investigating Officer";

        String sha256Hash = computeSha256(text);

        Evidence evidence = new Evidence();
        evidence.setCaseId(caseId);
        evidence.setOriginalFileName(safeTitle.replaceAll("[^a-zA-Z0-9.-]", "_") + ".txt");
        evidence.setEvidenceType(safeType);
        evidence.setContentType("text/plain");
        evidence.setSizeBytes(text.getBytes(StandardCharsets.UTF_8).length);
        evidence.setSha256Hash(sha256Hash);
        evidence.setIntegrityStatus("VERIFIED");
        evidence.setUploadedBy(safeOfficer);
        evidence.setDescription(safeTitle + " - " + safeType);
        evidence.setUploadedAt(LocalDateTime.now());
        evidence.setVerifiedAt(LocalDateTime.now());
        evidence = evidenceRepository.save(evidence);

        Map<String, Object> blockResult = blockchainAuditService.recordEvidenceEvent(
                evidence.getId(),
                "INTELLIGENCE_INGESTED",
                safeOfficer
        );

        AiAnalysisRequest aiReq = new AiAnalysisRequest();
        aiReq.setText(text);
        aiReq.setCaseId(caseId);
        aiReq.setEntityName(safeTitle);
        AiAnalysisResponse aiRes = aiAnalysisService.analyze(aiReq);

        IntelligenceRecord record = new IntelligenceRecord();
        record.setCaseId(caseId);
        record.setRecordType(safeType);
        record.setSource(safeSource);
        record.setTitle(safeTitle);
        record.setContent(text);
        record.setProcessingStatus("PROCESSED");
        record.setConfidence(aiRes.getConfidence() > 0 ? (double) aiRes.getConfidence() / 100.0 : 0.88);
        record.setCreatedAt(LocalDateTime.now());
        record.setDescription("SHA-256: " + sha256Hash + " | Blockchain Block: " + blockResult.getOrDefault("blockHash", "N/A"));
        intelligenceRecordRepository.save(record);

        List<Map<String, Object>> entities = aiRes.getEntities();
        if (entities != null) {
            for (Map<String, Object> ent : entities) {
                String entId = String.valueOf(ent.getOrDefault("id", ""));
                String name = String.valueOf(ent.getOrDefault("name", ""));
                String type = String.valueOf(ent.getOrDefault("type", "ENTITY"));
                if (!entId.isBlank() && !name.isBlank()) {
                    neo4jService.createNode(
                            entId,
                            name,
                            type,
                            "MEDIUM",
                            "PHONE".equalsIgnoreCase(type) ? name : "",
                            "LOCATION".equalsIgnoreCase(type) ? name : ""
                    );
                }
            }
        }

        List<Map<String, Object>> relationships = aiRes.getRelationships();
        if (relationships != null) {
            for (Map<String, Object> rel : relationships) {
                String src = String.valueOf(rel.getOrDefault("source", ""));
                String tgt = String.valueOf(rel.getOrDefault("target", ""));
                String relType = String.valueOf(rel.getOrDefault("type", "ASSOCIATED_WITH"));
                if (!src.isBlank() && !tgt.isBlank()) {
                    neo4jService.createRelationship(src, tgt, relType, "Extracted from " + safeTitle);
                }
            }
        }

        auditLogService.record(
                safeOfficer,
                "OFFICER",
                "INGEST_DATA",
                "EVIDENCE",
                String.valueOf(evidence.getId()),
                caseId,
                "Ingested intelligence record '" + safeTitle + "' with SHA-256 hash " + sha256Hash.substring(0, 12) + "...",
                "127.0.0.1"
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Intelligence processed, verified on ledger, and mapped to Knowledge Graph.");
        response.put("caseId", caseId);
        response.put("evidenceId", evidence.getId());
        response.put("sha256Hash", sha256Hash);
        response.put("blockHash", blockResult.get("blockHash"));
        response.put("previousHash", blockResult.get("previousHash"));
        response.put("entitiesCount", entities != null ? entities.size() : 0);
        response.put("entities", entities != null ? entities : List.of());
        response.put("relationships", relationships != null ? relationships : List.of());
        response.put("suspiciousPatterns", aiRes.getSuspiciousPatterns() != null ? aiRes.getSuspiciousPatterns() : List.of());
        response.put("suspicionScore", aiRes.getSuspicionScore());
        response.put("risk", aiRes.getRisk());
        response.put("explanation", aiRes.getExplanation());

        return response;
    }

    public List<IntelligenceRecord> ingestFile(
            String caseId,
            String recordType,
            String source,
            MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Input file cannot be empty");
        }
        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("Case ID is required");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            fileName = "uploaded-data.txt";
        }

        String lowerName = fileName.toLowerCase();
        if (!lowerName.endsWith(".csv") && !lowerName.endsWith(".json") && !lowerName.endsWith(".pdf") && !lowerName.endsWith(".txt")) {
            throw new IllegalArgumentException("Unsupported file type: " + fileName + ". Supported formats are CSV, JSON, PDF, and TXT.");
        }

        String detectedType = recordType;
        if (detectedType == null || detectedType.isBlank()) {
            detectedType = detectRecordType(fileName);
        }

        String detectedSource = source;
        if (detectedSource == null || detectedSource.isBlank()) {
            detectedSource = "Uploaded Dataset";
        }

        byte[] bytes = file.getBytes();
        String sha256 = computeSha256(bytes);
        Evidence evidence = new Evidence();
        evidence.setCaseId(caseId);
        evidence.setOriginalFileName(fileName);
        evidence.setEvidenceType(detectedType);
        evidence.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        evidence.setSizeBytes(file.getSize());
        evidence.setSha256Hash(sha256);
        evidence.setIntegrityStatus("VERIFIED");
        evidence.setUploadedBy("Investigating Officer");
        evidence.setDescription("Uploaded dataset: " + fileName);
        evidence.setUploadedAt(LocalDateTime.now());
        evidence.setVerifiedAt(LocalDateTime.now());
        evidence = evidenceRepository.save(evidence);

        blockchainAuditService.recordEvidenceEvent(evidence.getId(), "FILE_INGESTED", "Investigating Officer");

        if (isCsv(fileName)) {
            return ingestCsv(caseId, detectedType, detectedSource, file);
        }

        return ingestGenericText(caseId, detectedType, detectedSource, file);
    }

    private List<IntelligenceRecord> ingestCsv(
            String caseId,
            String recordType,
            String source,
            MultipartFile file) throws IOException {

        List<IntelligenceRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return records;
            }

            String[] headers = splitCsvLine(headerLine);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] values = splitCsvLine(line);
                IntelligenceRecord record = new IntelligenceRecord();
                record.setCaseId(caseId);
                record.setRecordType(recordType);
                record.setSource(source);
                record.setProcessingStatus("PROCESSED");
                record.setCreatedAt(LocalDateTime.now());

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    String header = headers[i].trim().toLowerCase();
                    String value = values[i].trim();
                    switch (header) {
                        case "entityname", "entity_name", "name", "caller_msisdn", "caller", "source_account", "source_holder" -> record.setEntityName(value);
                        case "entitytype", "entity_type", "type", "call_type", "channel" -> record.setEntityType(value);
                        case "relatedentity", "related_entity", "receiver_msisdn", "receiver", "called_party", "destination_account", "destination_holder" -> record.setRelatedEntity(value);
                        case "relationshiptype", "relationship_type", "relationship" -> record.setRelationshipType(value);
                        case "location", "cell_tower_id", "tower" -> record.setLocation(value);
                        case "description", "suspect_tag", "notes", "flag" -> record.setDescription(value);
                        case "confidence" -> record.setConfidence(value);
                        case "timestamp", "call_timestamp", "date", "event_date" -> record.setEventDate(value);
                    }
                }

                if (record.getEventDate() == null) {
                    record.setEventDate(LocalDateTime.now());
                }

                if (record.getEntityName() != null && !record.getEntityName().isBlank()) {
                    neo4jService.createNode(
                            "ENT-" + Math.abs(record.getEntityName().hashCode()),
                            record.getEntityName(),
                            record.getEntityType() != null ? record.getEntityType() : "PERSON",
                            "MEDIUM",
                            "",
                            record.getLocation() != null ? record.getLocation() : ""
                    );

                    if (record.getRelatedEntity() != null && !record.getRelatedEntity().isBlank()) {
                        neo4jService.createNode(
                                "ENT-" + Math.abs(record.getRelatedEntity().hashCode()),
                                record.getRelatedEntity(),
                                "PERSON",
                                "MEDIUM",
                                "",
                                record.getLocation() != null ? record.getLocation() : ""
                        );
                        neo4jService.createRelationship(
                                record.getEntityName(),
                                record.getRelatedEntity(),
                                record.getRelationshipType() != null && !record.getRelationshipType().isBlank() ? record.getRelationshipType() : "COMMUNICATED_WITH",
                                record.getDescription() != null ? record.getDescription() : "Ingested CDR/Transaction Link"
                        );
                    }
                }

                records.add(intelligenceRecordRepository.save(record));
            }
        }

        return records;
    }

    private List<IntelligenceRecord> ingestGenericText(
            String caseId,
            String recordType,
            String source,
            MultipartFile file) throws IOException {

        List<IntelligenceRecord> records = new ArrayList<>();
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        IntelligenceRecord record = new IntelligenceRecord();
        record.setCaseId(caseId);
        record.setRecordType(recordType);
        record.setSource(source);
        record.setTitle(file.getOriginalFilename());
        record.setContent(content);
        record.setEntityType("DOCUMENT");
        record.setDescription(content);
        record.setConfidence(0.85);
        record.setProcessingStatus("PROCESSED");
        record.setCreatedAt(LocalDateTime.now());

        records.add(intelligenceRecordRepository.save(record));
        return records;
    }

    public List<IntelligenceRecord> getAllRecords() {
        return intelligenceRecordRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<IntelligenceRecord> getCaseRecords(String caseId) {
        return intelligenceRecordRepository.findByCaseIdOrderByCreatedAtDesc(caseId);
    }

    private String detectRecordType(String fileName) {
        if (fileName == null) return "GENERAL INTELLIGENCE";
        String lower = fileName.toLowerCase();
        if (lower.contains("fir") || lower.contains("police") || lower.endsWith(".pdf")) return "FIR / POLICE REPORT";
        if (lower.contains("cdr") || lower.contains("telecom") || lower.endsWith(".csv")) return "CDR";
        if (lower.contains("finan") || lower.contains("hawala") || lower.contains("bank") || lower.contains("ledger") || lower.endsWith(".json")) return "FINANCIAL TRANSACTIONS";
        if (lower.contains("crim") || lower.contains("ncrb") || lower.contains("cctns") || lower.contains("history")) return "CRIMINAL HISTORY";
        if (lower.contains("intel") || lower.contains("memo") || lower.contains("brief")) return "INTELLIGENCE REPORT";
        if (lower.contains("surv") || lower.contains("cctv") || lower.contains("stakeout") || lower.endsWith(".txt")) return "SURVEILLANCE REPORT";
        if (lower.contains("soc") || lower.contains("chat") || lower.contains("osint") || lower.contains("telegram") || lower.contains("signal")) return "SOCIAL MEDIA DATA";
        return "GENERAL INTELLIGENCE";
    }

    private boolean isCsv(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".csv");
    }

    private String[] splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (c == ',' && !insideQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private String computeSha256(String text) {
        return computeSha256(text.getBytes(StandardCharsets.UTF_8));
    }

    private String computeSha256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 computation failed", e);
        }
    }
}
