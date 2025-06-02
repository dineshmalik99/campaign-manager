package com.lbg.hackathon.campaign_manager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.lbg.hackathon.campaign_manager.config.DataComposerConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.UUID;

@Service
public class ComposerService {

    private final DataComposerConfigs dataComposerConfigs;

//    private final String projectId = "triple-student-461610-i1";
//    private final String location = "europe-west2";
//    private final String environment = "test-hackathon-2";
//    private final String dagId = "my_composer_job";

    @Autowired
    public ComposerService(DataComposerConfigs dataComposerConfigs) {
        this.dataComposerConfigs = dataComposerConfigs;
    }

    public String getDagStatus(String dagRunId) throws Exception {
        // Load credentials with scope
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        // Use HttpCredentialsAdapter instead of .initialize()
        HttpRequestFactory requestFactory = new NetHttpTransport()
                .createRequestFactory(new HttpCredentialsAdapter(credentials));

        String statusUrl = String.format(
                "https://composer.googleapis.com/v1/projects/%s/locations/%s/environments/%s/dags/%s/dagRuns/%s",
                dataComposerConfigs.getProjectId(), dataComposerConfigs.getLocation(), dataComposerConfigs.getEnvId(), dataComposerConfigs.getDagId(), dagRunId);

        GenericUrl url = new GenericUrl(statusUrl);
        HttpResponse response = requestFactory.buildGetRequest(url).execute();

        // Parse response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response.getContent());

        return jsonResponse.toPrettyString();  // Or extract "state" field if needed
    }

    public void triggerDag(String campaignId) throws IOException, InterruptedException {
        // CONFIG
        String composerUri = "https://" + dataComposerConfigs.getEnvId() + "." + dataComposerConfigs.getLocation() + ".composer.googleusercontent.com";
        String triggerUrl = composerUri + "/api/v1/dags/" + dataComposerConfigs.getDagId() + "/dagRuns";

        // Get Google Credentials and ID token
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        if (!(credentials instanceof IdTokenProvider)) {
            throw new IOException("Credentials are not ID token provider");
        }

        IdTokenCredentials idTokenCredentials = IdTokenCredentials.newBuilder()
                .setIdTokenProvider((IdTokenProvider) credentials)
                .setTargetAudience(composerUri)
                .build();

        idTokenCredentials.refresh();


        Map<String, Object> body = Map.of(
                "executionId", UUID.randomUUID().toString(),
                "argument", "{\"campaign-id\":\"" + campaignId + "\"}",
                "dagName", dataComposerConfigs.getDagId()
        );

        String jsonBody = new ObjectMapper().writeValueAsString(body);

        // Create HTTP Request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(triggerUrl))
                .header("Authorization", "Bearer " + idTokenCredentials.getAccessToken().getTokenValue())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Send Request
        HttpClient client = HttpClient.newHttpClient();
        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        // Log response
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("DAG triggered successfully.");
        } else {
            System.err.println("Failed to trigger DAG. Status: " + response.statusCode());
            System.err.println("Response: " + response.body());
        }
    }
}
