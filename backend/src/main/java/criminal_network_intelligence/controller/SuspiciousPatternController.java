package criminal_network_intelligence.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.SuspiciousPatternService;

@RestController
@RequestMapping("/api/analytics/suspicious")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class SuspiciousPatternController {

    private final SuspiciousPatternService suspiciousPatternService;

    public SuspiciousPatternController(
            SuspiciousPatternService suspiciousPatternService
    ) {
        this.suspiciousPatternService =
                suspiciousPatternService;
    }

    @GetMapping("/patterns")
    public ResponseEntity<Map<String, Object>> detectPatterns() {

        return ResponseEntity.ok(
                suspiciousPatternService.detectPatterns()
        );
    }
}