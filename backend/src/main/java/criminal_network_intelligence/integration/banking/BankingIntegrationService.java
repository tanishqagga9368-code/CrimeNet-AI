package criminal_network_intelligence.integration.banking;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import criminal_network_intelligence.integration.DataSourceType;
import criminal_network_intelligence.integration.ExternalDataResponse;
import criminal_network_intelligence.integration.ExternalDataSource;

@Service
public class BankingIntegrationService implements ExternalDataSource {

    @Value("${integration.banking.enabled:false}")
    private boolean enabled;

    @Value("${integration.banking.provider-url:}")
    private String providerUrl;

    @Override
    public DataSourceType getType() {
        return DataSourceType.BANKING;
    }

    @Override
    public String getName() {
        return "Authorized Banking Intelligence Provider";
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
                    "Bank account identifier is required."
            );
        }

        if (!isAvailable()) {
            return ExternalDataResponse.failure(
                    getName(),
                    mask(identifier),
                    "Authorized banking provider is not configured."
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("integrationStatus", "READY_FOR_AUTHORIZED_PROVIDER");
        data.put("providerUrl", providerUrl);
        data.put("account", mask(identifier));
        data.put("recordType", "BANKING");
        data.put(
                "message",
                "Connect authorized banking intelligence provider here."
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

        return "********" +
                value.substring(value.length() - 4);
    }
}