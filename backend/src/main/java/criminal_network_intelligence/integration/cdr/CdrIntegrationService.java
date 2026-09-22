package criminal_network_intelligence.integration.cdr;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import criminal_network_intelligence.integration.DataSourceType;
import criminal_network_intelligence.integration.ExternalDataResponse;
import criminal_network_intelligence.integration.ExternalDataSource;

@Service
public class CdrIntegrationService implements ExternalDataSource {

    @Value("${integration.cdr.enabled:false}")
    private boolean enabled;

    @Value("${integration.cdr.provider-url:}")
    private String providerUrl;

    @Override
    public DataSourceType getType() {
        return DataSourceType.CDR;
    }

    @Override
    public String getName() {
        return "Authorized CDR Provider";
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
                    "Phone number is required."
            );
        }

        if (!isAvailable()) {
            return ExternalDataResponse.failure(
                    getName(),
                    mask(identifier),
                    "Authorized CDR provider is not configured."
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("integrationStatus", "READY_FOR_AUTHORIZED_PROVIDER");
        data.put("providerUrl", providerUrl);
        data.put("phone", mask(identifier));
        data.put("recordType", "CDR");
        data.put(
                "message",
                "Connect authorized telecom/CDR provider API here."
        );

        return ExternalDataResponse.success(
                getName(),
                mask(identifier),
                data
        );
    }

    private String mask(String value) {

        if (value == null || value.length() < 4) {
            return "****";
        }

        return "******" +
                value.substring(value.length() - 4);
    }
}