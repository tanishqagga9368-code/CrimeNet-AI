package criminal_network_intelligence.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.service.DataIngestionService;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
@RequestMapping("/api/ingestion")
public class DataIngestionController {

    private final DataIngestionService dataIngestionService;

    public DataIngestionController(DataIngestionService dataIngestionService) {
        this.dataIngestionService = dataIngestionService;
    }

    @PostMapping("/text")
    public ResponseEntity<?> ingestText(@RequestBody Map<String, Object> request) {
        try {
            String caseId = String.valueOf(request.getOrDefault("caseId", "CR-2026-041"));
            String recordType = String.valueOf(request.getOrDefault("recordType", "FIR_POLICE_REPORT"));
            String source = String.valueOf(request.getOrDefault("source", "Field Intelligence Unit"));
            String title = String.valueOf(request.getOrDefault("title", "Intelligence Log"));
            String text = String.valueOf(request.getOrDefault("text", ""));
            String officer = String.valueOf(request.getOrDefault("officer", "Investigating Officer"));

            Map<String, Object> result = dataIngestionService.ingestTextData(
                    caseId,
                    recordType,
                    source,
                    title,
                    text,
                    officer
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Ingestion failed: " + e.getMessage()));
        }
    }

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> ingestFile(
            @RequestParam String caseId,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String source,
            @RequestParam("file") MultipartFile file) {

        try {
            List<IntelligenceRecord> records = dataIngestionService.ingestFile(
                    caseId,
                    recordType,
                    source,
                    file
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Data ingested successfully and recorded in blockchain audit ledger",
                            "caseId", caseId,
                            "recordsCreated", records.size(),
                            "records", records
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Unable to process input file"));
        }
    }

    @GetMapping("/records")
    public ResponseEntity<List<IntelligenceRecord>> getAllRecords() {
        return ResponseEntity.ok(dataIngestionService.getAllRecords());
    }

    @GetMapping("/records/{caseId}")
    public ResponseEntity<List<IntelligenceRecord>> getCaseRecords(@PathVariable String caseId) {
        return ResponseEntity.ok(dataIngestionService.getCaseRecords(caseId));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "service", "Data Ingestion Pipeline",
                "status", "READY",
                "supportedFormats", List.of("TEXT", "CSV", "JSON", "PDF", "TXT"),
                "pipeline", "Upload → SHA-256 Hash → Ledger Block → AI Extraction → Knowledge Graph Sync"
        ));
    }
}
