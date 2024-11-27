package com.coresaken.mcserverlist.data.dto;

import com.coresaken.mcserverlist.database.model.server.*;
import com.coresaken.mcserverlist.database.model.server.Version;
import com.coresaken.mcserverlist.database.model.server.ratings.RatingCategory;
import com.coresaken.mcserverlist.database.model.server.staff.Rank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ServerDto {
    Long id;
    String ip;
    int port;
    ServerDetail detail;
    Name name;
    String description;
    String banner;

    boolean premium = false;
    boolean mods = false;

    Mode mode;

    boolean online;
    int onlinePlayers;

    LocalDateTime nextRefreshAt;
    LocalDateTime createdAt;

    int promotionPoints;
    List<SubServer> subServers = new ArrayList<>();

    List<Rank> staff = new ArrayList<>();

    List<Version> versions = new ArrayList<>();
    List<Vote> votes = new ArrayList<>();

    List<Link> links = new ArrayList<>();

    List<HourlyPlayerCount> hourlyPlayerCounts;
    List<DailyPlayerCount> dailyPlayerCounts;

    List<ServerRoleDto> roles;
    ServerUserRole.Role role;

    List<PlayerRatings> ratings = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class PlayerRatings{
        Long id;
        RatingCategory category;
        Long userId;
        int rating;
    }
}
