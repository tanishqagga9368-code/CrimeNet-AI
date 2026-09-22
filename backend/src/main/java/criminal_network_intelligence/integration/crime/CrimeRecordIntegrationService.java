package criminal_network_intelligence.integration.crime;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import criminal_network_intelligence.integration.DataSourceType;
import criminal_network_intelligence.integration.ExternalDataResponse;
import criminal_network_intelligence.integration.ExternalDataSource;

@Service
public class CrimeRecordIntegrationService implements ExternalDataSource {

    @Value("${integration.crime.enabled:false}")
    private boolean enabled;

    @Value("${integration.crime.provider-url:}")
    private String providerUrl;

    @Override
    public DataSourceType getType() {
        return DataSourceType.NCRB;
    }

    @Override
    public String getName() {
        return "Authorized Crime/NCRB Provider";
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
                    "Crime/person identifier is required."
            );
        }

        if (!isAvailable()) {
            return ExternalDataResponse.failure(
                    getName(),
                    identifier,
                    "Authorized crime/NCRB provider is not configured."
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("integrationStatus", "READY_FOR_AUTHORIZED_PROVIDER");
        data.put("providerUrl", providerUrl);
        data.put("identifier", identifier);
        data.put("recordType", "NCRB");
        data.put(
                "message",
                "Connect authorized crime-record provider here."
        );

        return ExternalDataResponse.success(
                getName(),
                identifier,
                data
        );
    }
}