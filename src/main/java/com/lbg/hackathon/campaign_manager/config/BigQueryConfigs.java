package com.lbg.hackathon.campaign_manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bigquery")
@Getter
@Setter
public class BigQueryConfigs {
    private String projectId;
    private String dataset;
    private String table;
}
