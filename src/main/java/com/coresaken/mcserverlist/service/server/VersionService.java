package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.database.model.server.Mode;
import com.coresaken.mcserverlist.database.model.server.Version;
import com.coresaken.mcserverlist.database.repository.server.ModeRepository;
import com.coresaken.mcserverlist.database.repository.server.VersionRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
@RequiredArgsConstructor
public class VersionService {
    final VersionRepository versionRepository;

    List<Version> versionList;

    @PostConstruct
    public void initialize(){
        versionList = versionRepository.findAll();
    }
}
