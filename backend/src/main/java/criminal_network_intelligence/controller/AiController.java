package criminal_network_intelligence.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.ai.AiAnalysisRequest;
import criminal_network_intelligence.ai.AiAnalysisResponse;
import criminal_network_intelligence.service.AiAnalysisService;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
@RequestMapping("/api/ai")
public class AiController {

    private final AiAnalysisService aiAnalysisService;

    public AiController(
            AiAnalysisService aiAnalysisService
    ) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AiAnalysisResponse> analyze(
            @RequestBody AiAnalysisRequest request
    ) {

        return ResponseEntity.ok(
                aiAnalysisService.analyze(request)
        );
    }
}