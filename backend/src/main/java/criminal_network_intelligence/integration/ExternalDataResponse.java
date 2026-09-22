package criminal_network_intelligence.integration;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExternalDataResponse {

    private boolean success;
    private String source;
    private String message;
    private String identifier;
    private Map<String, Object> data;

    public ExternalDataResponse() {
        this.data = new LinkedHashMap<>();
    }

    public ExternalDataResponse(
            boolean success,
            String source,
            String message,
            String identifier,
            Map<String, Object> data
    ) {
        this.success = success;
        this.source = source;
        this.message = message;
        this.identifier = identifier;
        this.data = data == null
                ? new LinkedHashMap<>()
                : data;
    }

    public static ExternalDataResponse success(
            String source,
            String identifier,
            Map<String, Object> data
    ) {
        return new ExternalDataResponse(
                true,
                source,
                "Data retrieved successfully.",
                identifier,
                data
        );
    }

    public static ExternalDataResponse failure(
            String source,
            String identifier,
            String message
    ) {
        return new ExternalDataResponse(
                false,
                source,
                message,
                identifier,
                new LinkedHashMap<>()
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data == null
                ? new LinkedHashMap<>()
                : data;
    }
}