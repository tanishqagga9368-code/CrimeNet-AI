package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class AnomalyDetectionService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;

    public AnomalyDetectionService(
            IntelligenceRecordRepository intelligenceRecordRepository
    ) {
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
    }

    public Map<String, Object> detectAnomalies() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> anomalies =
                new ArrayList<>();

        Map<String, Integer> entityActivity =
                countEntityActivity(records);

        Map<String, Set<String>> entityCases =
                collectEntityCases(records);

        for (IntelligenceRecord record : records) {

            String entity =
                    safe(record.getEntityName());

            if (entity.isBlank()) {
                continue;
            }

            int activityCount =
                    entityActivity.getOrDefault(
                            entity,
                            0
                    );

            int caseCount =
                    entityCases
                            .getOrDefault(
                                    entity,
                                    new LinkedHashSet<>()
                            )
                            .size();

            double financialScore =
                    detectFinancialAnomaly(record);

            double communicationScore =
                    detectCommunicationAnomaly(record);

            double activityScore =
                    detectActivityAnomaly(
                            activityCount,
                            caseCount
                    );

            double score =
                    Math.max(
                            financialScore,
                            Math.max(
                                    communicationScore,
                                    activityScore
                            )
                    );

            if (score < 0.60) {
                continue;
            }

            Map<String, Object> anomaly =
                    new LinkedHashMap<>();

            anomaly.put(
                    "recordId",
                    record.getId()
            );

            anomaly.put(
                    "caseId",
                    record.getCaseId()
            );

            anomaly.put(
                    "entityName",
                    entity
            );

            anomaly.put(
                    "entityType",
                    record.getEntityType()
            );

            anomaly.put(
                    "recordType",
                    record.getRecordType()
            );

            anomaly.put(
                    "location",
                    record.getLocation()
            );

            anomaly.put(
                    "anomalyScore",
                    round(score)
            );

            anomaly.put(
                    "riskLevel",
                    riskLevel(score)
            );

            anomaly.put(
                    "activityCount",
                    activityCount
            );

            anomaly.put(
                    "caseCount",
                    caseCount
            );

            anomaly.put(
                    "pattern",
                    detectPattern(
                            financialScore,
                            communicationScore,
                            activityScore
                    )
            );

            anomaly.put(
                    "explanation",
                    buildExplanation(
                            record,
                            financialScore,
                            communicationScore,
                            activityScore,
                            activityCount,
                            caseCount
                    )
            );

            anomaly.put(
                    "requiresReview",
                    true
            );

            anomalies.add(anomaly);
        }

        anomalies.sort(
                Comparator.comparingDouble(
                        (Map<String, Object> item) ->
                                getDouble(
                                        item.get("anomalyScore")
                                )
                ).reversed()
        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "success",
                true
        );

        response.put(
                "totalRecordsAnalyzed",
                records.size()
        );

        response.put(
                "totalAnomalies",
                anomalies.size()
        );

        response.put(
                "anomalies",
                anomalies
        );

        response.put(
                "interpretation",
                "Anomaly detection identifies unusual patterns "
                        + "that may deserve investigator review. "
                        + "An anomaly is not evidence of guilt."
        );

        return response;
    }

    public List<Map<String, Object>> detectCaseAnomalies(
            String caseId
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (caseId == null || caseId.isBlank()) {
            return result;
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(
                                caseId
                        );

        Map<String, Integer> entityActivity =
                countEntityActivity(records);

        for (IntelligenceRecord record : records) {

            String entity =
                    safe(record.getEntityName());

            if (entity.isBlank()) {
                continue;
            }

            int activityCount =
                    entityActivity.getOrDefault(
                            entity,
                            0
                    );

            double financialScore =
                    detectFinancialAnomaly(record);

            double communicationScore =
                    detectCommunicationAnomaly(record);

            double activityScore =
                    activityCount >= 3
                            ? 0.70
                            : 0.0;

            double score =
                    Math.max(
                            financialScore,
                            Math.max(
                                    communicationScore,
                                    activityScore
                            )
                    );

            if (score < 0.60) {
                continue;
            }

            Map<String, Object> anomaly =
                    new LinkedHashMap<>();

            anomaly.put(
                    "recordId",
                    record.getId()
            );

            anomaly.put(
                    "caseId",
                    record.getCaseId()
            );

            anomaly.put(
                    "entityName",
                    entity
            );

            anomaly.put(
                    "recordType",
                    record.getRecordType()
            );

            anomaly.put(
                    "location",
                    record.getLocation()
            );

            anomaly.put(
                    "anomalyScore",
                    round(score)
            );

            anomaly.put(
                    "riskLevel",
                    riskLevel(score)
            );

            anomaly.put(
                    "pattern",
                    detectPattern(
                            financialScore,
                            communicationScore,
                            activityScore
                    )
            );

            anomaly.put(
                    "explanation",
                    buildExplanation(
                            record,
                            financialScore,
                            communicationScore,
                            activityScore,
                            activityCount,
                            1
                    )
            );

            anomaly.put(
                    "requiresReview",
                    true
            );

            result.add(anomaly);
        }

        result.sort(
                Comparator.comparingDouble(
                        (Map<String, Object> item) ->
                                getDouble(
                                        item.get("anomalyScore")
                                )
                ).reversed()
        );

        return result;
    }

    private Map<String, Integer> countEntityActivity(
            List<IntelligenceRecord> records
    ) {

        Map<String, Integer> counts =
                new LinkedHashMap<>();

        for (IntelligenceRecord record : records) {

            String entity =
                    safe(record.getEntityName());

            if (entity.isBlank()) {
                continue;
            }

            counts.put(
                    entity,
                    counts.getOrDefault(
                            entity,
                            0
                    ) + 1
            );
        }

        return counts;
    }

    private Map<String, Set<String>> collectEntityCases(
            List<IntelligenceRecord> records
    ) {

        Map<String, Set<String>> result =
                new LinkedHashMap<>();

        for (IntelligenceRecord record : records) {

            String entity =
                    safe(record.getEntityName());

            String caseId =
                    safe(record.getCaseId());

            if (entity.isBlank()
                    || caseId.isBlank()) {
                continue;
            }

            result.computeIfAbsent(
                    entity,
                    key -> new LinkedHashSet<>()
            ).add(caseId);
        }

        return result;
    }

    private double detectFinancialAnomaly(
            IntelligenceRecord record
    ) {

        String text =
                safe(record.getDescription())
                        .toLowerCase();

        String type =
                safe(record.getRecordType())
                        .toLowerCase();

        if (!type.contains("financial")
                && !type.contains("transaction")
                && !text.contains("transfer")
                && !text.contains("payment")
                && !text.contains("transaction")) {

            return 0.0;
        }

        double amount =
                extractAmount(text);

        if (amount >= 1000000) {
            return 0.98;
        }

        if (amount >= 500000) {
            return 0.90;
        }

        if (amount >= 100000) {
            return 0.75;
        }

        return 0.65;
    }

    private double detectCommunicationAnomaly(
            IntelligenceRecord record
    ) {

        String text =
                (
                        safe(record.getDescription())
                                + " "
                                + safe(record.getRelationshipType())
                ).toLowerCase();

        String type =
                safe(record.getRecordType())
                        .toLowerCase();

        if (type.contains("cdr")
                || text.contains("call")
                || text.contains("calls")
                || text.contains("communication")) {

            if (text.contains("frequent")
                    || text.contains("repeated")
                    || text.contains("multiple")
                    || text.contains("high frequency")) {

                return 0.88;
            }

            return 0.65;
        }

        return 0.0;
    }

    private double detectActivityAnomaly(
            int activityCount,
            int caseCount
    ) {

        if (caseCount >= 4) {
            return 0.95;
        }

        if (caseCount >= 3) {
            return 0.88;
        }

        if (caseCount >= 2) {
            return 0.75;
        }

        if (activityCount >= 8) {
            return 0.90;
        }

        if (activityCount >= 5) {
            return 0.78;
        }

        if (activityCount >= 3) {
            return 0.65;
        }

        return 0.0;
    }

    private double extractAmount(
            String text
    ) {

        String cleaned =
                text.replaceAll(
                        "[,₹]",
                        ""
                );

        java.util.regex.Matcher matcher =
                java.util.regex.Pattern
                        .compile(
                                "(\\d+(?:\\.\\d+)?)\\s*(crore|lakh|million|thousand)?"
                        )
                        .matcher(cleaned);

        double highest = 0.0;

        while (matcher.find()) {

            try {

                double value =
                        Double.parseDouble(
                                matcher.group(1)
                        );

                String unit =
                        matcher.group(2);

                if (unit != null) {

                    unit =
                            unit.toLowerCase();

                    if (unit.equals("crore")) {
                        value *= 10000000;
                    } else if (unit.equals("lakh")) {
                        value *= 100000;
                    } else if (unit.equals("million")) {
                        value *= 1000000;
                    } else if (unit.equals("thousand")) {
                        value *= 1000;
                    }
                }

                highest =
                        Math.max(
                                highest,
                                value
                        );

            } catch (NumberFormatException ignored) {
                // Ignore malformed numeric values.
            }
        }

        return highest;
    }

    private String detectPattern(
            double financialScore,
            double communicationScore,
            double activityScore
    ) {

        if (financialScore >= 0.80
                && communicationScore >= 0.80) {

            return "FINANCIAL_COMMUNICATION_ANOMALY";
        }

        if (financialScore >= 0.80) {
            return "HIGH_VALUE_FINANCIAL_ACTIVITY";
        }

        if (communicationScore >= 0.80) {
            return "UNUSUAL_COMMUNICATION_PATTERN";
        }

        if (activityScore >= 0.80) {
            return "CROSS_CASE_ACTIVITY_ANOMALY";
        }

        return "UNUSUAL_ACTIVITY";
    }

    private String buildExplanation(
            IntelligenceRecord record,
            double financialScore,
            double communicationScore,
            double activityScore,
            int activityCount,
            int caseCount
    ) {

        List<String> reasons =
                new ArrayList<>();

        if (financialScore >= 0.80) {
            reasons.add(
                    "high-value financial activity indicator"
            );
        }

        if (communicationScore >= 0.80) {
            reasons.add(
                    "unusual communication indicator"
            );
        }

        if (caseCount >= 2) {
            reasons.add(
                    "entity appears across multiple cases"
            );
        }

        if (activityCount >= 5) {
            reasons.add(
                    "high activity frequency"
            );
        }

        if (reasons.isEmpty()) {
            reasons.add(
                    "unusual activity pattern detected"
            );
        }

        return String.join(
                "; ",
                reasons
        )
                + ". Record type: "
                + safe(record.getRecordType())
                + ". Investigator review recommended.";
    }

    private String riskLevel(
            double score
    ) {

        if (score >= 0.85) {
            return "HIGH";
        }

        if (score >= 0.70) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private double getDouble(
            Object value
    ) {

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(
                    value.toString()
            );
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }
}