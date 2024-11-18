package com.coresaken.mcserverlist.data.mapper;

import com.coresaken.mcserverlist.data.dto.ServerDto;
import com.coresaken.mcserverlist.data.dto.ServerRoleDto;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;

public class ServerMapper {
    public static ServerDto toDTO(Server server, User user){
        if(server == null){
            return null;
        }

        ServerDto dto = new ServerDto();
        dto.setId(server.getId());
        dto.setIp(server.getIp());
        dto.setPort(server.getPort());
        dto.setDetail(server.getDetail());
        dto.setName(server.getName());
        dto.setMode(server.getMode());
        dto.setDescription(server.getDescription());
        dto.setBanner(server.getBanner());
        dto.setPremium(server.isPremium());
        dto.setMods(server.isMods());
        dto.setOnline(server.isOnline());
        dto.setOnlinePlayers(server.getOnlinePlayers());
        dto.setPromotionPoints(server.getPromotionPoints());
        dto.setVersions(server.getVersions());
        dto.setLinks(server.getLinks());
        dto.setSubServers(server.getSubServers());

        ServerUserRole serverUserRole = server.getServerUserRoles().stream().filter(role -> role.getUser().equals(user)).findFirst().orElse(null);
        if((serverUserRole != null && serverUserRole.getRole().value >= ServerUserRole.Role.ADMINISTRATOR.value) || (user != null && user.getRole() == User.Role.ADMIN)){
            dto.setRoles(server.getServerUserRoles().stream()
                    .map(role -> new ServerRoleDto(role.getUser().getEmail(), role.getRole()))
                    .toList());
            dto.setRole(serverUserRole == null ? ServerUserRole.Role.OWNER : serverUserRole.getRole());
        }

        return dto;
    }
}
