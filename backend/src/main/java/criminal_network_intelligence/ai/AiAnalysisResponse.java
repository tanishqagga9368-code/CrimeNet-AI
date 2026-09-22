package criminal_network_intelligence.ai;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAnalysisResponse {

    private String entityId;
    private String entityName;
    private String risk;
    private int suspicionScore;
    private int confidence;
    private int connections;

    private List<Map<String, Object>> entities;
    private List<Map<String, Object>> relationships;
    private List<Map<String, Object>> suspiciousPatterns;
    private List<String> explanation;

    public AiAnalysisResponse() {
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public int getSuspicionScore() {
        return suspicionScore;
    }

    public void setSuspicionScore(int suspicionScore) {
        this.suspicionScore = suspicionScore;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @JsonSetter("confidence")
    public void setConfidenceFlexible(Object conf) {
        if (conf instanceof Number) {
            double d = ((Number) conf).doubleValue();
            this.confidence = (int) Math.round(d <= 1.0 ? d * 100 : d);
        }
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public List<Map<String, Object>> getEntities() {
        return entities;
    }

    public void setEntities(List<Map<String, Object>> entities) {
        this.entities = entities;
    }

    public List<Map<String, Object>> getRelationships() {
        return relationships;
    }

    public void setRelationships(
            List<Map<String, Object>> relationships
    ) {
        this.relationships = relationships;
    }

    public List<Map<String, Object>> getSuspiciousPatterns() {
        return suspiciousPatterns;
    }

    public void setSuspiciousPatterns(
            List<Map<String, Object>> suspiciousPatterns
    ) {
        this.suspiciousPatterns = suspiciousPatterns;
    }

    public List<String> getExplanation() {
        return explanation;
    }

    public void setExplanation(List<String> explanation) {
        this.explanation = explanation;
    }

    @JsonSetter("explanation")
    public void setExplanationFlexible(Object exp) {
        if (exp instanceof List) {
            this.explanation = ((List<?>) exp).stream().map(Object::toString).toList();
        } else if (exp != null) {
            this.explanation = List.of(exp.toString());
        }
    }
}