package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.StaffDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.staff.Player;
import com.coresaken.mcserverlist.database.model.server.staff.Rank;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageServerService {
    final ServerRepository serverRepository;

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

    public Server getServerById(Long id){
        return serverRepository.findById(id).orElse(null);
    }
}
