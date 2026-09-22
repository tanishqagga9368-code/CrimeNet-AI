package criminal_network_intelligence.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import criminal_network_intelligence.service.LocationIntelligenceService;

@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class LocationIntelligenceController {

    private final LocationIntelligenceService
            locationIntelligenceService;

    public LocationIntelligenceController(
            LocationIntelligenceService locationIntelligenceService
    ) {
        this.locationIntelligenceService =
                locationIntelligenceService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>>
    getLocationIntelligence() {

        return ResponseEntity.ok(
                locationIntelligenceService
                        .getLocationIntelligence()
        );
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<Map<String, Object>>
    getCaseLocations(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                locationIntelligenceService
                        .getCaseLocations(caseId)
        );
    }

    @GetMapping("/activity")
    public ResponseEntity<List<Map<String, Object>>>
    getLocationActivity(
            @RequestParam String location
    ) {

        return ResponseEntity.ok(
                locationIntelligenceService
                        .getLocationActivity(location)
        );
    }

    @GetMapping("/movement")
    public ResponseEntity<List<Map<String, Object>>>
    getMovementTimeline(
            @RequestParam String entityName
    ) {

        return ResponseEntity.ok(
                locationIntelligenceService
                        .getMovementTimeline(entityName)
        );
    }

    @GetMapping("/shared")
    public ResponseEntity<List<Map<String, Object>>>
    getSharedLocations() {

        return ResponseEntity.ok(
                locationIntelligenceService
                        .getSharedLocations()
        );
    }
}