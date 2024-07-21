package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.dto.NewServerDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.data.response.ServerStatus;
import com.coresaken.mcserverlist.database.model.server.*;
import com.coresaken.mcserverlist.database.repository.*;
import com.coresaken.mcserverlist.database.repository.server.ModeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewServerService {
    final ServerStatusService serverStatusService;

    final ServerRepository serverRepository;
    final SubServerRepository subServerRepository;

    final ServerDetailRepository serverDetailRepository;
    final NameRepository nameRepository;
    final ModeRepository modeRepository;
    final BlockedServerRepository blockedServerRepository;
    final HourlyPlayerCountRepository hourlyPlayerCountRepository;

    @Transient
    public Response addNewServer(NewServerDto newServerDto){
        Response response = checkServerData(newServerDto, null);
        if(response.getStatus() != HttpStatus.OK){
            return response;
        }

        ServerStatus serverStatus = serverStatusService.getServerStatus(newServerDto.getIp(), newServerDto.getPort());
        if(!serverStatus.isOnline()){
            return Response.builder().status(HttpStatus.NOT_FOUND).build();
        }

        Server server = new Server();
        server = serverRepository.save(server);
        saveBasicInformation(server, newServerDto, serverStatus);

        HourlyPlayerCount hourlyPlayerCount = new HourlyPlayerCount();
        hourlyPlayerCount.setHour(LocalDateTime.now());
        hourlyPlayerCount.setPlayerCount(serverStatus.getPlayers().getOnline());
        hourlyPlayerCount.setServer(server);
        hourlyPlayerCountRepository.save(hourlyPlayerCount);

        List<Mode> modes = newServerDto.getModes();
        if(!modes.isEmpty()){
            if(modes.size()> 1){
                int index = 0;
                for(Mode mode: newServerDto.getModes()){
                    SubServer subServer = new SubServer();

                    subServer.setParent(server);
                    subServer.setMode(mode);
                    subServer.setDetail(server.getDetail());

                    Name subServerName = nameRepository.save(new Name(mode.getName(), "ffffff"));
                    subServer.setName(subServerName);

                    subServer.setIndex(index);
                    index++;

                    subServerRepository.save(subServer);
                }
            }
        }

        server.setNextRefreshAt(LocalDateTime.now().plusMinutes(30));
        serverRepository.save(server);
        return Response.builder().status(HttpStatus.PERMANENT_REDIRECT).redirect("/server/"+server.getIp()).build();
    }

    private void saveBasicInformation(Server server, NewServerDto newServerDto, ServerStatus serverStatus){
        Name name = server.getName();
        if(name != null){
            name.setName(newServerDto.getIp());
            nameRepository.save(name);
        }
        else{
            name = nameRepository.save(new Name(newServerDto.getIp(), "ffffff"));
            server.setName(name);
        }

        server.setIp(newServerDto.getIp().toLowerCase());
        server.setPort(newServerDto.getPort());
        server.setOnline(serverStatus.isOnline());
        server.setOnlinePlayers(server.getOnlinePlayers());

        ServerDetail serverDetail = new ServerDetail();
        serverDetail.setMotdHtml(serverStatus.getMotd().getHtml());
        serverDetail.setMotdClean(serverStatus.getMotd().getClean());
        serverDetail.setIcon(serverStatus.getIcon());
        serverDetail = serverDetailRepository.save(serverDetail);
        server.setDetail(serverDetail);

        if(server.getVersions() == null){
            server.setVersions(newServerDto.getVersions());
        }
        else{
            server.getVersions().clear();
            server.getVersions().addAll(newServerDto.getVersions());
        }

        List<Mode> modes = newServerDto.getModes();
        if(!modes.isEmpty()){
            if(modes.size()==1){
                server.setMode(modes.get(0));
            }
            else{
                server.setMode(modeRepository.getReferenceById(0L));
            }
        }
        else{
            server.setMode(null);
        }
    }
    private Response checkServerData(NewServerDto serverDto, Server server){
        if(serverDto.getIp().contains("aternos.me")){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Nie możesz dodawać serwera aternos!").build();
        }

        BlockedServer blockedServer = blockedServerRepository.findByIp(serverDto.getIp()).orElse(null);
        if(blockedServer!=null){
            if(blockedServer.getServer()!=null){
                return Response.builder().status(HttpStatus.PERMANENT_REDIRECT).redirect("/server/"+ serverDto.getIp()).build();
            }
            else {
                return Response.builder().status(HttpStatus.BAD_REQUEST).message("Ten adres IP jest obecnie zablokowany! Jeśli uważasz to za błąd, skontaktuj się z nami.").build();
            }
        }

        if(serverRepository.findByIp(serverDto.getIp()).isPresent()){
            if(server == null){
                return Response.builder().status(HttpStatus.BAD_REQUEST).message("Serwer znajduje się już na liście.").build();
            }
            else{
                if(!server.getIp().equalsIgnoreCase(serverDto.getIp())){
                    return Response.builder().status(HttpStatus.BAD_REQUEST).message("Serwer znajduje się już na liście.").build();
                }
            }
        }

        return Response.builder().status(HttpStatus.OK).build();
    }
}
