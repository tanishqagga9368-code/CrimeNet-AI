package criminal_network_intelligence.controller;

import criminal_network_intelligence.service.NetworkSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/network/simulation")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class NetworkSimulationController {

    private final NetworkSimulationService
            networkSimulationService;

    public NetworkSimulationController(
            NetworkSimulationService networkSimulationService
    ) {
        this.networkSimulationService =
                networkSimulationService;
    }

    @GetMapping("/remove/{entityId}")
    public ResponseEntity<Map<String, Object>>
    simulateNodeRemoval(
            @PathVariable String entityId
    ) {

        return ResponseEntity.ok(
                networkSimulationService
                        .simulateNodeRemoval(entityId)
        );
    }

    @GetMapping("/critical-nodes")
    public ResponseEntity<List<Map<String, Object>>>
    getCriticalNodes(
            @RequestParam(
                    defaultValue = "10"
            ) int limit
    ) {

        return ResponseEntity.ok(
                networkSimulationService
                        .getCriticalNodes(limit)
        );
    }
}