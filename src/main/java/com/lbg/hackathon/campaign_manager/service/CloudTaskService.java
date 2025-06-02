package com.lbg.hackathon.campaign_manager.service;


import com.google.cloud.bigquery.*;
import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Timestamps;
import com.lbg.hackathon.campaign_manager.config.CloudTaskConfigs;
import com.lbg.hackathon.campaign_manager.model.JobRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class CloudTaskService {

    private final CloudTaskConfigs cloudTaskConfigs;
    private final CloudTasksClient tasksClient = CloudTasksClient.create();

    private final BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();

    public CloudTaskService(CloudTaskConfigs cloudTaskConfigs) throws IOException {
        this.cloudTaskConfigs = cloudTaskConfigs;
    }

//    public void scheduleCloudRunJobAt(LocalDateTime starttime) {
//        String projectId = cloudTaskConfigs.getProjectId();
//        String location = cloudTaskConfigs.getLocation();
//        String queueId = cloudTaskConfigs.getQueueId();
//        String url = cloudTaskConfigs.getUrl(); // Must be public or secured with ID token
//        String payload = "{}"; // Optional JSON body
//
//        CloudTasksClient tasksClient = null;
//        try {
//            tasksClient = CloudTasksClient.create();
//        } catch (IOException e) {
//            System.out.println("Error in creating task");
//            e.printStackTrace();
//        }
//        QueueName queueName = QueueName.of(projectId, location, queueId);
//
//        HttpRequest httpRequest = HttpRequest.newBuilder()
//                .setUrl(url)
//                .setHttpMethod(HttpMethod.POST)
//                .putHeaders("Content-Type", "application/json")
//                .setBody(ByteString.copyFromUtf8(payload))
//                .build();
//
//        Instant scheduledTime = starttime.atZone(ZoneId.systemDefault()).toInstant();
//        Task task = Task.newBuilder()
//                .setHttpRequest(httpRequest)
//                .setScheduleTime(Timestamps.fromMillis(scheduledTime.toEpochMilli()))
//                .build();
//
//        tasksClient.createTask(queueName, task);
//        tasksClient.shutdown();
//    }

    public String createCloudTask(JobRequest job) {
        String parent = QueueName.of(cloudTaskConfigs.getProjectId(), cloudTaskConfigs.getLocation(), cloudTaskConfigs.getQueueId()).toString();
        String payload = "{\"id\":\"" + job.getId() + "\"}";

        Task task = Task.newBuilder()
                .setHttpRequest(HttpRequest.newBuilder()
                        .setUrl(cloudTaskConfigs.getComposerEndpoint())
                        .setHttpMethod(HttpMethod.POST)
                        .setBody(ByteString.copyFromUtf8(payload))
                        .build())
                .build();

        Task createdTask = tasksClient.createTask(parent, task);
        return createdTask.getName();
    }

    public boolean cancelJob(String id) throws Exception {
        TableId mappingTableId = TableId.of("your_dataset", "task_mapping_table");

        String query = String.format("SELECT taskName FROM `%s.%s.%s` WHERE id='%s'",
                cloudTaskConfigs.getProjectId(), mappingTableId.getDataset(), mappingTableId.getTable(), id);
        TableResult result = bigQuery.query(QueryJobConfiguration.newBuilder(query).build());

        if (!result.iterateAll().iterator().hasNext()) return false;

        String taskName = result.iterateAll().iterator().next().get("taskName").getStringValue();
        tasksClient.deleteTask(taskName);
        return true;
    }

}
