package criminal_network_intelligence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EntityScan(basePackages = "criminal_network_intelligence.model")
@EnableJpaRepositories(basePackages = "criminal_network_intelligence.repository")
@EnableNeo4jRepositories(basePackages = "criminal_network_intelligence.neo4j")
public class CriminalNetworkIntelligenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                CriminalNetworkIntelligenceApplication.class,
                args
        );
    }
}