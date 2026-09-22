package criminal_network_intelligence.integration;

public interface ExternalDataSource {

    DataSourceType getType();

    String getName();

    boolean isAvailable();

    ExternalDataResponse fetch(String identifier);
}