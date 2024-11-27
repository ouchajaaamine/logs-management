package com.example;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import java.util.ArrayList;
import com.google.gson.Gson;
import org.elasticsearch.client.RequestOptions;

// This class manages log fetching tasks from Elasticsearch.
public class ElasticSearchClient {
    private RestHighLevelClient client;
    private String indexPattern;
    private LocalDateTime lastFetchTime;

    // Hardcoded server URL and API key for Elasticsearch
    private static final String SERVER_URL = "https://localhost:9200";
    private static final String API_KEY = "amtLeTQ1SUJoeXBuVTRjeTRGZGg6b05ReEd4aWxRZVNLOC1mdE5yUFhaQQ==";

    // Constructor initializes the Elasticsearch client and sets up index pattern and initial fetch time.
    public ElasticSearchClient(String indexPattern) {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost("localhost", 9200, "https"))
            .setDefaultHeaders(new Header[]{
                new BasicHeader("Authorization", "ApiKey " + API_KEY)
            });
        this.client = new RestHighLevelClient(builder);
        this.indexPattern = indexPattern;
        this.lastFetchTime = LocalDateTime.now();
    }

    // Starts the log fetching task, which runs every 2 seconds.
    public void startFetchingLogs() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                SearchRequest searchRequest = new SearchRequest(indexPattern);
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
                    .from(lastFetchTime.format(DateTimeFormatter.ISO_DATE_TIME)));
                searchRequest.source(sourceBuilder);

                SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
                List<LogEntry> newLogs = new ArrayList<>();
                for (SearchHit hit : response.getHits().getHits()) {
                    // Convert hit to LogEntry
                    LogEntry log = new Gson().fromJson(hit.getSourceAsString(), LogEntry.class);
                    newLogs.add(log);
                }

                if (!newLogs.isEmpty()) {
                    WebSocketServer.sendLogsToClients(newLogs);
                    setLastFetchTime(newLogs.get(newLogs.size() - 1).getTimestamp());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS); // Start immediately, repeat every 2 seconds
    }

    // Updates the last fetch time
    public void setLastFetchTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        this.lastFetchTime = LocalDateTime.parse(time, formatter);
    }
}