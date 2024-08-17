package com.coresaken.mcserverlist.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class DontSleepService {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Scheduled(fixedRate = 1200000) // 1200000 ms = 20 minut
    public void sendTestMessage() {
        try {
            String url = "https://mc-list.pl/dont-sleep-buddy";

            String message = "Test";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "text/plain")
                    .POST(HttpRequest.BodyPublishers.ofString(message, StandardCharsets.UTF_8))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace(); // Obsługa błędów
        }
    }

}
