package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.*;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.data.response.ServerStatus;
import com.coresaken.mcserverlist.database.model.server.*;
import com.coresaken.mcserverlist.database.model.server.staff.Player;
import com.coresaken.mcserverlist.database.model.server.staff.Rank;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import com.coresaken.mcserverlist.service.BannerFileService;
import com.coresaken.mcserverlist.service.NewServerService;
import com.coresaken.mcserverlist.service.ServerStatusService;
import com.coresaken.mcserverlist.util.LinkChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageServerService {
    final ServerRepository serverRepository;
    final NewServerService newServerService;
    final ServerStatusService serverStatusService;

    @Transactional
    public Response saveServerInfo(Server server, BasicServerDto serverDto) {
        Response response = newServerService.checkServerData(serverDto, server);
        if(response.getStatus() != HttpStatus.OK){
            return response;
        }

        ServerStatus serverStatus = serverStatusService.getServerStatus(serverDto.getIp(), serverDto.getPort());
        if(!serverStatus.isOnline()){
            return Response.builder().status(HttpStatus.NOT_FOUND).build();
        }

        newServerService.saveBasicInformation(server, serverDto, serverStatus);
        serverRepository.save(server);
        return Response.builder().status(HttpStatus.OK).message("Zmiany zostały zapisane prawidłowo").build();
    }

    public Response saveServerStaff(Server server, StaffDto staffDto){
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

        return Response.builder().status(HttpStatus.OK).build();
    }


    public Response saveServerDescription(Server server, StringDto stringDto) {
        server.setDescription(stringDto.getText());
        serverRepository.save(server);

        return Response.builder().status(HttpStatus.OK).build();
    }

    public Response saveServerLinks(Server server, List<Link> links) {
        server.getLinks().clear();
        server.getLinks().addAll(links);
        serverRepository.save(server);
        return Response.builder().status(HttpStatus.OK).build();
    }

    public Response saveServerBanner(Server server, MultipartFile file, String url) {
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
                Response uploadResponse = BannerFileService.upload(file, server);
                if(uploadResponse.getStatus() != HttpStatus.OK){
                    return uploadResponse;
                }
            }
            else{
                server.setBanner(null);
            }
        }

        serverRepository.save(server);
        return Response.builder().status(HttpStatus.OK).message("Ustawiłeś prawidłowo swój banner").build();
    }
}
