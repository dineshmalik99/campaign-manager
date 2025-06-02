package com.lbg.hackathon.campaign_manager.model;

import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;


@Getter
public class JobRequest {

    @NonNull
    private String campaign_id;
    private String campaign_name;
    private String campaign_description;
    private String camoaign_message_id;
    @NonNull
    private LocalDateTime campaign_submit_time;

    @NonNull
    private String campaign_run_status;
    @NonNull
    private String campaign_sql_query;

}
