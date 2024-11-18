package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.ServerRoleDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.database.repository.NameRepository;
import com.coresaken.mcserverlist.database.repository.SubServerRepository;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.util.PermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServerRoleService {
    final ServerService serverService;
    final UserService userService;
    final ModeService modeService;

    final SubServerRepository subServerRepository;
    final NameRepository nameRepository;
    final UserRepository userRepository;

    @Transactional
    public ResponseEntity<Response> createRole(Long serverId, ServerRoleDto serverRoleDto) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        User user = userRepository.findByEmail(serverRoleDto.email()).orElse(null);
        if(user == null){
            return Response.badRequest(3, "Brak użytkownika z podanym adresem e-mail");
        }

        assert server != null;
        ServerUserRole serverUserRole = server.getServerUserRoles().stream().filter(role -> role.getUser().getEmail().equals(serverRoleDto.email())).findFirst().orElse(null);
        if(serverUserRole == null){
            serverUserRole = new ServerUserRole();
            serverUserRole.setUser(user);
            serverUserRole.setServer(server);
            serverUserRole.setRole(serverRoleDto.role());

            server.getServerUserRoles().add(serverUserRole);
        }
        else{
            if(serverUserRole.getRole() == ServerUserRole.Role.OWNER && user.getRole() != User.Role.ADMIN){
                return Response.badRequest(4, "Nie możesz zmienić roli właściciela.");
            }
            serverUserRole.setRole(serverRoleDto.role());
        }

        serverService.save(server);

        return Response.ok("Rola została prawidłowo stworzona.");
    }

    public ResponseEntity<Response> deleteRole(Long serverId, ServerRoleDto serverRoleDto) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        assert server != null;
        ServerUserRole serverUserRole = server.getServerUserRoles().stream().filter(role -> role.getUser().getEmail().equals(serverRoleDto.email())).findFirst().orElse(null);

        if(serverUserRole == null){
            return Response.badRequest(3, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        User user = userService.getLoggedUser();
        if(serverUserRole.getRole() == ServerUserRole.Role.OWNER && serverUserRole.getUser().equals(user) && user.getRole() != User.Role.ADMIN){
            return Response.badRequest(4, "Nie możesz usunąć swojej roli jako Właściciela!");
        }

        server.getServerUserRoles().remove(serverUserRole);
        serverService.save(server);
        return Response.ok("Rola została prawidłowo usunięta.");
    }

    private ResponseEntity<Response> checkPermission(Server server){
        if(server==null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Serwer o podanym ID nie istnieje");
        }
        if(!PermissionChecker.hasPermissionForServer(userService.getLoggedUser(), server, ServerUserRole.Role.ADMINISTRATOR)){
            return Response.badRequest(2, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        return Response.ok("Sukces");
    }
}
