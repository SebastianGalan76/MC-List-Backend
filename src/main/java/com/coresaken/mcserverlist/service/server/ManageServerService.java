package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.*;
import com.coresaken.mcserverlist.data.response.RedirectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.data.dto.ServerStatusDto;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.*;
import com.coresaken.mcserverlist.database.model.server.staff.Player;
import com.coresaken.mcserverlist.database.model.server.staff.Rank;
import com.coresaken.mcserverlist.database.repository.NameRepository;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import com.coresaken.mcserverlist.service.BannerFileService;
import com.coresaken.mcserverlist.service.NewServerService;
import com.coresaken.mcserverlist.service.ServerStatusService;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.util.LinkChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public ResponseEntity<RedirectResponse> saveServerInfo(Server server, BasicServerDto serverDto) {
        ResponseEntity<RedirectResponse> response = serverService.checkServerInformation(serverDto, server);
        if(response.getStatusCode() != HttpStatus.OK){
            return response;
        }

        ServerStatusDto serverStatusDto = serverStatusService.getServerStatus(serverDto.ip(), serverDto.port());
        if(!serverStatusDto.online()){
            return RedirectResponse.badRequest(1, "", null);
        }

        serverService.saveBasicInformation(server, serverDto, serverStatusDto);
        serverRepository.save(server);
        return RedirectResponse.ok("Zmiany zostały zapisane prawidłowo", null);
    }

    public ResponseEntity<Response> saveServerStaff(Server server, StaffDto staffDto){
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
    }


    public ResponseEntity<Response> saveServerDescription(Server server, StringDto stringDto) {
        server.setDescription(stringDto.getText());
        serverRepository.save(server);

        return Response.ok("Zapisano prawidłowo opis serwera.");
    }

    public ResponseEntity<Response> saveServerLinks(Server server, List<Link> links) {
        server.getLinks().clear();
        server.getLinks().addAll(links);
        serverRepository.save(server);
        return Response.ok("Zapisano prawidłowo linki serwera.");
    }

    public ResponseEntity<Response> saveServerBanner(Server server, MultipartFile file, String url) {
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
                server.setBanner("/uploads/banners/" + uploadResponse.getBody().getMessage());
            }
            else{
                server.setBanner(null);
            }
        }

        serverRepository.save(server);
        return Response.ok("Ustawiłeś prawidłowo swój banner");
    }

    @Transactional
    public ResponseEntity<Response> saveServerRoles(Server server, ServerRoleDto serverRoleDto) {
        Set<ServerUserRole> ownerRole = server.getServerUserRoles().stream()
                .filter(role -> role.getRole() == ServerUserRole.Role.OWNER)
                .collect(Collectors.toSet());

        server.getServerUserRoles().clear();
        server.getServerUserRoles().addAll(ownerRole);
        if(serverRoleDto.getRoles() == null){
            return Response.ok(null);
        }

        Set<Long> savedIds = ownerRole.stream().map(sur -> sur.getUser().getId()).collect(Collectors.toSet());

        for(ServerRoleDto.RoleDto roleDto: serverRoleDto.getRoles()){
            User user = userService.getUserByEmailOrLogin(roleDto.getUser().getLogin());
            if(user == null || savedIds.contains(user.getId())){
                continue;
            }

            try{
                ServerUserRole.Role role = ServerUserRole.Role.valueOf(roleDto.getRole());
                ServerUserRole serverUserRole = new ServerUserRole();
                serverUserRole.setUser(user);
                serverUserRole.setServer(server);
                serverUserRole.setRole(role);
                server.getServerUserRoles().add(serverUserRole);
                savedIds.add(user.getId());
            }catch (IllegalArgumentException e){
                continue;
            }
        }

        serverRepository.save(server);
        return Response.ok("Zapisano prawidłowo uprawnienia.");
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
