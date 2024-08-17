package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.SearchServerDto;
import com.coresaken.mcserverlist.data.payment.PromotionPoints;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Report;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.database.model.server.ratings.PlayerRating;
import com.coresaken.mcserverlist.database.repository.PlayerRatingRepository;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import com.coresaken.mcserverlist.database.repository.SubServerRepository;
import com.coresaken.mcserverlist.database.repository.server.ReportRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerService {
    final UserService userService;
    final ServerStatusService serverStatusService;

    final ServerRepository serverRepository;
    final SubServerRepository subServerRepository;
    final ReportRepository reportRepository;

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
        serversByModeAndVersions.addAll(subServerRepository.findServersByModeAndVersion(searchServerDto.getMode(), versionId));

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

    @Transactional
    public void addPromotionPoints(PromotionPoints promotionPoints) {
        Server server = serverRepository.findById(promotionPoints.getServerId()).orElse(null);
        if(server == null){
            return;
        }

        server.setPromotionPoints(server.getPromotionPoints() + promotionPoints.getPromotionPoints());
    }

    public Response reportServer(Long id, String reason) {
        Server server = getServerById(id);

        if(server==null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił nieoczekiwany błąd #323245").build();
        }

        User user = userService.getLoggedUser();
        if(user == null){
            return Response.builder().status(HttpStatus.UNAUTHORIZED).message("Twoja sesja wygasła. Musisz się zalogować ponownie").build();
        }

        Report report = new Report();
        report.setReason(reason);
        report.setUser(user);
        report.setServer(server);
        reportRepository.save(report);
        return Response.builder().status(HttpStatus.OK).message("Zgłoszenie zostało wysłane do administracji").build();
    }

    public String getRandomServerIp() {
        Server server = serverRepository.findRandomServer().orElse(null);
        return server == null ? null : server.getIp();
    }

    public void save(Server server){
        serverRepository.save(server);
    }
}
