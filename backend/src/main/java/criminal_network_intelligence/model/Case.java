package criminal_network_intelligence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "investigation_cases")
public class Case {

    @Id
    @Column(name = "case_id", nullable = false, unique = true)
    private String caseId;

    @Column(name = "title")
    private String title;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "entities")
    private int entities;

    @Column(name = "status")
    private String status;

    @Column(name = "investigating_officer")
    private String investigatingOfficer;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @Column(name = "location")
    private String location;

    public Case() {
    }

    public Case(
            String caseId,
            String title,
            String riskLevel,
            int entities,
            String status) {

        this.caseId = caseId;
        this.title = title;
        this.riskLevel = riskLevel;
        this.entities = entities;
        this.status = status;
    }

    public Case(
            String caseId,
            String title,
            String description,
            String riskLevel,
            int entities,
            String status,
            String investigatingOfficer,
            String createdAt,
            String updatedAt) {

        this.caseId = caseId;
        this.title = title;
        this.description = description;
        this.riskLevel = riskLevel;
        this.entities = entities;
        this.status = status;
        this.investigatingOfficer = investigatingOfficer;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getEntities() {
        return entities;
    }

    public void setEntities(int entities) {
        this.entities = entities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInvestigatingOfficer() {
        return investigatingOfficer;
    }

    public void setInvestigatingOfficer(String investigatingOfficer) {
        this.investigatingOfficer = investigatingOfficer;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}