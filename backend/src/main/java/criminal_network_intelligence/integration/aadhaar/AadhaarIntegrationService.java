package criminal_network_intelligence.integration.aadhaar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import criminal_network_intelligence.integration.DataSourceType;
import criminal_network_intelligence.integration.ExternalDataResponse;
import criminal_network_intelligence.integration.ExternalDataSource;

@Service
public class AadhaarIntegrationService implements ExternalDataSource {

    @Value("${integration.aadhaar.enabled:false}")
    private boolean enabled;

    @Value("${integration.aadhaar.provider-url:}")
    private String providerUrl;

    @Override
    public DataSourceType getType() {
        return DataSourceType.AADHAAR_EKYC;
    }

    @Override
    public String getName() {
        return "Aadhaar e-KYC / Authentication";
    }

    @Override
    public boolean isAvailable() {
        return enabled && providerUrl != null && !providerUrl.isBlank();
    }

    @Override
    public ExternalDataResponse fetch(String identifier) {

        String safeIdentifier = maskIdentifier(identifier);

        if (identifier == null || identifier.isBlank()) {
            return ExternalDataResponse.failure(
                    getName(),
                    safeIdentifier,
                    "Identifier is required."
            );
        }

        if (!isAvailable()) {
            return ExternalDataResponse.failure(
                    getName(),
                    safeIdentifier,
                    "Authorized Aadhaar provider integration is not configured."
            );
        }

        /*
         * Production integration point:
         *
         * 1. Use only an authorized UIDAI/AUA/KUA/ASA integration.
         * 2. Authenticate the requesting officer/system.
         * 3. Obtain the required resident authorization/consent.
         * 4. Send the permitted authentication/e-KYC request
         *    through the authorized provider.
         * 5. Do not permanently store biometric/PID data.
         * 6. Store only the minimum legally permitted response/metadata.
         *
         * The actual provider API/SDK must be added here after
         * official credentials and integration specifications
         * are available.
         */

        Map<String, Object> data = new LinkedHashMap<>();

        data.put(
                "integrationStatus",
                "AUTHORIZED_PROVIDER_CONFIGURED"
        );

        data.put(
                "identifier",
                safeIdentifier
        );

        data.put(
                "providerUrlConfigured",
                true
        );

        data.put(
                "biometricStorage",
                "NOT_STORED"
        );

        data.put(
                "consentRequired",
                true
        );

        return ExternalDataResponse.success(
                getName(),
                safeIdentifier,
                data
        );
    }

    private String maskIdentifier(String identifier) {

        if (identifier == null || identifier.isBlank()) {
            return "MASKED";
        }

        String value = identifier.trim();

        if (value.length() <= 4) {
            return "****";
        }

        return "********" +
                value.substring(value.length() - 4);
    }
}