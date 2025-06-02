package com.lbg.hackathon.campaign_manager.service;

import com.google.cloud.bigquery.*;
import com.lbg.hackathon.campaign_manager.config.BigQueryConfigs;
import com.lbg.hackathon.campaign_manager.model.JobRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BigQueryService {

//    private static final String DATASET_NAME = "hackathon1";
//    private static final String TABLE_NAME = "job_table";
//    private static final String PROJECT_ID = "triple-student-461610-i1";

    private final BigQueryConfigs bigQueryConfigs;

    private static final String[] VALID_STATUSES = {
            "SUBMITTED", "SCHEDULED", "CANCELLED", "COMPLETED", "FAILED"
    };

    @Autowired
    public BigQueryService(BigQueryConfigs bigQueryConfigs) {
        this.bigQueryConfigs = bigQueryConfigs;
    }

    public void insertJob(JobRequest request) {
        if (Arrays.stream(VALID_STATUSES).noneMatch(request.getCampaign_run_status()::equalsIgnoreCase)) {
            throw new IllegalArgumentException("Invalid status: " + request.getCampaign_run_status());
        }

        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

        TableId tableId = TableId.of(bigQueryConfigs.getProjectId(), bigQueryConfigs.getDataset(), bigQueryConfigs.getTable());
        LocalDateTime inputTime = LocalDateTime.parse(request.getCampaign_run_start_time().toString());
        String formatted = inputTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String campaign_id = UUID.randomUUID().toString();
        InsertAllRequest.RowToInsert row = InsertAllRequest.RowToInsert.of(
                campaign_id,
                Map.of(
                        "campaign_id", campaign_id,
                        "campaign_name", request.getCampaign_name(),
                        "campaign_description", request.getCampaign_description(),
                        "campaign_message_id", request.getCampaign_message_id(),
                        "campaign_run_start_time", formatted,
                        "campaign_submit_time", Instant.now().toString(),
                        "campaign_run_status", request.getCampaign_run_status().toUpperCase(),
                        "campaign_sql_query", request.getCampaign_sql_query()
                )
        );

        InsertAllResponse response = bigquery.insertAll(InsertAllRequest.newBuilder(tableId)
                .addRow(row)
                .build());

        if (response.hasErrors()) {
            throw new RuntimeException("BigQuery insert failed: " + response.getInsertErrors());
        }
    }

    public List<Map<String, Object>> getJobs(String status, int page, int size) {
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

        String tableName = String.format("`%s.%s.%s`", bigQueryConfigs.getProjectId(), bigQueryConfigs.getDataset(), bigQueryConfigs.getTable());

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName);

        if (status != null && !status.isBlank()) {
            queryBuilder.append(" WHERE status = @status");
        }

        queryBuilder.append(" ORDER BY timestamp DESC");
        queryBuilder.append(" LIMIT @limit OFFSET @offset");

        QueryJobConfiguration.Builder configBuilder = QueryJobConfiguration.newBuilder(queryBuilder.toString())
                .addNamedParameter("limit", QueryParameterValue.int64(size))
                .addNamedParameter("offset", QueryParameterValue.int64(page * size));

        if (status != null && !status.isBlank()) {
            configBuilder.addNamedParameter("status", QueryParameterValue.string(status));
        }

        QueryJobConfiguration queryConfig = configBuilder.build();

        TableResult result;
        try {
            result = bigquery.query(queryConfig);
        } catch (InterruptedException e) {
            throw new RuntimeException("Query interrupted", e);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (FieldValueList row : result.iterateAll()) {
            Map<String, Object> record = new HashMap<>();
            for (Field field : result.getSchema().getFields()) {
                record.put(field.getName(), row.get(field.getName()).getValue());
            }
            rows.add(record);
        }

        return rows;
    }

    public TableResult getJobById(String id) {
        BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();

        String tableName = String.format("`%s.%s.%s`", bigQueryConfigs.getProjectId(), bigQueryConfigs.getDataset(), bigQueryConfigs.getTable());
        String query = String.format(
                "SELECT * FROM `%s` WHERE id = '%s' LIMIT 1", tableName,
                id
        );

        QueryJobConfiguration config = QueryJobConfiguration.newBuilder(query).build();
        TableResult result = null;
        try {
            result = bigQuery.query(config);
        } catch (InterruptedException e) {
            System.out.println("internal error" + e);
            e.printStackTrace();
        }

        return result;
    }

    public void updateJob(String id, String newStatus, String newDescription) throws InterruptedException {
        BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
        String tableName = String.format("`%s.%s.%s`", bigQueryConfigs.getProjectId(), bigQueryConfigs.getDataset(), bigQueryConfigs.getTable());
        String query = String.format("UPDATE `%s` SET status = '%s', description = '%s' WHERE id = '%s'", tableName, newStatus, newDescription, id);

        QueryJobConfiguration config = QueryJobConfiguration.newBuilder(query).build();
        bigQuery.query(config);
    }

}
