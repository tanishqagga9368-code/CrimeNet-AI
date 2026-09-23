package criminal_network_intelligence.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import criminal_network_intelligence.model.AuditLog;
import criminal_network_intelligence.model.BlockchainAuditBlock;
import criminal_network_intelligence.model.Case;
import criminal_network_intelligence.model.Evidence;
import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.AuditLogRepository;
import criminal_network_intelligence.repository.BlockchainAuditRepository;
import criminal_network_intelligence.repository.CaseRepository;
import criminal_network_intelligence.repository.EvidenceRepository;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private final EvidenceRepository evidenceRepository;
    private final CaseRepository caseRepository;
    private final BlockchainAuditRepository blockchainAuditRepository;
    private final IntelligenceRecordRepository intelligenceRecordRepository;
    private final AuditLogRepository auditLogRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(
            EvidenceRepository evidenceRepository,
            CaseRepository caseRepository,
            BlockchainAuditRepository blockchainAuditRepository,
            IntelligenceRecordRepository intelligenceRecordRepository,
            AuditLogRepository auditLogRepository,
            JdbcTemplate jdbcTemplate) {
        this.evidenceRepository = evidenceRepository;
        this.caseRepository = caseRepository;
        this.blockchainAuditRepository = blockchainAuditRepository;
        this.intelligenceRecordRepository = intelligenceRecordRepository;
        this.auditLogRepository = auditLogRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE IF EXISTS evidence DROP COLUMN IF EXISTS evidence_id CASCADE");
        } catch (Exception ignored) {
        }
        LocalDateTime now = LocalDateTime.now();

        if (caseRepository.count() == 0) {
            caseRepository.save(new Case(
                    "CR-2026-041",
                    "Operation Trident",
                    "High-priority multi-jurisdictional investigation into organized financial crime syndicate operating shell corporate entities, money laundering pipelines, and encrypted communications.",
                    "HIGH",
                    12,
                    "IN_PROGRESS",
                    "Investigating Officer Sharma",
                    now.minusDays(5).toString(),
                    now.minusHours(2).toString()
            ));

            caseRepository.save(new Case(
                    "CR-2026-038",
                    "Hawala Syndicate Nexus",
                    "Cross-border unauthorized foreign currency transfers and hawala distribution network linked to commercial hubs.",
                    "CRITICAL",
                    8,
                    "UNDER_SURVEILLANCE",
                    "Inspector Verma",
                    now.minusDays(12).toString(),
                    now.minusDays(1).toString()
            ));

            caseRepository.save(new Case(
                    "CR-2026-035",
                    "Vehicle Trafficking Network",
                    "Interstate luxury vehicle identification cloning and illicit transport network using forged registration certificates.",
                    "MEDIUM",
                    6,
                    "ACTIVE",
                    "Inspector Khan",
                    now.minusDays(20).toString(),
                    now.minusDays(3).toString()
            ));

            caseRepository.save(new Case(
                    "CR-2026-044",
                    "Offshore Bullion & Cyber Remittance",
                    "Cross-border bullion liquidity clearing and encrypted remittance channel linking shell corporations with offshore bullion dealers.",
                    "HIGH",
                    8,
                    "ACTIVE",
                    "Special Agent Rao",
                    now.minusDays(8).toString(),
                    now.minusHours(4).toString()
            ));
        }

        if (evidenceRepository.count() == 0) {
            Evidence e1 = new Evidence();
            e1.setCaseId("CR-2026-041");
            e1.setOriginalFileName("operation-trident-fir.pdf");
            e1.setEvidenceType("FIR Report");
            e1.setContentType("application/pdf");
            e1.setSizeBytes(245760);
            e1.setSha256Hash("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2");
            e1.setIntegrityStatus("VERIFIED");
            e1.setUploadedBy("Investigator Sharma");
            e1.setDescription("Primary FIR lodging charges of criminal conspiracy, money laundering, and shell enterprise operation.");
            e1.setUploadedAt(now.minusHours(8));
            e1.setVerifiedAt(now.minusHours(7));
            evidenceRepository.save(e1);

            Evidence e2 = new Evidence();
            e2.setCaseId("CR-2026-041");
            e2.setOriginalFileName("syndicate-cdr-analysis.csv");
            e2.setEvidenceType("Call Detail Record");
            e2.setContentType("text/csv");
            e2.setSizeBytes(128450);
            e2.setSha256Hash("b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3");
            e2.setIntegrityStatus("VERIFIED");
            e2.setUploadedBy("Investigator Sharma");
            e2.setDescription("Call detail records revealing 47 high-frequency communication events between John Anderson and Sarah Mitchell.");
            e2.setUploadedAt(now.minusHours(6));
            e2.setVerifiedAt(now.minusHours(5));
            evidenceRepository.save(e2);

            Evidence e3 = new Evidence();
            e3.setCaseId("CR-2026-041");
            e3.setOriginalFileName("bank-transfers-hawala.xlsx");
            e3.setEvidenceType("Financial Ledger");
            e3.setContentType("application/vnd.ms-excel");
            e3.setSizeBytes(512340);
            e3.setSha256Hash("c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4");
            e3.setIntegrityStatus("VERIFIED");
            e3.setUploadedBy("Investigator Sharma");
            e3.setDescription("Financial documentation verifying ₹45,00,000 rapid transfer from John Anderson to Robert Chen.");
            e3.setUploadedAt(now.minusDays(1));
            e3.setVerifiedAt(now.minusHours(20));
            evidenceRepository.save(e3);

            Evidence e4 = new Evidence();
            e4.setCaseId("CR-2026-041");
            e4.setOriginalFileName("surveillance-warehouse-log.pdf");
            e4.setEvidenceType("Surveillance Report");
            e4.setContentType("application/pdf");
            e4.setSizeBytes(78420);
            e4.setSha256Hash("d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5");
            e4.setIntegrityStatus("VERIFIED");
            e4.setUploadedBy("Officer Verma");
            e4.setDescription("Physical surveillance photos and entry log at Downtown Warehouse showing vehicle MH-01-AB-1234.");
            e4.setUploadedAt(now.minusDays(2));
            e4.setVerifiedAt(now.minusDays(2));
            evidenceRepository.save(e4);
        }

        if (blockchainAuditRepository.count() == 0) {
            String genesisHash = "0000000000000000000000000000000000000000000000000000000000000000";

            BlockchainAuditBlock b1 = new BlockchainAuditBlock();
            b1.setEvidenceId(1L);
            b1.setCaseId("CR-2026-041");
            b1.setEventType("FIR_EVIDENCE_REGISTERED");
            b1.setEvidenceHash("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2");
            b1.setPreviousHash(genesisHash);
            b1.setBlockHash("1111a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6");
            b1.setPerformedBy("Investigator Sharma");
            b1.setCreatedAt(now.minusHours(8));
            blockchainAuditRepository.save(b1);

            BlockchainAuditBlock b2 = new BlockchainAuditBlock();
            b2.setEvidenceId(2L);
            b2.setCaseId("CR-2026-041");
            b2.setEventType("CDR_LOGS_INGESTED");
            b2.setEvidenceHash("b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3");
            b2.setPreviousHash(b1.getBlockHash());
            b2.setBlockHash("2222b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1");
            b2.setPerformedBy("Investigator Sharma");
            b2.setCreatedAt(now.minusHours(6));
            blockchainAuditRepository.save(b2);

            BlockchainAuditBlock b3 = new BlockchainAuditBlock();
            b3.setEvidenceId(3L);
            b3.setCaseId("CR-2026-041");
            b3.setEventType("FINANCIAL_HAWALA_INGESTED");
            b3.setEvidenceHash("c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4");
            b3.setPreviousHash(b2.getBlockHash());
            b3.setBlockHash("3333c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2");
            b3.setPerformedBy("Investigator Sharma");
            b3.setCreatedAt(now.minusDays(1));
            blockchainAuditRepository.save(b3);

            BlockchainAuditBlock b4 = new BlockchainAuditBlock();
            b4.setEvidenceId(4L);
            b4.setCaseId("CR-2026-041");
            b4.setEventType("SURVEILLANCE_VERIFIED");
            b4.setEvidenceHash("d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5");
            b4.setPreviousHash(b3.getBlockHash());
            b4.setBlockHash("4444d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3");
            b4.setPerformedBy("Officer Verma");
            b4.setCreatedAt(now.minusDays(2));
            blockchainAuditRepository.save(b4);
        }

        if (intelligenceRecordRepository.count() == 0) {
            saveRecord("CR-2026-041", "FIR", "Police FIR Registration", "FIR registered under IPC 120B / PMLA for organized financial syndicate.", "John Anderson", "PERSON", "Global Trade Corp", "Delhi Crime Branch", 0.95, now.minusDays(5), "Downtown Warehouse, South Delhi");
            saveRecord("CR-2026-041", "SURVEILLANCE", "Warehouse Physical Sighting", "Suspect John Anderson arrived at Downtown Warehouse in vehicle MH-01-AB-1234.", "John Anderson", "PERSON", "Downtown Warehouse", "Field Surveillance Team Alpha", 0.88, now.minusDays(4), "Downtown Warehouse, South Delhi");
            saveRecord("CR-2026-041", "CDR", "Repeated Call Intercept", "47 calls between +91-98765-43210 and +91-98765-43211 across 7 days.", "John Anderson", "PERSON", "Sarah Mitchell", "Telecom Intercept Monitoring", 0.92, now.minusDays(3), "Delhi NCR Sector 18");
            saveRecord("CR-2026-041", "FINANCIAL", "Hawala Fund Movement", "Direct transfer of ₹45,00,000 to offshore conduit Robert Chen.", "John Anderson", "PERSON", "Robert Chen", "FIU-IND Suspicious Transaction Alert", 0.96, now.minusDays(2), "Industrial Zone B, Noida Sector 62");
            saveRecord("CR-2026-041", "INTELLIGENCE", "Corporate Shell Link", "Sarah Mitchell documented as executive director of Global Trade Corp shell.", "Sarah Mitchell", "PERSON", "Global Trade Corp", "Ministry of Corporate Affairs Registry", 0.90, now.minusDays(1), "Downtown Warehouse, South Delhi");
        }

        // Cross-Case Seed Records linking Robert Chen, Global Trade Corp, and John Anderson across multiple cases
        boolean hasChenIn38 = intelligenceRecordRepository.findByCaseId("CR-2026-038").stream().anyMatch(r -> "Robert Chen".equalsIgnoreCase(r.getEntityName()));
        if (!hasChenIn38) {
            saveRecord("CR-2026-038", "FINANCIAL", "Hawala Conduit Account", "Offshore intermediary account linked to Robert Chen flagged by Financial Intelligence Unit.", "Robert Chen", "PERSON", "Global Trade Corp", "FIU-IND Red Flag", 0.94, now.minusDays(7), "Industrial Zone B, Noida Sector 62");
            saveRecord("CR-2026-038", "INTELLIGENCE", "Shell Director Cross-Filing", "Global Trade Corp registered nominee directors overlapping with Trident syndicate.", "Global Trade Corp", "ORGANIZATION", "Robert Chen", "Enforcement Directorate Report", 0.91, now.minusDays(6), "Downtown Warehouse, South Delhi");
            saveRecord("CR-2026-038", "SURVEILLANCE", "International Airport Transit Sighting", "Robert Chen logged boarding flight with suspected Hawala courier.", "Robert Chen", "PERSON", "Global Trade Corp", "Immigration Border Control", 0.95, now.minusDays(3), "Indira Gandhi International Airport, Delhi");
        }

        boolean hasAndersonIn35 = intelligenceRecordRepository.findByCaseId("CR-2026-035").stream().anyMatch(r -> "John Anderson".equalsIgnoreCase(r.getEntityName()));
        if (!hasAndersonIn35) {
            saveRecord("CR-2026-035", "VEHICLE", "Freight Vehicle Intercept", "Vehicle MH-01-AB-1234 intercepted at toll carrying undeclared consignments linked to John Anderson.", "John Anderson", "PERSON", "MH-01-AB-1234", "Highway Toll ANPR & Highway Patrol", 0.89, now.minusDays(4), "Nhava Sheva Port, Navi Mumbai");
            saveRecord("CR-2026-035", "INTELLIGENCE", "Shared Warehouse Sighting", "John Anderson coordinated freight logistics at port container hub.", "John Anderson", "PERSON", "Downtown Warehouse", "Port Authority Intelligence", 0.92, now.minusDays(2), "Nhava Sheva Port, Navi Mumbai");
        }

        boolean hasVikramIn41 = intelligenceRecordRepository.findByCaseId("CR-2026-041").stream().anyMatch(r -> "Vikram Malhotra".equalsIgnoreCase(r.getEntityName()));
        if (!hasVikramIn41) {
            saveRecord("CR-2026-041", "FIR", "Operation Trident Master Charge Sheet", "Vikram Malhotra identified as kingpin directing Trident Holdings Ltd and overseas Hawala routing.", "Vikram Malhotra", "PERSON", "Trident Holdings Ltd", "Special Cyber Cell", 0.97, now.minusDays(6), "Nariman Point, Mumbai");
            saveRecord("CR-2026-041", "CDR", "Burner Phone Sighting Intercept", "48 calls recorded between burner +91-98201-11223 and logistics terminals.", "Vikram Malhotra", "PERSON", "+91-98201-11223", "Telecom Intercept Division", 0.94, now.minusDays(5), "Mumbai South");
        }

        boolean hasVikramIn38 = intelligenceRecordRepository.findByCaseId("CR-2026-038").stream().anyMatch(r -> "Vikram Malhotra".equalsIgnoreCase(r.getEntityName()));
        if (!hasVikramIn38) {
            saveRecord("CR-2026-038", "FINANCIAL", "Hawala Directives Wire", "Directives issued to Amit Mehra for ₹45,00,000 transfer from ACC-987654321.", "Vikram Malhotra", "PERSON", "Amit Mehra", "FIU-IND Wire Audit", 0.96, now.minusDays(4), "Mumbai South");
        }

        boolean hasAmitIn38 = intelligenceRecordRepository.findByCaseId("CR-2026-038").stream().anyMatch(r -> "Amit Mehra".equalsIgnoreCase(r.getEntityName()));
        if (!hasAmitIn38) {
            saveRecord("CR-2026-038", "FINANCIAL", "Hawala Pooling Account", "Amit Mehra routed funds through ACC-554433221 to Dubai Bullion Exchange.", "Amit Mehra", "PERSON", "Dubai Bullion Exchange", "FIU-IND Suspicious Alert", 0.93, now.minusDays(3), "Mumbai South");
        }

        boolean hasRanaIn35 = intelligenceRecordRepository.findByCaseId("CR-2026-035").stream().anyMatch(r -> "Devendra Rana".equalsIgnoreCase(r.getEntityName()));
        if (!hasRanaIn35) {
            saveRecord("CR-2026-035", "VEHICLE", "Port Container Checkpoint Hit", "Trailer MH-03-EF-9900 driven by Devendra Rana carrying untagged cargo.", "Devendra Rana", "PERSON", "MH-03-EF-9900", "Port Authority Checkpoint", 0.91, now.minusDays(3), "Nhava Sheva Port, Navi Mumbai");
        }

        // Backfill locations on existing records where location is blank
        List<IntelligenceRecord> unlocated = intelligenceRecordRepository.findAll();
        for (IntelligenceRecord rec : unlocated) {
            if (rec.getLocation() == null || rec.getLocation().isBlank()) {
                String en = rec.getEntityName() != null ? rec.getEntityName() : "";
                if (en.equalsIgnoreCase("Robert Chen")) {
                    rec.setLocation("Industrial Zone B, Noida Sector 62");
                } else if (en.equalsIgnoreCase("Sarah Mitchell")) {
                    rec.setLocation("Delhi NCR Sector 18");
                } else if (en.equalsIgnoreCase("John Anderson")) {
                    rec.setLocation("Downtown Warehouse, South Delhi");
                } else {
                    rec.setLocation("Downtown Warehouse, South Delhi");
                }
                intelligenceRecordRepository.save(rec);
            }
        }

        if (auditLogRepository.count() == 0) {
            auditLogRepository.save(new AuditLog("admin", "ADMIN", "SYSTEM_INIT", "SYSTEM", "SYS-001", null, "System initialized with baseline security and integrity checks.", "127.0.0.1"));
            auditLogRepository.save(new AuditLog("officer", "OFFICER", "CASE_OPEN", "CASE", "CR-2026-041", "CR-2026-041", "Opened case Operation Trident for investigation.", "127.0.0.1"));
            auditLogRepository.save(new AuditLog("officer", "OFFICER", "EVIDENCE_VERIFY", "EVIDENCE", "1", "CR-2026-041", "Cryptographic hash verification succeeded for operation-trident-fir.pdf", "127.0.0.1"));
            auditLogRepository.save(new AuditLog("analyst", "ANALYST", "NETWORK_ANALYZE", "GRAPH", "EN-008", "CR-2026-041", "Ran key person detection algorithm on suspect Robert Chen.", "127.0.0.1"));
        }
    }

    private void saveRecord(String caseId, String recordType, String title, String content, String entityName, String entityType, String relatedEntity, String source, double confidence, LocalDateTime eventDate, String location) {
        IntelligenceRecord r = new IntelligenceRecord();
        r.setCaseId(caseId);
        r.setRecordType(recordType);
        r.setTitle(title);
        r.setContent(content);
        r.setDescription(content);
        r.setEntityName(entityName);
        r.setEntityType(entityType);
        r.setRelatedEntity(relatedEntity);
        r.setSource(source);
        r.setConfidence(confidence);
        r.setEventDate(eventDate);
        r.setCreatedAt(eventDate);
        r.setProcessingStatus("PROCESSED");
        r.setLocation(location);
        intelligenceRecordRepository.save(r);
    }
}
