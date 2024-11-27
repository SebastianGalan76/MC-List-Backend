package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.SubServerDto;
import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.*;
import com.coresaken.mcserverlist.database.repository.NameRepository;
import com.coresaken.mcserverlist.database.repository.SubServerRepository;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.util.PermissionChecker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubServerService {
    final ServerService serverService;
    final UserService userService;
    final ModeService modeService;

    final SubServerRepository subServerRepository;
    final NameRepository nameRepository;

    @Transactional
    public ResponseEntity<Response> saveAllSubServers(Long serverId, List<SubServer> subServers) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        assert server != null;
        Map<Long, Integer> indexMap = new HashMap<>();
        for(int i=0;i<subServers.size();i++){
            indexMap.put(subServers.get(i).getId(), i);
        }

        server.getSubServers().forEach(subServer ->
            subServer.setIndex(indexMap.getOrDefault(subServer.getId(), -1))
        );
        return Response.ok("Zmiany zostały zapisane.");
    }

    public ResponseEntity<ObjectResponse<SubServer>> createSubServer(Long serverId, SubServerDto subServerDto) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return ObjectResponse.convertFromResponse(permissionResponse);
        }

        ResponseEntity<Response> validateSubServerResponse = validateSubServer(subServerDto);
        if(validateSubServerResponse.getStatusCode() != HttpStatus.OK){
            return ObjectResponse.convertFromResponse(validateSubServerResponse);
        }

        SubServer subServer = new SubServer();
        subServer.setName(nameRepository.save(new Name(subServerDto.getName(), subServerDto.getColor())));
        subServer.setMode(subServerDto.getMode());
        subServer.setVersions(subServerDto.getVersions());
        subServer.setIndex(subServerDto.getIndex());
        subServer.setServer(server);

        assert server != null;
        server.getSubServers().add(subServer);
        serverService.save(server);

        return ObjectResponse.ok("Tryb został prawidłowo stworzony", subServer);
    }

    @Transactional
    public ResponseEntity<Response> editSubServer(Long serverId, SubServerDto subServerDto) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        ResponseEntity<Response> validateLinkResponse = validateSubServer(subServerDto);
        if(validateLinkResponse.getStatusCode() != HttpStatus.OK){
            return validateLinkResponse;
        }

        try{
            SubServer savedSubServer = subServerRepository.getReferenceById(subServerDto.getId());
            Name name = savedSubServer.getName();
            name.setName(subServerDto.getName());
            name.setColor(subServerDto.getColor());

            savedSubServer.setMode(subServerDto.getMode());
            savedSubServer.setVersions(subServerDto.getVersions());

            subServerRepository.save(savedSubServer);
        }catch (EntityNotFoundException e){
            return Response.badRequest(3, "Tryb nie istnieje. Prawdopodobnie został usunięty!");
        }

        return Response.ok("Tryb został prawidłowo edytowany.");
    }

    public ResponseEntity<Response> deleteSubServer(Long serverId, SubServer subServer) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        assert server != null;
        if(!server.getSubServers().contains(subServer)){
            return Response.badRequest(3, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        server.getSubServers().remove(subServer);
        serverService.save(server);
        return Response.ok("Link został prawidłowo usunięty.");
    }

    private ResponseEntity<Response> checkPermission(Server server){
        if(server==null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Serwer o podanym ID nie istnieje");
        }
        if(!PermissionChecker.hasPermissionForServer(userService.getLoggedUser(), server, ServerUserRole.Role.MODERATOR)){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        return Response.ok("Sukces");
    }

    private ResponseEntity<Response> validateSubServer(SubServerDto subServer){
        subServer.setName(subServer.getName().trim());
        if(subServer.getName().isEmpty()){
            return Response.badRequest(4, "Wprowadź nazwę trybu.");
        }
        if(subServer.getMode() == null){
            return Response.badRequest(5, "Wybierz tryb serwera.");
        }
        if(modeService.getModeById(subServer.getMode().getId())==null){
            return Response.badRequest(6, "Wybierz tryb serwera.");
        }

        return Response.ok("Sukces");
    }
}
