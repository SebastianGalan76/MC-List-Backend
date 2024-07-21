package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.database.model.server.Mode;
import com.coresaken.mcserverlist.database.repository.server.ModeRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
@RequiredArgsConstructor
public class ModeService {
    final ModeRepository modeRepository;

    List<Mode> modeList;

    @PostConstruct
    public void initialize(){
        modeList = modeRepository.findAll();
    }
}
