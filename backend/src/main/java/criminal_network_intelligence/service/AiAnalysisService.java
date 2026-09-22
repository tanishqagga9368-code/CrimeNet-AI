package criminal_network_intelligence.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import criminal_network_intelligence.ai.AiAnalysisRequest;
import criminal_network_intelligence.ai.AiAnalysisResponse;

@Service
public class AiAnalysisService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String aiServiceUrl;

    public AiAnalysisService(
            ObjectMapper objectMapper,
            @Value("${ai.service.url:http://127.0.0.1:8000}")
            String aiServiceUrl
    ) {
        this.objectMapper = objectMapper;
        this.aiServiceUrl = aiServiceUrl;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public AiAnalysisResponse analyze(
            AiAnalysisRequest request
    ) {

        if (request == null) {
            request = new AiAnalysisRequest();

            request.setEntityId("UNKNOWN");
            request.setEntityName("Unknown Entity");
        }

        try {

            String requestJson =
                    objectMapper.writeValueAsString(request);

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            aiServiceUrl
                                                    + "/api/ai/analyze-entity"
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .timeout(
                                    Duration.ofSeconds(15)
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(requestJson)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() >= 200
                    && response.statusCode() < 300) {

                return objectMapper.readValue(
                        response.body(),
                        AiAnalysisResponse.class
                );
            } else {
                System.err.println("AiAnalysisService HTTP STATUS: " + response.statusCode() + " BODY: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("AiAnalysisService ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return fallbackResponse(request);
    }

    public AiAnalysisResponse analyze(
            String entityName
    ) {

        AiAnalysisRequest request =
                new AiAnalysisRequest();

        request.setEntityId("UNKNOWN");
        request.setEntityName(
                entityName == null || entityName.isBlank()
                        ? "Unknown Entity"
                        : entityName
        );

        return analyze(request);
    }

    private AiAnalysisResponse fallbackResponse(
            AiAnalysisRequest request
    ) {

        AiAnalysisResponse response =
                new AiAnalysisResponse();

        response.setEntityId(
                request.getEntityId() == null
                        ? "UNKNOWN"
                        : request.getEntityId()
        );

        response.setEntityName(
                request.getEntityName() == null
                        || request.getEntityName().isBlank()
                        ? "Unknown Entity"
                        : request.getEntityName()
        );

        response.setRisk("HIGH");
        response.setSuspicionScore(85);
        response.setConfidence(80);
        response.setConnections(3);

        response.setExplanation(
                java.util.List.of(
                        "AI service is currently unavailable.",
                        "Fallback analytical response generated.",
                        "This score is an investigative indicator, not a determination of guilt."
                )
        );

        return response;
    }
}