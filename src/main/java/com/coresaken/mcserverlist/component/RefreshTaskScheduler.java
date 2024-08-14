package com.coresaken.mcserverlist.component;

import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import com.coresaken.mcserverlist.service.PlayerStatsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class RefreshTaskScheduler {
    final ServerRepository serverRepository;
    final RefreshTaskQueue taskQueue;
    final PlayerStatsService playerStatsService;
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void startProcessing() {
        executorService.submit(() -> {
            try {
                while (true) {
                    System.out.println("EX");
                    Server server = taskQueue.takeTask();
                    if(server == null){
                        continue;
                    }
                    System.out.println("EX2");
                    LocalDateTime now = LocalDateTime.now();
                    if(server.getNextRefreshAt().isAfter(now.minusMinutes(1))){
                        playerStatsService.refreshServer(server);
                        System.out.println("R "+server.getIp());
                    }
                    else{
                        LocalDateTime time = server.getNextRefreshAt();
                        long minutesDifference = java.time.Duration.between(time, now).toMinutes();
                        long increments = (minutesDifference / 30) + 1;
                        System.out.println("SKIP "+server.getIp());
                        server.setNextRefreshAt(time.plusMinutes(increments * 30));
                        serverRepository.save(server);
                    }

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Scheduled(fixedRate = 60000)
    public void scheduleTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Server> serversToRefresh = serverRepository.findServersToRefresh(now);
        System.out.println("TR "+serversToRefresh.size());
        for (Server server : serversToRefresh) {
            taskQueue.addTask(server);
        }
    }
}