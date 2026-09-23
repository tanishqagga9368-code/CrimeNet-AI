package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.model.AuditLog;
import criminal_network_intelligence.service.AuditLogService;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(
            AuditLogService auditLogService
    ) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {

        return ResponseEntity.ok(
                auditLogService.getAllLogs()
        );
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<AuditLog>> getUserLogs(
            @PathVariable String username
    ) {

        return ResponseEntity.ok(
                auditLogService.getUserLogs(username)
        );
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<AuditLog>> getCaseLogs(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                auditLogService.getCaseLogs(caseId)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {

        return ResponseEntity.ok(
                auditLogService.getSummary()
        );
    }
}