package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.BasicServerDto;
import com.coresaken.mcserverlist.data.dto.ServerDto;
import com.coresaken.mcserverlist.data.dto.ServerStatusDto;
import com.coresaken.mcserverlist.data.mapper.ServerMapper;
import com.coresaken.mcserverlist.data.response.RedirectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.*;
import com.coresaken.mcserverlist.database.repository.*;
import com.coresaken.mcserverlist.database.repository.server.ModeRepository;
import com.coresaken.mcserverlist.service.ServerStatusService;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.util.PermissionChecker;
import com.coresaken.mcserverlist.util.UnicodeConverter;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServerService {
    final UserService userService;
    final ServerStatusService serverStatusService;

    final ServerRepository serverRepository;
    final SubServerRepository subServerRepository;

    final NameRepository nameRepository;
    final ModeRepository modeRepository;

    final ServerDetailRepository serverDetailRepository;
    final BlockedServerRepository blockedServerRepository;

    final PlayerRatingRepository playerRatingRepository;

    public ServerDto getServer(String serverIp){
        Server server = serverRepository.findByIp(serverIp).orElse(null);
        if(server != null){
            return ServerMapper.toDTO(server, userService.getLoggedUser(), playerRatingRepository.findByServer(server));
        }
        return null;
    }

    public Page<Server> getServers(int page){
        Pageable pageable = PageRequest.of(page - 1, 30);
        return serverRepository.findAllOrderByVotesAndId(pageable);
    }

    @Nullable
    public Server getServerById(Long serverId) {
        return serverRepository.findById(serverId).orElse(null);
    }

    @Nullable
    public Server getServerByIp(String serverIp) {
        return serverRepository.findByIp(serverIp).orElse(null);
    }

    @Nullable
    public String getRandomServerIp() {
        Server server = serverRepository.findRandomServer().orElse(null);
        return server == null ? null : server.getIp();
    }

    public void save(Server server){
        serverRepository.save(server);
    }

    public ResponseEntity<Response> delete(Long id){
        Server server = getServerById(id);

        if(server==null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Serwer nie istnieje");
        }

        if(!PermissionChecker.hasPermissionForServer(userService.getLoggedUser(), server, ServerUserRole.Role.OWNER)){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        serverRepository.delete(server);
        return Response.ok("Usunięto prawidłowo serwer");
    }

    public void saveBasicInformation(Server server, BasicServerDto basicServerDto, ServerStatusDto serverStatusDto){
        Name name = server.getName();
        if(name != null){
            name.setName(basicServerDto.ip());
            nameRepository.save(name);
        }
        else{
            server.setName(nameRepository.save(new Name(basicServerDto.ip(), "#ffffff")));
        }

        server.setIp(basicServerDto.ip().toLowerCase());
        server.setPort(basicServerDto.port());
        server.setOnline(serverStatusDto.online());
        server.setOnlinePlayers(serverStatusDto.players().online());
        server.setPremium(basicServerDto.premium());
        server.setMods(basicServerDto.mods());

        ServerDetail serverDetail = new ServerDetail();
        serverDetail.setMotdHtml(serverStatusDto.motd().html());
        serverDetail.setMotdClean(UnicodeConverter.convertUnicodeToAscii(serverStatusDto.motd().clean()));
        serverDetail.setIcon(serverStatusDto.icon());

        serverDetail = serverDetailRepository.save(serverDetail);
        server.setDetail(serverDetail);

        if(server.getVersions() == null){
            server.setVersions(basicServerDto.versions());
        }
        else{
            server.getVersions().clear();
            server.getVersions().addAll(basicServerDto.versions());
        }

        List<Mode> modes = basicServerDto.modes();
        if(modes != null && !modes.isEmpty()){
            if(modes.size()==1){
                server.setMode(modes.get(0));
            }
            else{
                server.setMode(modeRepository.getReferenceById(1L));
            }
        }
        else{
            server.setMode(null);
        }

        if(server.getSubServers() != null && server.getSubServers().size()>1){
            server.setMode(modeRepository.getReferenceById(1L));
        }
    }

    public ResponseEntity<RedirectResponse> checkServerInformation(BasicServerDto serverDto, Server server){
        String ip = serverDto.ip().toLowerCase();

        if(ip.contains("aternos.me")){
            return RedirectResponse.badRequest(1, "Nie możesz dodawać serwera aternos!", null);
        }

        //Checking if the server's ip is blocked
        BlockedServer blockedServer = blockedServerRepository.findByIp(ip).orElse(null);
        if(blockedServer!=null){
            //IP is blocked, because it is connected with another server
            if(blockedServer.getServer()!=null){
                return RedirectResponse.badRequest(2, "Ten adres IP jest powiązany z innym istniejącym serwerem.", "/server/"+ip);
            }
            //IP is blocked and user cannot add this server
            else {
                return RedirectResponse.badRequest(3, "Ten adres IP jest obecnie zablokowany! Jeśli uważasz to za błąd, skontaktuj się z nami.", null);
            }
        }

        //Checking if the server exists by ip
        if(serverRepository.findByIp(ip).isPresent()){
            //If the server is null - user tries to add a new server
            if(server == null){
                return RedirectResponse.badRequest(4, "Serwer znajduje się już na naszej liście.", "/server/"+ip);
            }

            //If the server is not null - user is trying to modify the server information
            if(!server.getIp().equalsIgnoreCase(ip)){
                return RedirectResponse.badRequest(5, "Serwer znajduje się już na liście.", "/server/"+ip);
            }
        }

        return RedirectResponse.ok(null, null);
    }
}
