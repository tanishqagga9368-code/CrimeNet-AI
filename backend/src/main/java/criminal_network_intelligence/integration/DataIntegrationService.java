package criminal_network_intelligence.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import criminal_network_intelligence.service.DataIngestionService;

@Service
public class DataIntegrationService {

    private final List<ExternalDataSource> dataSources;
    private final DataIngestionService dataIngestionService;

    public DataIntegrationService(
            List<ExternalDataSource> dataSources,
            @Lazy DataIngestionService dataIngestionService
    ) {
        this.dataSources = dataSources == null ? new ArrayList<>() : dataSources;
        this.dataIngestionService = dataIngestionService;
    }

    public List<Map<String, Object>> getAvailableSources() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (ExternalDataSource source : dataSources) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", source.getType().name());
            item.put("name", source.getName());
            item.put("available", source.isAvailable());
            result.add(item);
        }

        return result;
    }

    public ExternalDataResponse fetchFromSource(
            DataSourceType type,
            String identifier
    ) {
        if (type == null) {
            return ExternalDataResponse.failure(
                    "Integration Gateway",
                    identifier,
                    "Data source type is required."
            );
        }

        for (ExternalDataSource source : dataSources) {
            if (source.getType() == type) {
                return source.fetch(identifier);
            }
        }

        return ExternalDataResponse.failure(
                "Integration Gateway",
                identifier,
                "No integration provider registered for " + type.name()
        );
    }

    public List<ExternalDataResponse> fetchFromAllSources(String identifier) {
        List<ExternalDataResponse> results = new ArrayList<>();
        for (ExternalDataSource source : dataSources) {
            results.add(source.fetch(identifier));
        }
        return results;
    }

    public Map<String, Object> getIntegrationSummary() {
        int total = dataSources.size();
        int available = 0;

        for (ExternalDataSource source : dataSources) {
            if (source.isAvailable()) {
                available++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSources", total);
        result.put("availableSources", available);
        result.put("offlineSources", total - available);
        result.put("securityModel", "Authorized API/provider access only");
        result.put("biometricStorage", "NOT_STORED");
        result.put("evidencePolicy", "External source responses should be audited and integrity-hashed.");
        return result;
    }

    /**
     * Query structured/mock connectors for prototype demonstration (Requirement 12).
     */
    public Map<String, Object> queryDemoConnector(String sourceType, String query) {
        String raw = (sourceType != null ? sourceType : "FIR").toUpperCase();
        String type = "REPORT";
        if (raw.contains("FIR") || raw.contains("POLICE")) type = "FIR";
        else if (raw.contains("VEHICLE") || raw.contains("VAHAN")) type = "VEHICLE";
        else if (raw.contains("CDR") || raw.contains("TELECOM")) type = "CDR";
        else if (raw.contains("BANK") || raw.contains("FINANCIAL") || raw.contains("FIU")) type = "BANKING";
        else if (raw.contains("CRIM") || raw.contains("NCRB") || raw.contains("BUREAU")) type = "CRIMINAL";
        else if (raw.contains("REPORT") || raw.contains("INTEL")) type = "REPORT";

        String q = query != null ? query.trim() : "";
        Map<String, Object> resp = new LinkedHashMap<>();

        resp.put("sourceType", raw);
        resp.put("normalizedType", type);
        resp.put("query", q);
        resp.put("timestamp", LocalDateTime.now().toString());
        resp.put("notice", "DEMO / SYNTHETIC AUTHORIZED CONNECTOR — Simulated inter-agency registry response");

        switch (type) {
            case "FIR":
                resp.put("connectorName", "State Crime Records CCTNS Gateway (Demo)");
                resp.put("recordId", "FIR/DL/2026/" + (q.isEmpty() ? "412" : Math.abs(q.hashCode()) % 900 + 100));
                resp.put("subject", q.isEmpty() ? "Vikram Malhotra" : q);
                resp.put("sections", "IPC 120B (Criminal Conspiracy), IPC 420 (Cheating), PMLA Sec 3/4");
                resp.put("status", "CHARGE_SHEET_FILED");
                resp.put("jurisdiction", "Economic Offences Wing, Special Police Division");
                resp.put("extractedNarrative", "Case diary logs confirm subject " + (q.isEmpty() ? "Vikram Malhotra" : q)
                        + " operating shell corporate entity Trident Holdings Ltd and facilitating layered Hawala fund transfers of ₹45,00,000 via account ACC-987654321.");
                break;

            case "VEHICLE":
                String plate = q.isEmpty() ? "MH-01-AB-1234" : q.toUpperCase();
                resp.put("connectorName", "Vahan National Motor Vehicle & ANPR Registry (Demo)");
                resp.put("registrationNumber", plate);
                resp.put("vehicleClass", "Commercial Heavy Transport / Freight Carrier");
                resp.put("registeredOwner", "John Anderson");
                resp.put("chassisNumber", "MA3ER51S" + (Math.abs(plate.hashCode()) % 899999 + 100000));
                resp.put("lastAnprHit", "Nhava Sheva Port Toll Checkpoint 4 (Speed: 42 km/h)");
                resp.put("extractedNarrative", "Vehicle " + plate + " registered to John Anderson tracked transiting from Downtown Warehouse South Delhi to Nhava Sheva Port carrying unmanifested freight containers.");
                break;

            case "CDR":
                String phone = q.isEmpty() ? "+91-98201-11223" : q;
                resp.put("connectorName", "Telecom Security Intercept Feed (Demo)");
                resp.put("msisdn", phone);
                resp.put("subscriberName", "Vikram Malhotra");
                resp.put("imei", "86429104" + (Math.abs(phone.hashCode()) % 8999999 + 1000000));
                resp.put("activeCircle", "Mumbai - Maharashtra & Goa");
                resp.put("callFrequency", "48 calls recorded in 72h window");
                resp.put("primaryContact", "+91-98202-33445 (Amit Mehra)");
                resp.put("extractedNarrative", "Telecom intercept on " + phone + " indicates encrypted burst communications between Vikram Malhotra and Hawala conduit Amit Mehra coordinating cross-border Dubai remittances.");
                break;

            case "FINANCIAL":
            case "BANKING":
                String acc = q.isEmpty() ? "ACC-987654321" : q;
                resp.put("connectorName", "FIU-IND Anti-Money Laundering Gateway (Demo)");
                resp.put("accountNumber", acc);
                resp.put("entityName", "Trident Holdings Ltd");
                resp.put("beneficialOwner", "Vikram Malhotra");
                resp.put("flaggedAmount", "INR 45,00,000");
                resp.put("structuringPattern", "3 rapid RTGS tranches under ₹10L threshold to ACC-554433221 (Amit Mehra)");
                resp.put("riskIndicator", "CRITICAL_HAWALA_LAYER");
                resp.put("extractedNarrative", "FIU Suspicious Transaction Alert on account " + acc + " shows ₹45,00,000 rapidly dispersed from Trident Holdings Ltd to Amit Mehra before conversion to offshore bullion assets.");
                break;

            case "CRIMINAL":
            case "NCRB":
                resp.put("connectorName", "National Criminal Offender Database CCTNS (Demo)");
                resp.put("subjectName", q.isEmpty() ? "Robert Chen" : q);
                resp.put("dossierNumber", "NCRB/CR-IND/2026/8812");
                resp.put("historySummary", "Flagged in 3 cross-jurisdiction FIRs for financial fraud, bullion smuggling, and shell company directorship.");
                resp.put("associatedAliases", "R. Chen, Bob Chen, Global Trade Director");
                resp.put("extractedNarrative", "Subject " + (q.isEmpty() ? "Robert Chen" : q)
                        + " documented as primary bridge between domestic shell logistics and offshore Hawala liquidation networks.");
                break;

            case "REPORT":
            default:
                resp.put("connectorName", "Inter-Agency Intelligence Memoranda Network (Demo)");
                resp.put("memoReference", "INTEL-MEMO-2026-X41");
                resp.put("originatingAgency", "Special Operations Financial Task Force");
                resp.put("classification", "SECRET // LAW ENFORCEMENT DEMO USE ONLY");
                resp.put("extractedNarrative", "Multi-agency alert on Operation Trident: Coordinated syndicate between Vikram Malhotra (Kingpin), Robert Chen (Financial Bridge), and John Anderson (Logistics) operating across Mumbai, Delhi, and Dubai.");
                break;
        }

        return resp;
    }

    /**
     * Import retrieved connector record into case, run extraction, compute SHA-256 evidence, and sync graph.
     */
    public Map<String, Object> importDemoRecord(
            String caseId,
            String sourceType,
            String title,
            String narrative,
            String officer
    ) {
        String safeCaseId = (caseId != null && !caseId.isBlank()) ? caseId : "CR-2026-041";
        String safeSource = "Authorized Connector (" + (sourceType != null ? sourceType : "EXTERNAL") + ")";
        String safeTitle = (title != null && !title.isBlank()) ? title : "Imported " + sourceType + " Record";
        String safeOfficer = (officer != null && !officer.isBlank()) ? officer : "Investigating Officer";

        return dataIngestionService.ingestTextData(
                safeCaseId,
                sourceType,
                safeSource,
                safeTitle,
                narrative,
                safeOfficer
        );
    }
}