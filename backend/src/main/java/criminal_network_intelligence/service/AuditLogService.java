package criminal_network_intelligence.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.AuditLog;
import criminal_network_intelligence.repository.AuditLogRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository
    ) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog record(
            String username,
            String role,
            String action,
            String resourceType,
            String resourceId,
            String caseId,
            String description,
            String ipAddress
    ) {

        AuditLog auditLog =
                new AuditLog(
                        safe(username, "SYSTEM"),
                        safe(role, "SYSTEM"),
                        safe(action, "UNKNOWN"),
                        resourceType,
                        resourceId,
                        caseId,
                        description,
                        ipAddress
                );

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository
                .findAllByOrderByCreatedAtDesc();
    }

    public List<AuditLog> getUserLogs(
            String username
    ) {

        return auditLogRepository
                .findByUsernameOrderByCreatedAtDesc(
                        username
                );
    }

    public List<AuditLog> getCaseLogs(
            String caseId
    ) {

        return auditLogRepository
                .findByCaseIdOrderByCreatedAtDesc(
                        caseId
                );
    }

    public Map<String, Object> getSummary() {

        List<AuditLog> logs =
                auditLogRepository
                        .findAllByOrderByCreatedAtDesc();

        Map<String, Integer> actionCounts =
                new LinkedHashMap<>();

        for (AuditLog log : logs) {

            String action =
                    safe(
                            log.getAction(),
                            "UNKNOWN"
                    );

            actionCounts.put(
                    action,
                    actionCounts.getOrDefault(
                            action,
                            0
                    ) + 1
            );
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "totalAuditEvents",
                logs.size()
        );

        response.put(
                "actionCounts",
                actionCounts
        );

        response.put(
                "auditStatus",
                "ACTIVE"
        );

        response.put(
                "interpretation",
                "Audit logs provide accountability for "
                        + "investigation-system activity."
        );

        return response;
    }

    private String safe(
            String value,
            String fallback
    ) {

        if (value == null
                || value.isBlank()) {

            return fallback;
        }

        return value.trim();
    }
}