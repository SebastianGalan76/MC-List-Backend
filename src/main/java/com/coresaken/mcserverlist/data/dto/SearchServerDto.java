package com.coresaken.mcserverlist.data.dto;

import com.coresaken.mcserverlist.database.model.server.Mode;
import com.coresaken.mcserverlist.database.model.server.Version;

public record SearchServerDto(String name, Mode mode, Version version, boolean premium, boolean mods) {
}
