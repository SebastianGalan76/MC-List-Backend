package com.coresaken.mcserverlist.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class DontSleepService {
    final RestTemplate restTemplate;

    @Scheduled(fixedRate = 1200000) // 1200000 ms = 20 minut
    public void sendTestMessage() {
        String url = "https://mc-list.pl/dont-sleep-buddy";
        String message = "Test";
        restTemplate.postForObject(url, message, String.class);
    }
}
