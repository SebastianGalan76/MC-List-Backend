package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TakeOverService {
    final UserService userService;
    final ServerService serverService;
    final ServerStatusService serverStatusService;

    public ResponseEntity<Response> takeOver(Long serverId){
        User user = userService.getLoggedUser();
        if(user == null){
            return Response.badRequest(1, "Twoja sesja wygasła. Zaloguj się ponownie");
        }

        Server server = serverService.getServerById(serverId);
        if(server == null){
            return Response.badRequest(2, "Serwer o podanym ID nie istnieje.");
        }
        for (ServerUserRole sur:server.getServerUserRoles()){
            if(sur.getRole() == ServerUserRole.Role.OWNER){
                return Response.badRequest(3, "Serwer posiada już właściciela. Jeśli ktoś przejął Twój serwer, skontaktuj się z nami");
            }
        }

        String motd = serverStatusService.getServerStatus(server.getIp(), server.getPort()).motd().html();

        if(motd.contains(user.getUuid())){
            Optional<ServerUserRole> optionalSUR = server.getServerUserRoles().stream().filter(sur -> sur.getUser().equals(user)).findFirst();

            if(optionalSUR.isPresent()){
                ServerUserRole savedSUR = optionalSUR.get();
                savedSUR.setRole(ServerUserRole.Role.OWNER);
            }
            else{
                ServerUserRole sur = new ServerUserRole();
                sur.setRole(ServerUserRole.Role.OWNER);
                sur.setUser(user);
                sur.setServer(server);

                server.getServerUserRoles().add(sur);
            }

            serverService.save(server);
            return Response.ok("Serwer został prawidłowo przejęty");
        }

        return Response.badRequest(4, motd);
    }

}
