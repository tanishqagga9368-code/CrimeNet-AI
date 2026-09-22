package criminal_network_intelligence.neo4j;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface CriminalNodeRepository
        extends Neo4jRepository<CriminalNode, String> {

    Optional<CriminalNode> findByName(String name);
}