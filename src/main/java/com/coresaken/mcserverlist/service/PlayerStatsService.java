package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.response.ServerStatus;
import com.coresaken.mcserverlist.database.model.server.HourlyPlayerCount;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerDetail;
import com.coresaken.mcserverlist.database.repository.HourlyPlayerCountRepository;
import com.coresaken.mcserverlist.database.repository.ServerDetailRepository;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PlayerStatsService {
    final int REFRESH_INTERVAL_MINUTE = 30;

    ServerRepository serverRepository;
    ServerDetailRepository serverDetailRepository;
    HourlyPlayerCountRepository hourlyPlayerCountRepository;
    ServerStatusService serverStatusService;

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    @PostConstruct
    public void init() {
        scheduleAllServers();
    }

    public void scheduleServer(Server server) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecutionTime = calculateNextRefreshTime(server.getNextRefreshAt());

        long delay = ChronoUnit.MILLIS.between(now, nextExecutionTime);
        scheduler.schedule(() -> refreshServer(server), delay, TimeUnit.MILLISECONDS);
    }

    private void scheduleAllServers() {
        List<Server> servers = serverRepository.findAllServersWithNextRefreshAtNotNull();
        for (Server server : servers) {
            scheduleServer(server);
        }
    }

    private void savePlayerStatistic(Server server, int playerCount) {
        LocalDateTime now = LocalDateTime.now();
        HourlyPlayerCount hourlyPlayerCount = new HourlyPlayerCount();
        hourlyPlayerCount.setHour(now);
        hourlyPlayerCount.setPlayerCount(playerCount);
        hourlyPlayerCount.setServer(server);

        List<HourlyPlayerCount> existingCounts = hourlyPlayerCountRepository.findByServerOrderByHourDesc(server);
        if (existingCounts.size() >= 48) {
            hourlyPlayerCountRepository.delete(existingCounts.get(existingCounts.size() - 1));
        }

        hourlyPlayerCountRepository.save(hourlyPlayerCount);
    }

    private void refreshServer(Server server) {
        ServerStatus serverResponse = serverStatusService.getServerStatus(server.getIp(), server.getPort());

        server.setOnline(serverResponse.isOnline());
        server.setOnlinePlayers(serverResponse.getPlayers().getOnline());

        ServerDetail serverDetail = server.getDetail();
        serverDetail.setIcon(serverResponse.getIcon());
        serverDetail.setMotdHtml(serverResponse.getMotd().getHtml());
        serverDetail.setMotdClean(serverResponse.getMotd().getClean());
        server.setDetail(serverDetail);

        server.setNextRefreshAt(LocalDateTime.now().plusMinutes(REFRESH_INTERVAL_MINUTE));
        savePlayerStatistic(server, serverResponse.getPlayers().getOnline());

        serverDetailRepository.save(serverDetail);
        serverRepository.save(server);

        scheduleServer(server);
    }

    private LocalDateTime calculateNextRefreshTime(LocalDateTime currentNextRefreshTime){
        LocalDateTime now = LocalDateTime.now();
        if (!currentNextRefreshTime.isAfter(now)) {
            while (currentNextRefreshTime.isBefore(now)) {
                currentNextRefreshTime = currentNextRefreshTime.plusMinutes(REFRESH_INTERVAL_MINUTE);
            }
        }
        return currentNextRefreshTime;
    }
}
