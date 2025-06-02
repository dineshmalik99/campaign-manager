package com.lbg.hackathon.campaign_manager.model;

import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;


@Getter
public class JobRequest {

    @NonNull
    private String id;
    private String name;
    private String description;
    private String message;
    @NonNull
    private LocalDateTime starttime;

    @NonNull
    private String status;
    @NonNull
    private String sqlQuery;

    // Getters and Setters
}
