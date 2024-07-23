package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServerService {
    final ServerRepository serverRepository;

    public Page<Server> getServers(int page){
        Pageable pageable = PageRequest.of(page - 1, 30);
        return serverRepository.findAll(pageable);
    }

    @Nullable
    public Server getServerById(Long serverId) {
        return serverRepository.findById(serverId).orElse(null);
    }

    @Nullable
    public Server getServerByIp(String serverIp) {
        return serverRepository.findByIp(serverIp).orElse(null);
    }
}
