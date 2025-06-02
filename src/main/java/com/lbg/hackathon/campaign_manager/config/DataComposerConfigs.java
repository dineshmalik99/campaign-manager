package com.lbg.hackathon.campaign_manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "data-composer")
@Getter
@Setter
public class DataComposerConfigs {
    private String envId;
    private String location;
    private String projectId;
    private String dagId;
}
