package com.coresaken.mcserverlist.data.dto;

import com.coresaken.mcserverlist.database.model.server.Mode;
import com.coresaken.mcserverlist.database.model.server.Version;

import java.util.List;

public record BasicServerDto(String ip, int port, List<Mode> modes, List<Version> versions, boolean premium, boolean mods) {
}
