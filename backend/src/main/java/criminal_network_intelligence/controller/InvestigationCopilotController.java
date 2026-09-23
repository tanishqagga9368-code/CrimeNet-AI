package criminal_network_intelligence.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.InvestigationCopilotService;

@RestController
@RequestMapping("/api/copilot")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class InvestigationCopilotController {

    private final InvestigationCopilotService copilotService;

    public InvestigationCopilotController(
            InvestigationCopilotService copilotService
    ) {
        this.copilotService = copilotService;
    }

    /**
     * Ask the investigation copilot.
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(
            @RequestBody Map<String, Object> request
    ) {

        String question =
                getString(
                        request.get("question")
                );

        String entityId =
                getString(
                        request.get("entityId")
                );

        String caseId =
                getString(
                        request.get("caseId")
                );

        return ResponseEntity.ok(
                copilotService.ask(
                        question,
                        entityId,
                        caseId
                )
        );
    }

    /**
     * Simple health/status endpoint.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "service",
                "Investigation Copilot"
        );

        response.put(
                "status",
                "READY"
        );

        response.put(
                "mode",
                "Graph Intelligence + Rule-based NLP"
        );

        response.put(
                "supportedQueries",
                new String[]{
                        "connected entities",
                        "cross-case connections",
                        "timeline",
                        "key persons",
                        "graph statistics",
                        "risk indicators"
                }
        );

        return ResponseEntity.ok(response);
    }

    private String getString(Object value) {

        if (value == null) {
            return null;
        }

        String text =
                value.toString().trim();

        return text.isBlank()
                ? null
                : text;
    }
}