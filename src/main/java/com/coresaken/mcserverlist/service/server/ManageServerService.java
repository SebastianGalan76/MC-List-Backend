package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.*;
import com.coresaken.mcserverlist.data.response.RedirectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.data.dto.ServerStatusDto;
import com.coresaken.mcserverlist.database.model.server.*;
import com.coresaken.mcserverlist.database.repository.NameRepository;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import com.coresaken.mcserverlist.service.BannerFileService;
import com.coresaken.mcserverlist.service.NewServerService;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.util.LinkChecker;
import com.coresaken.mcserverlist.util.PermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageServerService {
    final ServerService serverService;

    final ServerRepository serverRepository;

    final NewServerService newServerService;
    final ServerStatusService serverStatusService;
    final UserService userService;
    final ModeService modeService;

    final NameRepository nameRepository;

    @Transactional
    public ResponseEntity<RedirectResponse> saveServerInfo(Long serverId, BasicServerDto serverDto) {
        Server server = serverService.getServerById(serverId);

        if(server==null){
            return RedirectResponse.badRequest(6,"Wystąpił nieoczekiwany błąd. Serwer o podanym ID nie istnieje!", null);
        }
        if(!PermissionChecker.hasPermissionForServer(userService.getLoggedUser(), server, ServerUserRole.Role.MODERATOR)){
            return RedirectResponse.badRequest(7,"Nie posiadasz wymaganych uprawnień, aby to zrobić!", null);
        }

        ResponseEntity<RedirectResponse> response = serverService.checkServerInformation(serverDto, server);
        if(response.getStatusCode() != HttpStatus.OK){
            return response;
        }

        ServerStatusDto serverStatusDto = serverStatusService.getServerStatus(serverDto.ip(), serverDto.port());
        if(serverStatusDto == null || !serverStatusDto.online()){
            return RedirectResponse.badRequest(8, "Nie możemy nawiązać połączenie z Twoim serwerem!", null);
        }

        serverService.saveBasicInformation(server, serverDto, serverStatusDto);
        serverRepository.save(server);
        return RedirectResponse.ok("Zmiany zostały prawidłowo zapisane.", null);
    }

    /*public ResponseEntity<Response> saveServerStaff(Server server, StaffDto staffDto){
        if (server.getStaff() == null) {
            server.setStaff(new ArrayList<>());
        }

        for(Rank rank:staffDto.getRankList()){
            if(rank!=null){
                List<Player> players = rank.getPlayers();
                if(players!=null){
                    for(Player player:players){
                        player.setRank(rank);
                    }
                }

                rank.setServer(server);
            }
        }

        server.getStaff().clear();
        server.getStaff().addAll(staffDto.getRankList());
        serverRepository.save(server);

        return Response.ok("Zapisano prawidłowo Administrację serwera.");
    }*/


    public ResponseEntity<Response> saveServerDescription(Long serverId, String description) {
        Server server = serverService.getServerById(serverId);

        if(server==null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Serwer o podanym ID nie istnieje!");
        }
        if(!PermissionChecker.hasPermissionForServer(userService.getLoggedUser(), server, ServerUserRole.Role.HELPER)){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        server.setDescription(description);
        serverRepository.save(server);
        return Response.ok("Opis serwera został prawidłowo zapisany.");
    }

    public ResponseEntity<Response> saveServerBanner(Long serverId, MultipartFile file, String url) {
        Server server = serverService.getServerById(serverId);

        if(server==null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Serwer o podanym ID nie istnieje.");
        }
        if(!PermissionChecker.hasPermissionForServer(userService.getLoggedUser(), server, ServerUserRole.Role.MODERATOR)){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        if(server.getBanner() != null){
            if(!LinkChecker.isLink(server.getBanner())){
                BannerFileService.remove(server.getBanner());
            }
        }

        if(url == null && file == null){
            server.setBanner(null);
        }

        if(LinkChecker.isLink(url)){
            server.setBanner(url);
        }
        else{
            if(file != null){
                ResponseEntity<Response> uploadResponse = BannerFileService.upload(file);
                if(uploadResponse.getStatusCode() != HttpStatus.OK){
                    return uploadResponse;
                }
                server.setBanner(uploadResponse.getBody().getMessage());
            }
            else{
                server.setBanner(null);
            }
        }

        serverRepository.save(server);
        return Response.ok("Ustawiłeś prawidłowo swój banner");
    }

    @Transactional
    public ResponseEntity<Response> saveSubServers(Server server, ListDto<SubServerDto> listDto) {
        server.getSubServers().clear();

        List<SubServerDto> subServerDTO = listDto.getData();
        if(!subServerDTO.isEmpty()){
            server.setMode(modeService.getNetworkMode());

            for(SubServerDto subServerDto:subServerDTO){
                SubServer subServer = new SubServer();
                subServer.setIndex(subServerDto.getIndex());

                subServer.setServer(server);
                subServer.setName(nameRepository.save(new Name(subServerDto.getName(), subServerDto.getColor())));
                subServer.setMode(subServerDto.getMode());
                subServer.setVersions(subServerDto.getVersions());

                server.getSubServers().add(subServer);
            }
        }

        serverRepository.save(server);
        return Response.ok("Zapisano prawidłowo informacje o podserwerach.");
    }
}
