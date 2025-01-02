package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.dto.BasicServerDto;
import com.coresaken.mcserverlist.data.response.RedirectResponse;
import com.coresaken.mcserverlist.data.dto.ServerStatusDto;
import com.coresaken.mcserverlist.database.model.server.*;
import com.coresaken.mcserverlist.database.repository.*;
import com.coresaken.mcserverlist.database.repository.server.DailyPlayerCountRepository;
import com.coresaken.mcserverlist.database.repository.server.ModeRepository;
import com.coresaken.mcserverlist.service.server.ServerService;
import com.coresaken.mcserverlist.service.server.ServerStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewServerService {
    final ServerService serverService;

    final ServerStatusService serverStatusService;

    final ServerRepository serverRepository;
    final SubServerRepository subServerRepository;

    final NameRepository nameRepository;
    final ModeRepository modeRepository;

    final HourlyPlayerCountRepository hourlyPlayerCountRepository;
    final DailyPlayerCountRepository dailyPlayerCountRepository;

    @Transactional
    public ResponseEntity<RedirectResponse> addNewServer(BasicServerDto basicServerDto){
        ResponseEntity<RedirectResponse> response = serverService.checkServerInformation(basicServerDto, null);
        if(response.getStatusCode() != HttpStatus.OK){
            return response;
        }

        ServerStatusDto serverStatusDto = serverStatusService.getServerStatus(basicServerDto.ip(), basicServerDto.port());
        if(serverStatusDto == null || !serverStatusDto.online()){
            return RedirectResponse.badRequest(1, "Nie znaleziono serwera!", null);
        }

        Server server = serverRepository.save(new Server());
        serverService.saveBasicInformation(server, basicServerDto, serverStatusDto);

        LocalDateTime now = LocalDateTime.now();
        HourlyPlayerCount hourlyPlayerCount = new HourlyPlayerCount();
        hourlyPlayerCount.setTime(now);
        hourlyPlayerCount.setPlayerCount(serverStatusDto.players().online());
        hourlyPlayerCount.setServer(server);
        hourlyPlayerCountRepository.save(hourlyPlayerCount);

        DailyPlayerCount dailyPlayerCount = new DailyPlayerCount();
        dailyPlayerCount.setTime(now);
        dailyPlayerCount.setPlayerCount(serverStatusDto.players().online());
        dailyPlayerCount.setServer(server);
        dailyPlayerCountRepository.save(dailyPlayerCount);

        List<Mode> modes = basicServerDto.modes();
        if(modes != null && !modes.isEmpty()){
            if(modes.size() == 1){
                server.setMode(modes.get(0));
            }
            else{
                server.setMode(modeRepository.getReferenceById(1L));
                int index = 0;
                for(Mode mode:modes){
                    if(mode.getId() == 1){
                        continue;
                    }

                    SubServer subServer = new SubServer();
                    subServer.setMode(mode);
                    subServer.setName(nameRepository.save(new Name(mode.getName(), "#ffffff")));
                    subServer.setVersions(basicServerDto.versions());
                    subServer.setIndex(index);
                    subServer.setServer(server);

                    subServerRepository.save(subServer);
                    if(server.getSubServers() == null){
                        server.setSubServers(new ArrayList<>());
                    }
                    server.getSubServers().add(subServer);
                    index++;
                }
            }
        }

        server.setNextRefreshAt(now.plusMinutes(PlayerStatsService.REFRESH_INTERVAL_MINUTE));
        server.setCreatedAt(now);

        serverRepository.save(server);

        return RedirectResponse.ok("Serwer został prawidłowo dodany.", "/server/"+server.getIp());
    }
}
