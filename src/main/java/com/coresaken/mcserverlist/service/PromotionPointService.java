package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.database.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionPointService {
    final ServerRepository serverRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void executeTask() {
        serverRepository.decreasePromotionPoints();
    }
}
