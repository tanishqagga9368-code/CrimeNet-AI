package criminal_network_intelligence.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import criminal_network_intelligence.model.IntelligenceRecord;
import criminal_network_intelligence.repository.IntelligenceRecordRepository;

@Service
public class FinancialIntelligenceService {

    private final IntelligenceRecordRepository intelligenceRecordRepository;

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile(
                    "(?:₹|rs\\.?|inr|rs)\\s*([0-9,]+(?:\\.\\d+)?)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern NUMBER_AMOUNT_PATTERN =
            Pattern.compile(
                    "(?:amount|transfer|payment|transaction)"
                            + "\\s*(?:of|:|=)?\\s*"
                            + "(?:₹|rs\\.?|inr)?\\s*"
                            + "([0-9,]+(?:\\.\\d+)?)",
                    Pattern.CASE_INSENSITIVE
            );

    /*
     * Constructor injection.
     * This fixes the "intelligenceRecordRepository not initialized"
     * compilation error.
     */
    public FinancialIntelligenceService(
            IntelligenceRecordRepository intelligenceRecordRepository
    ) {
        this.intelligenceRecordRepository =
                intelligenceRecordRepository;
    }

    /**
     * Returns financial intelligence across all records.
     */
    public List<Map<String, Object>> getFinancialIntelligence() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (IntelligenceRecord record : records) {

            if (!isFinancialRecord(record)) {
                continue;
            }

            result.add(
                    buildTransactionItem(record)
            );
        }

        result.sort(
                Comparator.comparingDouble(
                        (Map<String, Object> item) ->
                                getDouble(item.get("amount"))
                ).reversed()
        );

        return result;
    }

    /**
     * Returns financial intelligence for a specific case.
     */
    public List<Map<String, Object>> getCaseFinancialIntelligence(
            String caseId
    ) {

        if (caseId == null || caseId.isBlank()) {
            return new ArrayList<>();
        }

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findByCaseIdOrderByCreatedAtDesc(caseId);

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (IntelligenceRecord record : records) {

            if (!isFinancialRecord(record)) {
                continue;
            }

            result.add(
                    buildTransactionItem(record)
            );
        }

        result.sort(
                Comparator.comparingDouble(
                        (Map<String, Object> item) ->
                                getDouble(item.get("amount"))
                ).reversed()
        );

        return result;
    }

    /**
     * Finds entities involved in repeated financial activity.
     */
    public List<Map<String, Object>> getRepeatedTransfers() {

        List<IntelligenceRecord> records =
                intelligenceRecordRepository
                        .findAllByOrderByCreatedAtDesc();

        Map<String, List<IntelligenceRecord>> grouped =
                new TreeMap<>(
                        String.CASE_INSENSITIVE_ORDER
                );

        for (IntelligenceRecord record : records) {

            if (!isFinancialRecord(record)) {
                continue;
            }

            String source =
                    safe(record.getEntityName()).trim();

            String target =
                    safe(record.getRelatedEntity()).trim();

            if (source.isBlank() && target.isBlank()) {
                continue;
            }

            String pair =
                    normalizePair(source, target);

            grouped
                    .computeIfAbsent(
                            pair,
                            key -> new ArrayList<>()
                    )
                    .add(record);
        }

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map.Entry<
                String,
                List<IntelligenceRecord>
                > entry : grouped.entrySet()) {

            List<IntelligenceRecord> transactions =
                    entry.getValue();

            if (transactions.size() < 2) {
                continue;
            }

            double totalAmount = 0;

            Set<String> cases =
                    new LinkedHashSet<>();

            Set<String> locations =
                    new LinkedHashSet<>();

            for (IntelligenceRecord record :
                    transactions) {

                totalAmount +=
                        extractAmount(record);

                if (record.getCaseId() != null
                        && !record.getCaseId().isBlank()) {

                    cases.add(
                            record.getCaseId()
                    );
                }

                if (record.getLocation() != null
                        && !record.getLocation().isBlank()) {

                    locations.add(
                            record.getLocation()
                    );
                }
            }

            int repetitionScore =
                    Math.min(
                            100,
                            transactions.size() * 20
                    );

            int financialScore =
                    calculateFinancialScore(
                            totalAmount,
                            transactions.size(),
                            cases.size()
                    );

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "entityPair",
                    entry.getKey()
            );

            item.put(
                    "transactionCount",
                    transactions.size()
            );

            item.put(
                    "totalAmount",
                    round(totalAmount)
            );

            item.put(
                    "relatedCases",
                    cases.size()
            );

            item.put(
                    "cases",
                    new ArrayList<>(cases)
            );

            item.put(
                    "locations",
                    new ArrayList<>(locations)
            );

            item.put(
                    "repetitionScore",
                    repetitionScore
            );

            item.put(
                    "financialRiskScore",
                    financialScore
            );

            item.put(
                    "riskLevel",
                    getRiskLevel(financialScore)
            );

