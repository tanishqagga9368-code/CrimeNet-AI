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

import criminal_network_intelligence.service.TimelineService;

@RestController
@RequestMapping("/api/timeline")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(
            TimelineService timelineService
    ) {
        this.timelineService = timelineService;
    }

    /**
     * Complete intelligence timeline.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTimeline() {

        return ResponseEntity.ok(
                timelineService.getAllTimeline()
        );
    }

    /**
     * Timeline for a particular case.
     *
     * Example:
     * /api/timeline/case/CR-2026-041
     */
    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Map<String, Object>>> getCaseTimeline(
            @PathVariable String caseId
    ) {

        return ResponseEntity.ok(
                timelineService.getCaseTimeline(caseId)
        );
    }

    /**
     * Timeline filtered by intelligence record type.
     *
     * Example:
     * /api/timeline/type/CDR
     */
    @GetMapping("/type")
    public ResponseEntity<List<Map<String, Object>>> getTimelineByType(
            @RequestParam String recordType
    ) {

        return ResponseEntity.ok(
                timelineService.getTimelineByType(
                        recordType
                )
        );
    }
}