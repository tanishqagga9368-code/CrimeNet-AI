package criminal_network_intelligence.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.Case;
import criminal_network_intelligence.model.Evidence;
import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.CaseRepository;
import criminal_network_intelligence.repository.EvidenceRepository;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class InvestigationReportService {

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final IntelligenceRecordRepository intelligenceRecordRepository;
    private final TimelineService timelineService;
    private final CrossCaseConnectionService crossCaseConnectionService;
    private final GraphAnalyticsService graphAnalyticsService;

    public InvestigationReportService(
            CaseRepository caseRepository,
            EvidenceRepository evidenceRepository,
            IntelligenceRecordRepository intelligenceRecordRepository,
            TimelineService timelineService,
            CrossCaseConnectionService crossCaseConnectionService,
            GraphAnalyticsService graphAnalyticsService
    ) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
        this.timelineService = timelineService;
        this.crossCaseConnectionService =
                crossCaseConnectionService;
        this.graphAnalyticsService =
                graphAnalyticsService;
    }

    /**
     * Generate a consolidated investigation report.
     */
    public Map<String, Object> generateReport(
            String caseId
    ) {

        Map<String, Object> report =
                new LinkedHashMap<>();

        if (caseId == null || caseId.isBlank()) {
            report.put("success", false);
            report.put(
                    "message",
                    "Case ID is required."
            );
            return report;
        }

        Optional<Case> caseOptional =
                caseRepository.findById(caseId);

        if (caseOptional.isEmpty()) {
            report.put("success", false);
            report.put(
                    "message",
                    "Case not found."
            );
            return report;
        }

        Case investigationCase =
                caseOptional.get();

        List<Evidence> evidence =
                evidenceRepository
                        .findByCaseIdOrderByUploadedAtDesc(
                                caseId
                        );

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(
                                caseId
                        );

        List<Map<String, Object>> timeline =
                timelineService.getCaseTimeline(
                        caseId
                );

        List<Map<String, Object>> crossCaseLinks =
                crossCaseConnectionService
                        .findConnectionsForCase(
                                caseId
                        );

        Map<String, Object> graphStatistics =
                graphAnalyticsService.getStatistics();

        Map<String, Object> reportCase =
                new LinkedHashMap<>();

        reportCase.put(
                "caseId",
                investigationCase.getCaseId()
        );

        reportCase.put(
                "title",
                investigationCase.getTitle()
        );

        reportCase.put(
                "description",
                investigationCase.getDescription()
        );

        reportCase.put(
                "status",
                investigationCase.getStatus()
        );

        reportCase.put(
                "riskLevel",
                investigationCase.getRiskLevel()
        );

        reportCase.put(
                "investigatingOfficer",
                investigationCase.getInvestigatingOfficer()
        );

        reportCase.put(
                "entities",
                investigationCase.getEntities()
        );

        reportCase.put(
                "createdAt",
                investigationCase.getCreatedAt()
        );

        reportCase.put(
                "updatedAt",
                investigationCase.getUpdatedAt()
        );

        report.put(
                "success",
                true
        );

        report.put(
                "reportGeneratedAt",
                LocalDateTime.now()
        );

        report.put(
                "case",
                reportCase
        );

        report.put(
                "caseId",
                investigationCase.getCaseId()
        );

        report.put(
                "caseTitle",
                investigationCase.getTitle()
        );

        report.put(
                "investigatingOfficer",
                investigationCase.getInvestigatingOfficer()
        );

        report.put(
                "entities",
                investigationCase.getEntities()
        );

        report.put(
                "investigatorNotes",
                investigationCase.getDescription()
        );

        report.put(
                "evidenceCount",
                evidence == null
                        ? 0
                        : evidence.size()
        );

        report.put(
                "intelligenceRecordCount",
                records == null
                        ? 0
                        : records.size()
        );

        report.put(
                "timelineEventCount",
                timeline.size()
        );

        report.put(
                "crossCaseConnectionCount",
                crossCaseLinks.size()
        );

        report.put(
                "evidence",
                buildEvidenceSection(evidence)
        );

        report.put(
                "timeline",
                timeline
        );

        report.put(
                "crossCaseConnections",
                crossCaseLinks
        );

        report.put(
                "intelligence",
                buildIntelligenceSection(records)
        );

        report.put(
                "graphStatistics",
                graphStatistics
        );

        report.put(
                "investigationSummary",
                buildInvestigationSummary(
                        investigationCase,
                        evidence,
                        records,
                        timeline,
                        crossCaseLinks
                )
        );

        report.put(
                "disclaimer",
                "This report contains automated investigation "
                        + "indicators and supporting information. "
                        + "It does not determine guilt or criminal responsibility."
        );

        return report;
    }

    /**
     * Get lightweight report summary.
     */
    public Map<String, Object> getReportSummary(
            String caseId
    ) {

        Map<String, Object> fullReport =
                generateReport(caseId);

        if (
                !Boolean.TRUE.equals(
                        fullReport.get("success")
                )
        ) {
            return fullReport;
        }

        Map<String, Object> summary =
                new LinkedHashMap<>();

        summary.put(
                "success",
                true
        );

        summary.put(
                "caseId",
                caseId
        );

        summary.put(
                "case",
                fullReport.get("case")
        );

        summary.put(
                "evidenceCount",
                fullReport.get("evidenceCount")
        );

        summary.put(
                "intelligenceRecordCount",
                fullReport.get(
                        "intelligenceRecordCount"
                )
        );

        summary.put(
                "timelineEventCount",
                fullReport.get(
                        "timelineEventCount"
                )
        );

        summary.put(
                "crossCaseConnectionCount",
                fullReport.get(
                        "crossCaseConnectionCount"
                )
        );

        summary.put(
                "investigationSummary",
                fullReport.get(
                        "investigationSummary"
                )
        );

        return summary;
    }

    /**
     * Build evidence section.
     */
    private List<Map<String, Object>> buildEvidenceSection(
            List<Evidence> evidence
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (evidence == null) {
            return result;
        }

        for (Evidence item : evidence) {

            if (item == null) {
                continue;
            }

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put(
                    "evidenceId",
                    item.getId()
            );

            data.put(
                    "fileName",
                    item.getOriginalFileName()
            );

            data.put(
                    "title",
                    item.getOriginalFileName()
            );

            data.put(
                    "evidenceType",
                    item.getEvidenceType()
            );

            data.put(
                    "sha256Hash",
                    item.getSha256Hash()
            );

            data.put(
                    "integrityStatus",
                    item.getIntegrityStatus()
            );

            data.put(
                    "uploadedBy",
                    item.getUploadedBy()
            );

            data.put(
                    "uploadedAt",
                    item.getUploadedAt()
            );

            result.add(data);
        }

        return result;
    }

    /**
     * Build intelligence section.
     */
    private List<Map<String, Object>> buildIntelligenceSection(
            List<IntelligenceRecord> records
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (records == null) {
            return result;
        }

        for (IntelligenceRecord record : records) {

            if (record == null) {
                continue;
            }

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put(
                    "recordId",
                    record.getId()
            );

            data.put(
                    "recordType",
                    record.getRecordType()
            );

            data.put(
                    "source",
                    record.getSource()
            );

            data.put(
                    "entityName",
                    record.getEntityName()
            );

            data.put(
                    "entityType",
                    record.getEntityType()
            );

            data.put(
                    "relatedEntity",
                    record.getRelatedEntity()
            );

            data.put(
                    "relationshipType",
                    record.getRelationshipType()
            );

            data.put(
                    "location",
                    record.getLocation()
            );

            data.put(
                    "eventDate",
                    record.getEventDate()
            );

            data.put(
                    "confidence",
                    record.getConfidence()
            );

            result.add(data);
        }

        return result;
    }

    /**
     * Build high-level investigation summary.
     */
    private Map<String, Object> buildInvestigationSummary(
            Case investigationCase,
            List<Evidence> evidence,
            List<IntelligenceRecord> records,
            List<Map<String, Object>> timeline,
            List<Map<String, Object>> crossCaseLinks
    ) {

        Map<String, Object> summary =
                new LinkedHashMap<>();

        int evidenceCount =
                evidence == null
                        ? 0
                        : evidence.size();

        int recordCount =
                records == null
                        ? 0
                        : records.size();

        int timelineCount =
                timeline == null
                        ? 0
                        : timeline.size();

        int crossCaseCount =
                crossCaseLinks == null
                        ? 0
                        : crossCaseLinks.size();

        summary.put(
                "riskLevel",
                investigationCase.getRiskLevel()
        );

        summary.put(
                "status",
                investigationCase.getStatus()
        );

        summary.put(
                "evidenceAvailable",
                evidenceCount > 0
        );

        summary.put(
                "intelligenceAvailable",
                recordCount > 0
        );

        summary.put(
                "timelineAvailable",
                timelineCount > 0
        );

        summary.put(
                "crossCaseLinksAvailable",
                crossCaseCount > 0
        );

        summary.put(
                "investigationLead",
                buildLeadText(
                        investigationCase,
                        evidenceCount,
                        recordCount,
                        crossCaseCount
                )
        );

        return summary;
    }

    /**
     * Generate investigator-friendly lead text.
     */
    private String buildLeadText(
            Case investigationCase,
            int evidenceCount,
            int recordCount,
            int crossCaseCount
    ) {

        StringBuilder text =
                new StringBuilder();

        text.append(
                "Case "
        );

        text.append(
                investigationCase.getCaseId()
        );

        text.append(
                " currently has "
        );

        text.append(evidenceCount);

        text.append(
                " evidence record(s) and "
        );

        text.append(recordCount);

        text.append(
                " intelligence record(s)."
        );

        if (crossCaseCount > 0) {

            text.append(
                    " Potential links with "
            );

            text.append(
                    crossCaseCount
            );

            text.append(
                    " cross-case intelligence record(s) "
                            + "should be reviewed."
            );
        } else {

            text.append(
                    " No cross-case connection was identified "
                            + "by the current dataset."
            );
        }

        return text.toString();
    }
}