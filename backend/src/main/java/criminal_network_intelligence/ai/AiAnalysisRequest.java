package criminal_network_intelligence.ai;

public class AiAnalysisRequest {

    private String entityId;
    private String entityName;
    private String text;
    private String caseId;

    public AiAnalysisRequest() {
    }

    public AiAnalysisRequest(
            String entityId,
            String entityName,
            String text,
            String caseId
    ) {
        this.entityId = entityId;
        this.entityName = entityName;
        this.text = text;
        this.caseId = caseId;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}