            item.put(
                    "investigationLead",
                    financialScore >= 75
                            ? "HIGH"
                            : financialScore >= 45
                            ? "MEDIUM"
                            : "LOW"
            );

            item.put(
                    "explanation",
                    buildRepeatedTransferExplanation(
                            transactions.size(),
                            totalAmount,
                            cases.size()
                    )
            );

            item.put(
                    "interpretation",
                    "Repeated financial activity is an investigation lead and does not by itself establish illegal activity or criminal involvement."
            );

            result.add(item);
        }

        result.sort(
                Comparator.comparingInt(
                        (Map<String, Object> item) ->
                                getInteger(
                                        item.get(
                                                "financialRiskScore"
                                        )
                                )
                ).reversed()
        );

        return result;
    }

    /**
     * Returns high-value financial transactions.
     */
    public List<Map<String, Object>> getHighValueTransactions(
            double threshold
    ) {

        double safeThreshold =
                Math.max(0, threshold);

        List<Map<String, Object>> transactions =
                getFinancialIntelligence();

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map<String, Object> transaction :
                transactions) {

            double amount =
                    getDouble(
                            transaction.get("amount")
                    );

            if (amount < safeThreshold) {
                continue;
            }

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.putAll(transaction);

            item.put(
                    "financialIndicator",
                    "HIGH_VALUE_TRANSACTION"
            );

            item.put(
                    "investigationLead",
                    "HIGH"
            );

            item.put(
                    "explanation",
                    "The transaction amount exceeds the configured investigation threshold and should be reviewed with its supporting evidence."
            );

            result.add(item);
        }

        return result;
    }

    /**
     * Provides a financial intelligence summary.
     */
    public Map<String, Object> getFinancialSummary() {

        List<Map<String, Object>> transactions =
                getFinancialIntelligence();

        double totalAmount = 0;
        double highestAmount = 0;

        int highValueCount = 0;
        int suspiciousCount = 0;

        Set<String> entities =
                new LinkedHashSet<>();

        Set<String> cases =
                new LinkedHashSet<>();

        for (Map<String, Object> transaction :
                transactions) {

            double amount =
                    getDouble(
                            transaction.get("amount")
                    );

            totalAmount += amount;

            highestAmount =
                    Math.max(
                            highestAmount,
                            amount
                    );

            if (amount >= 100000) {
                highValueCount++;
            }

            String risk =
                    safe(
                            transaction.get("riskLevel")
                    );

            if ("HIGH".equalsIgnoreCase(risk)) {
                suspiciousCount++;
            }

            String entity =
                    safe(
                            transaction.get("entityName")
                    );

            String related =
                    safe(
                            transaction.get("relatedEntity")
                    );

            if (!entity.isBlank()) {
                entities.add(entity);
            }

            if (!related.isBlank()) {
                entities.add(related);
            }

            String caseId =
                    safe(
                            transaction.get("caseId")
                    );

            if (!caseId.isBlank()) {
                cases.add(caseId);
            }
        }

        Map<String, Object> summary =
                new LinkedHashMap<>();

        summary.put(
                "totalFinancialRecords",
                transactions.size()
        );

        summary.put(
                "totalTransactionAmount",
                round(totalAmount)
        );

        summary.put(
                "highestTransactionAmount",
                round(highestAmount)
        );

        summary.put(
                "highValueTransactions",
                highValueCount
        );

        summary.put(
                "highRiskTransactions",
                suspiciousCount
        );

        summary.put(
                "uniqueEntities",
                entities.size()
        );

        summary.put(
                "relatedCases",
                cases.size()
        );

        summary.put(
                "interpretation",
                "Financial metrics identify patterns requiring investigative review. Amounts and risk indicators must be validated against original evidence."
        );

        return summary;
    }

    private Map<String, Object> buildTransactionItem(
            IntelligenceRecord record
    ) {

        double amount =
                extractAmount(record);

        int score =
                calculateFinancialScore(
                        amount,
                        1,
                        record.getCaseId() == null
                                ? 0
                                : 1
                );

        String description =
                safe(record.getDescription());

        if (containsAny(
                description,
                "suspicious",
                "fraud",
                "extortion",
                "ransom",
                "money laundering"
        )) {

            score =
                    Math.min(
                            100,
                            score + 25
                    );
        }

        Map<String, Object> item =
                new LinkedHashMap<>();

        item.put(
                "recordId",
                record.getId()
        );

        item.put(
                "caseId",
                record.getCaseId()
        );

        item.put(
                "entityName",
                record.getEntityName()
        );

        item.put(
                "relatedEntity",
                record.getRelatedEntity()
        );

        item.put(
                "relationshipType",
                record.getRelationshipType()
        );

        item.put(
                "recordType",
                record.getRecordType()
        );

        item.put(
                "source",
                record.getSource()
        );

        item.put(
                "amount",
                round(amount)
        );

        item.put(
                "currency",
                "INR"
        );

        item.put(
                "location",
                record.getLocation()
        );

        item.put(
                "eventDate",
                record.getEventDate()
        );

        item.put(
                "description",
                record.getDescription()
        );

        item.put(
                "confidence",
                record.getConfidence()
        );

        item.put(
                "financialScore",
                score
        );

        item.put(
                "riskLevel",
                getRiskLevel(score)
        );

        item.put(
                "investigationLead",
                score >= 75
                        ? "HIGH"
                        : score >= 45
                        ? "MEDIUM"
                        : "LOW"
        );

        item.put(
                "explanation",
                buildTransactionExplanation(
                        amount,
                        record
                )
        );

        item.put(
                "interpretation",
                "This financial indicator is generated from available intelligence records and should be verified against source evidence."
        );

        return item;
    }

    private boolean isFinancialRecord(
            IntelligenceRecord record
    ) {

        if (record == null) {
            return false;
        }

        String text =
                (
                        safe(record.getRecordType())
                                + " "
                                + safe(record.getRelationshipType())
                                + " "
                                + safe(record.getDescription())
                ).toLowerCase();

        return containsAny(
                text,
                "financial",
                "transaction",
                "transfer",
                "payment",
                "bank",
                "money",
                "account",
                "fund",
                "amount",
                "upi",
                "wallet"
        );
    }

    private double extractAmount(
            IntelligenceRecord record
    ) {

        String text =
                (
                        safe(record.getDescription())
                                + " "
                                + safe(record.getRelationshipType())
                                + " "
                                + safe(record.getSource())
                );

        Matcher currencyMatcher =
                AMOUNT_PATTERN.matcher(text);

        if (currencyMatcher.find()) {
            return parseAmount(
                    currencyMatcher.group(1)
            );
        }

        Matcher amountMatcher =
                NUMBER_AMOUNT_PATTERN.matcher(text);

        if (amountMatcher.find()) {
            return parseAmount(
                    amountMatcher.group(1)
            );
        }

        return 0;
    }

    private double parseAmount(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return 0;
        }

        try {
            return Double.parseDouble(
                    value.replace(",", "")
            );
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private int calculateFinancialScore(
            double amount,
            int transactionCount,
            int caseCount
    ) {

        int score = 15;

        if (amount >= 1000000) {
            score += 55;
        } else if (amount >= 500000) {
            score += 45;
        } else if (amount >= 100000) {
            score += 35;
        } else if (amount >= 50000) {
            score += 25;
        } else if (amount >= 10000) {
            score += 15;
        }

        score +=
                Math.min(
                        20,
                        Math.max(
                                0,
                                transactionCount - 1
                        ) * 5
                );

        score +=
                Math.min(
                        15,
                        Math.max(
                                0,
                                caseCount - 1
                        ) * 5
                );

        return Math.min(
                100,
                score
        );
    }

    private String buildTransactionExplanation(
            double amount,
            IntelligenceRecord record
    ) {

        if (amount >= 1000000) {
            return "A very high-value financial event was identified. Review the source evidence, parties involved and transaction context.";
        }

        if (amount >= 100000) {
            return "A high-value financial event was identified and should be correlated with other case intelligence.";
        }

        if (record.getRelatedEntity() != null
                && !record.getRelatedEntity().isBlank()) {

            return "A financial relationship between identified entities was recorded and can be correlated with other graph evidence.";
        }

        return "A financial intelligence record was identified from the available case data.";
    }

    private String buildRepeatedTransferExplanation(
            int transactionCount,
            double totalAmount,
            int caseCount
    ) {

        if (caseCount >= 2) {
            return "Repeated financial activity between the entity pair appears across multiple cases and should be reviewed for cross-case correlation.";
        }

        if (totalAmount >= 1000000) {
            return "Repeated financial activity has a high aggregate value and warrants evidence-based investigation.";
        }

        return "Repeated financial activity was identified between the entity pair.";
    }

    private String normalizePair(
            String source,
            String target
    ) {

        if (source.isBlank()) {
            return target;
        }

        if (target.isBlank()) {
            return source;
        }

        if (source.compareToIgnoreCase(target) <= 0) {
            return source + " ↔ " + target;
        }

        return target + " ↔ " + source;
    }

    private String getRiskLevel(
            int score
    ) {

        if (score >= 75) {
            return "HIGH";
        }

        if (score >= 45) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private boolean containsAny(
            String text,
            String... values
    ) {

        String normalized =
                safe(text).toLowerCase();

        for (String value : values) {

            if (normalized.contains(
                    value.toLowerCase()
            )) {
                return true;
            }
        }

        return false;
    }

    private String safe(
            Object value
    ) {

        return value == null
                ? ""
                : String.valueOf(value);
    }

    private int getInteger(
            Object value
    ) {

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(
                    String.valueOf(value)
            );
        } catch (Exception exception) {
            return 0;
        }
    }

    private double getDouble(
            Object value
    ) {

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(
                    String.valueOf(value)
            );
        } catch (Exception exception) {
            return 0;
        }
    }

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}