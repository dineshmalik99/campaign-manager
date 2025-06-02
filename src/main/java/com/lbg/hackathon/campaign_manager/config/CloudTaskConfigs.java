package com.lbg.hackathon.campaign_manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloud-task")
@Getter
@Setter
public class CloudTaskConfigs {
    private String projectId;
    private String location;
    private String queueId;
    private String url;
    private String composerEndpoint;
}
