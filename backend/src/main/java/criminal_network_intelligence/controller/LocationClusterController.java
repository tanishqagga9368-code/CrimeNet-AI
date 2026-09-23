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

import criminal_network_intelligence.service.LocationClusterService;

@RestController
@RequestMapping("/api/location/clusters")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class LocationClusterController {

    private final LocationClusterService locationClusterService;

    public LocationClusterController(
            LocationClusterService locationClusterService
    ) {
        this.locationClusterService =
                locationClusterService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>>
    getLocationClusters() {

        return ResponseEntity.ok(
                locationClusterService
                        .getLocationClusters()
        );
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Map<String, Object>>>
    getCaseLocationClusters(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                locationClusterService
                        .getCaseLocationClusters(caseId)
        );
    }

    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>>
    analyzeLocation(
            @RequestParam String location
    ) {

        return ResponseEntity.ok(
                locationClusterService
                        .analyzeLocation(location)
        );
    }

    @GetMapping("/shared")
    public ResponseEntity<List<Map<String, Object>>>
    getSharedLocations() {

        return ResponseEntity.ok(
                locationClusterService
                        .getSharedLocations()
        );
    }
}