package criminal_network_intelligence.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "intelligence_records")
public class IntelligenceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id")
    private String caseId;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "source_reference")
    private String sourceReference;

    @Column(name = "processing_status")
    private String processingStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "record_type")
    private String recordType;

    @Column(name = "source")
    private String source;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "related_entity")
    private String relatedEntity;

    @Column(name = "relationship_type")
    private String relationshipType;

    @Column(name = "location")
    private String location;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "confidence")
    private Double confidence;

    public IntelligenceRecord() {
    }

    public IntelligenceRecord(
            String caseId,
            String sourceType,
            String title,
            String content,
            String sourceReference,
            String processingStatus,
            LocalDateTime createdAt
    ) {
        this.caseId = caseId;
        this.sourceType = sourceType;
        this.title = title;
        this.content = content;
        this.sourceReference = sourceReference;
        this.processingStatus = processingStatus;
        this.createdAt = createdAt;
        this.recordType = sourceType;
        this.source = sourceReference;
        this.description = content;
        this.confidence = 0.0;
    }

    public IntelligenceRecord(
            String caseId,
            String sourceType,
            String title,
            String content,
            String sourceReference,
            String processingStatus,
            LocalDateTime createdAt,
            String recordType,
            String source,
            String entityName,
            String entityType,
            String relatedEntity,
            String relationshipType,
            String location,
            LocalDateTime eventDate,
            String description,
            Double confidence
    ) {
        this.caseId = caseId;
        this.sourceType = sourceType;
        this.title = title;
        this.content = content;
        this.sourceReference = sourceReference;
        this.processingStatus = processingStatus;
        this.createdAt = createdAt;
        this.recordType = recordType;
        this.source = source;
        this.entityName = entityName;
        this.entityType = entityType;
        this.relatedEntity = relatedEntity;
        this.relationshipType = relationshipType;
        this.location = location;
        this.eventDate = eventDate;
        this.description = description;
        this.confidence = confidence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRecordType() {
        if (recordType != null && !recordType.isBlank()) {
            return recordType;
        }
        return sourceType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getSource() {
        if (source != null && !source.isBlank()) {
            return source;
        }

        if (sourceReference != null && !sourceReference.isBlank()) {
            return sourceReference;
        }

        return sourceType;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(String relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getEventDate() {
        if (eventDate != null) {
            return eventDate;
        }
        return createdAt;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventDate(String eventDate) {
        if (eventDate == null || eventDate.isBlank()) {
            this.eventDate = null;
            return;
        }

        try {
            this.eventDate = LocalDateTime.parse(eventDate.trim());
        } catch (Exception exception) {
            this.eventDate = null;
        }
    }

    public String getDescription() {
        if (description != null && !description.isBlank()) {
            return description;
        }
        return content;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getConfidence() {
        if (confidence == null) {
            return 0.0;
        }
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public void setConfidence(String confidence) {
        if (confidence == null || confidence.isBlank()) {
            this.confidence = 0.0;
            return;
        }

        try {
            this.confidence = Double.parseDouble(confidence.trim());
        } catch (NumberFormatException exception) {
            this.confidence = 0.0;
        }
    }

    @Override
    public String toString() {
        return "IntelligenceRecord{" +
                "id=" + id +
                ", caseId='" + caseId + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", title='" + title + '\'' +
                ", recordType='" + recordType + '\'' +
                ", entityName='" + entityName + '\'' +
                ", entityType='" + entityType + '\'' +
                ", relatedEntity='" + relatedEntity + '\'' +
                ", relationshipType='" + relationshipType + '\'' +
                ", location='" + location + '\'' +
                ", eventDate=" + eventDate +
                ", confidence=" + confidence +
                '}';
    }
}