package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.model.Alert;
import criminal_network_intelligence.service.AlertService;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<Alert>> getAlerts(@RequestParam(required = false) String caseId) {
        return ResponseEntity.ok(alertService.getAlertsByCase(caseId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("unreadCount", alertService.getUnreadCount()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Alert updated = alertService.markAsRead(id);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
        return ResponseEntity.ok(alertService.createAlert(alert));
    }
}
