package com.coresaken.mcserverlist.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerStatus {
    boolean online;
    Player players;
    Motd motd;
    String icon;

    @Data
    public static class Player{
        int online;
        int max;
    }

    @Data
    public static class Motd{
        String clean;
        String html;
    }
}
