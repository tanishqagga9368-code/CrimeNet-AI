package criminal_network_intelligence.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import criminal_network_intelligence.model.Alert;
import criminal_network_intelligence.repository.AlertRepository;
import jakarta.annotation.PostConstruct;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @PostConstruct
    public void init() {
        if (alertRepository.count() == 0) {
            seedAlerts();
        }
    }

    @Transactional(readOnly = true)
    public List<Alert> getAllAlerts() {
        return alertRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public List<Alert> getAlertsByCase(String caseId) {
        if (caseId == null || caseId.isBlank() || "ALL".equalsIgnoreCase(caseId)) {
            return getAllAlerts();
        }
        return alertRepository.findByCaseIdOrderByTimestampDesc(caseId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return alertRepository.countByIsReadFalse();
    }

    public Alert markAsRead(Long id) {
        return alertRepository.findById(id).map(a -> {
            a.setRead(true);
            return alertRepository.save(a);
        }).orElse(null);
    }

    public Alert createAlert(Alert alert) {
        if (alert.getTimestamp() == null) {
            alert.setTimestamp(LocalDateTime.now());
        }
        return alertRepository.save(alert);
    }

    private void seedAlerts() {
        LocalDateTime now = LocalDateTime.now();

        Alert a1 = new Alert(
                "WATCHLIST_MATCH",
                "CRITICAL",
                "Potential Watchlist Match: Vikram Malhotra",
                "Automated surveillance hit from Nhava Sheva Toll ANPR Camera 04. High confidence facial recognition (94.2%) and vehicle plate MH-01-AB-1234 match.",
                "CR-2026-041",
                "EN-011",
                "Vikram Malhotra",
                "Surveillance Video / ANPR Stream (Demo)"
        );
        a1.setTimestamp(now.minusMinutes(18));
        alertRepository.save(a1);

        Alert a2 = new Alert(
                "CROSS_CASE_MATCH",
                "HIGH",
                "Cross-Case Shared Identifier Detected",
                "Burner phone +91-98201-11223 intercepted in Operation Trident (CR-2026-041) is actively linked to Hawala Nexus (CR-2026-038).",
                "CR-2026-038",
                "EN-012",
                "+91-98201-11223",
                "Telecom Intercept Feed"
        );
        a2.setTimestamp(now.minusHours(2));
        alertRepository.save(a2);

        Alert a3 = new Alert(
                "SUSPICIOUS_TRANSACTION",
                "CRITICAL",
                "Rapid Structuring & Hawala Transfer Alert",
                "Structured wire transfer of ₹45,00,000 outbound from Trident Holdings Ltd (ACC-987654321) fragmented into Amit Mehra and offshore bullion accounts.",
                "CR-2026-041",
                "EN-013",
                "ACC-987654321",
                "FIU-IND Suspicious Transaction Feed"
        );
        a3.setTimestamp(now.minusHours(5));
        alertRepository.save(a3);

        Alert a4 = new Alert(
                "SHARED_IDENTIFIER",
                "HIGH",
                "Shared Logistics Vehicle Identified Across 2 FIRs",
                "Commercial freight vehicle MH-01-AB-1234 registered under John Anderson (CR-2026-041) spotted at container freight station in Vehicle Trafficking Case (CR-2026-035).",
                "CR-2026-035",
                "EN-003",
                "MH-01-AB-1234",
                "Vahan ANPR Highway Registry"
        );
        a4.setTimestamp(now.minusHours(14));
        alertRepository.save(a4);

        Alert a5 = new Alert(
                "NEW_CONNECTION",
                "MEDIUM",
                "New Network Conduit Node Discovered",
                "Graph algorithm detected Robert Chen establishing beneficial ownership relay through shell corporation Global Trade Corp to Dubai Bullion Exchange.",
                "CR-2026-044",
                "EN-008",
                "Robert Chen",
                "Graph Topology Anomaly Engine"
        );
        a5.setTimestamp(now.minusDays(1));
        alertRepository.save(a5);
    }
}
