package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.EntityResolutionService;

@RestController
@RequestMapping("/api/entity-resolution")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class EntityResolutionController {

    private final EntityResolutionService entityResolutionService;

    public EntityResolutionController(
            EntityResolutionService entityResolutionService
    ) {
        this.entityResolutionService =
                entityResolutionService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> resolveEntities() {

        return ResponseEntity.ok(
                entityResolutionService.resolveEntities()
        );
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Map<String, Object>>> resolveCase(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                entityResolutionService.resolveCase(caseId)
        );
    }

    @GetMapping("/entity")
    public ResponseEntity<List<Map<String, Object>>> resolveEntity(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                entityResolutionService.resolveEntity(name)
        );
    }
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeEntities(@RequestBody Map<String, Object> payload) {
        String primary = String.valueOf(payload.getOrDefault("primaryEntity", ""));
        String duplicate = String.valueOf(payload.getOrDefault("duplicateEntity", ""));
        String notes = String.valueOf(payload.getOrDefault("notes", ""));
        String officer = String.valueOf(payload.getOrDefault("officer", "Investigating Officer"));

        return ResponseEntity.ok(
                entityResolutionService.mergeEntities(primary, duplicate, notes, officer)
        );
    }

}
