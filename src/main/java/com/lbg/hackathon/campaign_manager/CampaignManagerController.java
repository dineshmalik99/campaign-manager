package com.lbg.hackathon.campaign_manager;

import com.lbg.hackathon.campaign_manager.model.JobRequest;
import com.lbg.hackathon.campaign_manager.service.BigQueryService;
import com.lbg.hackathon.campaign_manager.service.CampaignService;
import com.lbg.hackathon.campaign_manager.service.ComposerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/campaign-manager")
public class CampaignManagerController {

    private final CampaignService campaignService;
    private final ComposerService composerStatusService;
    private final BigQueryService bigQueryService;

    public CampaignManagerController(CampaignService campaignService, ComposerService composerStatusService, BigQueryService bigQueryService) {
        this.campaignService = campaignService;
        this.composerStatusService = composerStatusService;
        this.bigQueryService = bigQueryService;
    }

//    @PostMapping("/start")
//    public ResponseEntity<String> startCampaign(@RequestBody CampaignRequest request) {
//        campaignService.handleCampaignRequest(request);
//        return ResponseEntity.ok("Campaign scheduled or started");
//    }

//    @GetMapping("/status")
//    public String getDagStatus(@RequestParam String dagRunId) throws Exception {
//        return composerStatusService.getDagStatus(dagRunId); // Contains current DAG status
//    }

    @GetMapping("/status")
    public List<Map<String, Object>> getJobs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bigQueryService.getJobs(status, page, size);
    }

    @PostMapping("/submit")
    public String submitJob(@Valid @RequestBody JobRequest jobRequest) {
        bigQueryService.insertJob(jobRequest);
        campaignService.handleCampaignRequest(jobRequest);
        return "Job submitted successfully";
    }

    @PostMapping("/cancel")
    public String cancelJob(@Valid @RequestBody JobRequest jobRequest) {
        bigQueryService.insertJob(jobRequest);
        campaignService.handleCampaignRequest(jobRequest);
        return "Job submitted successfully";
    }
}
