package com.coresaken.mcserverlist.data.mapper;

import com.coresaken.mcserverlist.data.dto.ServerListDto;
import com.coresaken.mcserverlist.database.model.server.Server;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerListMapper {
    public static ServerListDto toDTO(Server server){
        if(server == null){
            return null;
        }

        ServerListDto dto = new ServerListDto();
        dto.setId(server.getId());
        dto.setIp(server.getIp());
        dto.setPort(server.getPort());

        dto.setDetail(server.getDetail());
        dto.setName(server.getName());

        dto.setOnline(server.isOnline());
        dto.setPremium(server.isPremium());
        dto.setMods(server.isMods());

        dto.setPlayers(server.getOnlinePlayers());
        dto.setPromotionPoints(server.getPromotionPoints());
        dto.setVotes(server.getVotes().size());

        dto.setMode(server.getMode());
        dto.setVersions(server.getVersions());

        return dto;
    }
}
