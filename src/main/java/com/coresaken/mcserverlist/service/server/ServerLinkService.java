package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Link;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.database.repository.server.LinkRepository;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.util.LinkChecker;
import com.coresaken.mcserverlist.util.PermissionChecker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ServerLinkService {
    final ServerService serverService;
    final UserService userService;

    final LinkRepository linkRepository;

    public ResponseEntity<Response> saveAllLinks(Long serverId, List<Link> links) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        assert server != null;

        AtomicInteger index = new AtomicInteger(0);
        List<Link> validatedLink = links.stream()
                .filter(link -> validateLink(link).getStatusCode() == HttpStatus.OK && server.getLinks().contains(link))
                .peek(link -> {
                    link.setIndex(index.getAndIncrement());
                    link.setServer(server);
                })
                .toList();

        server.getLinks().clear();
        server.getLinks().addAll(validatedLink);
        serverService.save(server);
        return Response.ok("Zmiany zostały zapisane.");
    }

    @Transactional
    public ResponseEntity<ObjectResponse<Link>> createLink(Long serverId, Link link) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return ObjectResponse.convertFromResponse(permissionResponse);
        }

        ResponseEntity<Response> validateLinkResponse = validateLink(link);
        if(validateLinkResponse.getStatusCode() != HttpStatus.OK){
            return ObjectResponse.convertFromResponse(validateLinkResponse);
        }

        assert server != null;
        link.setServer(server);
        Link savedLink = linkRepository.save(link);
        server.getLinks().add(savedLink);
        return ObjectResponse.ok("Link został prawidłowo stworzony.", savedLink);
    }

    public ResponseEntity<Response> editLink(Long serverId, Link link) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        ResponseEntity<Response> validateLinkResponse = validateLink(link);
        if(validateLinkResponse.getStatusCode() != HttpStatus.OK){
            return validateLinkResponse;
        }

        try{
            Link savedLink = linkRepository.getReferenceById(link.getId());
            savedLink.setName(link.getName());
            savedLink.setUrl(link.getUrl());

            linkRepository.save(savedLink);
        }catch (EntityNotFoundException e){
            return Response.badRequest(3, "Link nie istnieje. Prawdopodobnie został usunięty!");
        }

        return Response.ok("Link został prawidłowo edytowany.");
    }

    @Transactional
    public ResponseEntity<Response> deleteLink(Long serverId, Link link) {
        Server server = serverService.getServerById(serverId);

        ResponseEntity<Response> permissionResponse = checkPermission(server);
        if(permissionResponse.getStatusCode() != HttpStatus.OK){
            return permissionResponse;
        }

        assert server != null;
        if(!server.getLinks().contains(link)){
            return Response.badRequest(3, "Nie posiadasz wymaganych uprawnień, aby to zrobić!");
        }
        server.getLinks().remove(link);
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

    private ResponseEntity<Response> validateLink(Link link){
        link.setName(link.getName().trim());
        if(link.getName().isEmpty()){
            return Response.badRequest(4, "Wprowadź nazwę linku.");
        }
        if(!LinkChecker.isLink(link.getUrl())){
            return Response.badRequest(5, "Wprowadź poprawny adres URL.");
        }

        return Response.ok("Sukces");
    }
}
