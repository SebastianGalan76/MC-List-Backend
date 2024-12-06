package com.coresaken.mcserverlist.data.mapper;

import com.coresaken.mcserverlist.data.dto.ServerDto;
import com.coresaken.mcserverlist.data.dto.ServerRoleDto;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.database.model.server.ratings.PlayerRating;
import com.coresaken.mcserverlist.database.model.server.ratings.RatingCategory;
import com.coresaken.mcserverlist.database.repository.PlayerRatingRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ServerMapper {

    public static ServerDto toDTO(Server server, User user, List<PlayerRating> ratings){
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
        dto.setStaff(server.getStaff());
        dto.setRatings(ratings.stream().map(r -> new ServerDto.PlayerRatings(r.getId(), r.getCategory(), r.getUser().getId(), r.getRate())).toList());

        ServerUserRole serverUserRole = server.getServerUserRoles().stream().filter(role -> role.getUser().equals(user)).findFirst().orElse(null);
        if((serverUserRole != null && serverUserRole.getRole().value >= ServerUserRole.Role.ADMINISTRATOR.value) || (user != null && user.getRole() == User.Role.ADMIN)){
            dto.setRoles(server.getServerUserRoles().stream()
                    .map(role -> new ServerRoleDto(role.getUser().getEmail(), role.getRole()))
                    .toList());
            dto.setRole(serverUserRole == null || user.getRole() == User.Role.ADMIN ? ServerUserRole.Role.OWNER : serverUserRole.getRole());
        }

        dto.setDailyPlayerCounts(server.getDailyPlayerCounts());
        dto.setHourlyPlayerCounts(server.getHourlyPlayerCounts());

        return dto;
    }
}
