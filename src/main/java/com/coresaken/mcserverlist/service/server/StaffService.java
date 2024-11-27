package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.database.model.server.staff.Rank;
import com.coresaken.mcserverlist.database.repository.server.RankRepository;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.util.PermissionChecker;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class StaffService {
    final UserService userService;
    final ServerService serverService;

    final RankRepository rankRepository;

    final EntityManager entityManager;

    @Transactional
    public ResponseEntity<Response> saveAllStaff(Long serverId, List<Rank> staff) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        assert server != null;
        Map<Long, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < staff.size(); i++) {
            indexMap.put(staff.get(i).getId(), i);
        }

        server.getStaff().forEach(rank ->
                rank.setIndex(indexMap.getOrDefault(rank.getId(), -1))
        );

        return Response.ok("Zmiany zostały zapisane.");
    }

    @Transactional
    public ResponseEntity<ObjectResponse<Rank>> createRank(Long serverId, Rank rank) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return ObjectResponse.convertFromResponse(permissionResponse);
        }

        assert server != null;
        rank.setServer(server);

        rank.getPlayers().forEach(player -> player.setRank(rank));
        Rank savedRank = rankRepository.save(rank);
        server.getStaff().add(savedRank);
        return ObjectResponse.ok("Ranga została prawidłowo stworzona.", savedRank);
    }

    @Transactional
    public ResponseEntity<Response> editRank(Long serverId, Rank rank) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        assert server != null;

        Rank savedRank = server.getStaff().stream().filter(r -> r.getId().equals(rank.getId())).findFirst().orElse(null);
        if(savedRank == null){
            return Response.badRequest(3, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        savedRank.setName(rank.getName());
        savedRank.setColor(rank.getColor());

        savedRank.getPlayers().clear();
        AtomicInteger index = new AtomicInteger(0);
        rank.getPlayers().forEach(player -> {
            player.setRank(savedRank);
            player.setIndex(index.getAndIncrement());
            savedRank.getPlayers().add(player);
        });

        savedRank.setIndex(rank.getIndex());

        serverService.save(server);
        return Response.ok("Zmiany zostały zapisane");
    }

    public ResponseEntity<Response> deleteRank(Long serverId, Rank rank) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        assert server != null;
        if(!server.getStaff().contains(rank)){
            return Response.badRequest(3, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }

        server.getStaff().remove(rank);
        serverService.save(server);
        return Response.ok("Ranga została prawidłowo usunięta!");
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
}
