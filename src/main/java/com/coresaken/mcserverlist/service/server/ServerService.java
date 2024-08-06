package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.SearchServerDto;
import com.coresaken.mcserverlist.data.dto.ServerStatusDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.database.model.server.ratings.PlayerRating;
import com.coresaken.mcserverlist.database.repository.PlayerRatingRepository;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import com.coresaken.mcserverlist.service.ServerStatusService;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.util.PermissionChecker;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerService {
    final UserService userService;
    final ServerStatusService serverStatusService;

    final ServerRepository serverRepository;
    final PlayerRatingRepository playerRatingRepository;

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

    public Response rateServer(Server server, List<PlayerRating> playerRatings){
        User user = userService.getLoggedUser();

        if(user==null){
            return Response.builder().status(HttpStatus.UNAUTHORIZED).message("Twoja sesja wygasła. Zaloguj się ponownie, aby ocenić serwer!").build();
        }
        if(server==null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił nieoczekiwany błąd. Kod błędu #3251. Zgłoś go do Administratora strony!").build();
        }

        //Remove ratings if all ratings are equal to 1
        boolean antiKid = false;
        for(PlayerRating pr:playerRatings){
            if (pr.getRate() > 1) {
                antiKid = true;
                break;
            }
        }
        if(!antiKid){
            return Response.builder().status(HttpStatus.OK).build();
        }

        for(PlayerRating pr:playerRatings){
            if(pr.getRate() == 0){
                continue;
            }

            Optional<PlayerRating> existingRating = playerRatingRepository.findByUserAndServerAndCategory(user, server, pr.getCategory());
            if (existingRating.isPresent()) {
                PlayerRating playerRating = existingRating.get();
                playerRating.setRate(pr.getRate());
                playerRatingRepository.save(playerRating);
            } else {
                pr.setUser(user);
                pr.setServer(server);
                playerRatingRepository.save(pr);
            }
        }

        return Response.builder().status(HttpStatus.OK).build();
    }

    public Response deleteServer(Server server){
        if(!PermissionChecker.hasPermissionForServer(userService.getLoggedUser(), server, ServerUserRole.Role.OWNER)){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Nie posiadasz wymaganych uprawnień, aby to zrobić!").build();
        }

        serverRepository.delete(server);
        return Response.builder().status(HttpStatus.OK).build();
    }

    public Page<Server> searchServer(SearchServerDto searchServerDto, Pageable pageable){
        Set<Server> serversByName = new HashSet<>();
        if(searchServerDto.getName() != null && !searchServerDto.getName().isEmpty()){
            serversByName.addAll(serverRepository.searchByIp(searchServerDto.getName()));
            serversByName.addAll(serverRepository.searchByMotd(searchServerDto.getName()));
        }
        else{
            serversByName.addAll(serverRepository.findAll());
        }

        Long versionId = searchServerDto.getVersion() != null ? searchServerDto.getVersion().getId() : 0;
        Set<Server> serversByModeAndVersions = new HashSet<>(serverRepository.findServersByModeAndVersionRange(searchServerDto.getMode(), versionId));

        Set<Server> commonServers = new HashSet<>(serversByName);
        if (searchServerDto.getMode() != null || searchServerDto.getVersion() != null) {
            commonServers.retainAll(serversByModeAndVersions);
        }
        if (searchServerDto.isPremium()) {
            commonServers.retainAll(serverRepository.findAllPremiumServers());
        }
        if(searchServerDto.isMods()){
            commonServers.retainAll(serverRepository.findAllServersWithMods());
        }

        List<Server> resultList = new ArrayList<>(commonServers);
        resultList = resultList.stream()
                .sorted(Comparator
                        .comparingInt(Server::getPromotionPoints)
                        .thenComparingInt(s -> s.getVotes().size()).reversed())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), resultList.size());

        return new PageImpl<>(resultList.subList(start, end), pageable, resultList.size());
    }

    public Response takeOver(Long serverId){
        User user = userService.getLoggedUser();
        if(user == null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Twoja sesja wygasła. Zaloguj się ponownie").build();
        }

        Server server = serverRepository.findById(serverId).orElse(null);
        if(server == null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił nieoczekiwany błąd #9928").build();
        }
        for (ServerUserRole sur:server.getServerUserRoles()){
            if(sur.getRole()== ServerUserRole.Role.OWNER){
                return Response.builder().status(HttpStatus.BAD_REQUEST).message("Serwer posiada już właściciela. Jeśli ktoś przejął Twój serwer, skontaktuj się z nami").build();
            }
        }

        String motd = serverStatusService.getServerStatus(server.getIp(), server.getPort()).motd().clean();

        if(motd.contains(user.getUuid())){
            ServerUserRole sur = new ServerUserRole();
            sur.setRole(ServerUserRole.Role.OWNER);
            sur.setUser(user);
            sur.setServer(server);

            server.getServerUserRoles().add(sur);
            serverRepository.save(server);
            return Response.builder().status(HttpStatus.OK).message("Serwer został prawidłowo przejęty").build();
        }

        return Response.builder().status(HttpStatus.BAD_REQUEST).message("Błędna weryfikacja. Jesteś pewien, że zresetowałeś serwer po zmianie MOTD serwera? Serwer musi być włączony. Obecny motd serwera: "+motd).build();
    }
}
