package criminal_network_intelligence;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://crime-net-ai-1dsp.vercel.app"}, originPatterns = {"https://*.vercel.app"})
public class StatsController {

    @GetMapping("/api/dashboard")
    public Map<String, Object> getDashboardStats() {

        return Map.of(
                "totalSuspects", 1284,
                "activeCases", 47,
                "networkConnections", 3692,
                "highRiskEntities", 26
        );
    }
}