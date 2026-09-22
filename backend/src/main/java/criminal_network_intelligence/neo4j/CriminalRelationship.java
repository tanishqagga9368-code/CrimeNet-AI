package criminal_network_intelligence.neo4j;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class CriminalRelationship {

    @RelationshipId
    private Long id;

    private String type;

    private String description;

    @TargetNode
    private CriminalNode target;

    public CriminalRelationship() {
    }

    public CriminalRelationship(
            String type,
            String description,
            CriminalNode target
    ) {
        this.type = type;
        this.description = description;
        this.target = target;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CriminalNode getTarget() {
        return target;
    }

    public void setTarget(CriminalNode target) {
        this.target = target;
    }
}