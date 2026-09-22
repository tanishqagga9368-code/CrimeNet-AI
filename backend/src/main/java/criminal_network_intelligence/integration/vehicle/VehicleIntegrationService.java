package criminal_network_intelligence.integration.vehicle;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import criminal_network_intelligence.integration.DataSourceType;
import criminal_network_intelligence.integration.ExternalDataResponse;
import criminal_network_intelligence.integration.ExternalDataSource;

@Service
public class VehicleIntegrationService implements ExternalDataSource {

    @Value("${integration.vehicle.enabled:false}")
    private boolean enabled;

    @Value("${integration.vehicle.provider-url:}")
    private String providerUrl;

    @Override
    public DataSourceType getType() {
        return DataSourceType.VEHICLE;
    }

    @Override
    public String getName() {
        return "Authorized Vehicle Intelligence Provider";
    }

    @Override
    public boolean isAvailable() {
        return enabled && providerUrl != null && !providerUrl.isBlank();
    }

    @Override
    public ExternalDataResponse fetch(String identifier) {

        if (identifier == null || identifier.isBlank()) {
            return ExternalDataResponse.failure(
                    getName(),
                    "",
                    "Vehicle registration number is required."
            );
        }

        if (!isAvailable()) {
            return ExternalDataResponse.failure(
                    getName(),
                    identifier,
                    "Authorized vehicle provider is not configured."
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("integrationStatus", "READY_FOR_AUTHORIZED_PROVIDER");
        data.put("providerUrl", providerUrl);
        data.put("registrationNumber", identifier);
        data.put("recordType", "VEHICLE");
        data.put(
                "message",
                "Connect authorized vehicle/RTO provider here."
        );

        return ExternalDataResponse.success(
                getName(),
                identifier,
                data
        );
    }
}