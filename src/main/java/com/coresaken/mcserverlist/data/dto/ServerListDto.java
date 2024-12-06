package com.coresaken.mcserverlist.data.dto;

import com.coresaken.mcserverlist.database.model.server.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ServerListDto {
    Long id;
    String ip;
    int port;

    ServerDetail detail;
    Name name;

    boolean online;
    boolean premium;
    boolean mods;

    int players;
    int promotionPoints;
    int votes;

    Mode mode;
    List<Version> versions = new ArrayList<>();
}
