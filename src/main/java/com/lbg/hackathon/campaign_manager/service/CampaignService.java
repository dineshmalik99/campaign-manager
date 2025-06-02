package com.lbg.hackathon.campaign_manager.service;

import com.lbg.hackathon.campaign_manager.model.JobRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.chrono.ChronoLocalDateTime;

@Service
public class CampaignService {

    private final ComposerService composerService;
    private final BigQueryService bigQueryService;
    private final CloudTaskService cloudTaskService;

    @Autowired
    public CampaignService(ComposerService composerService, BigQueryService bigQueryService, CloudTaskService cloudTaskService) {
        this.composerService = composerService;
        this.bigQueryService = bigQueryService;
        this.cloudTaskService = cloudTaskService;
    }


    public void handleCampaignRequest(JobRequest request) {

        if (bigQueryService.getJobById(request.getCampaign_id()).getTotalRows() > 0) {
            throw new IllegalStateException("Job ID already exists.");
        }
        Instant now = Instant.now();
        if (request.getCampaign_run_start_time().isAfter(ChronoLocalDateTime.from(now.plusSeconds(60)))) {
            bigQueryService.insertJob(request);
            cloudTaskService.createCloudTask(request);
        } else {
            throw new IllegalStateException("Starttime must be of future");
        }
    }




}
