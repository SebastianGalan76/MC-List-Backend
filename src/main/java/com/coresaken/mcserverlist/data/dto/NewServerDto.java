package com.coresaken.mcserverlist.data.dto;

import com.coresaken.mcserverlist.database.model.server.Mode;
import com.coresaken.mcserverlist.database.model.server.Version;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NewServerDto {
    String ip;
    int port;

    List<Mode> modes;
    List<Version> versions;
}
