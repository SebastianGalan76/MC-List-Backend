package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.SearchServerDto;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import com.coresaken.mcserverlist.database.repository.SubServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServerService {
    final ServerRepository serverRepository;
    final SubServerRepository subServerRepository;

    public Page<Server> searchServer(SearchServerDto searchServerDto, Pageable pageable){
        //Finding servers by name, ip or motd
        Set<Server> serversByName = new HashSet<>();
        if(searchServerDto.name() != null && !searchServerDto.name().isEmpty()){
            serversByName.addAll(serverRepository.searchByIp(searchServerDto.name()));
            serversByName.addAll(serverRepository.searchByMotd(searchServerDto.name()));
        }
        else{
            serversByName.addAll(serverRepository.findAll());
        }

        //Finding servers and subServers by mode and version
        Long versionId = searchServerDto.version() != null ? searchServerDto.version().getId() : 0;
        Set<Server> serversByModeAndVersions = new HashSet<>(serverRepository.findServersByModeAndVersionRange(searchServerDto.mode(), versionId));
        serversByModeAndVersions.addAll(subServerRepository.findServersByModeAndVersion(searchServerDto.mode(), versionId));

        Set<Server> commonServers = new HashSet<>(serversByName);
        if (searchServerDto.mode() != null || searchServerDto.version() != null) {
            commonServers.retainAll(serversByModeAndVersions);
        }
        if (searchServerDto.premium()) {
            commonServers.retainAll(serverRepository.findAllPremiumServers());
        }
        if(searchServerDto.mods()){
            commonServers.retainAll(serverRepository.findAllServersWithMods());
        }

        List<Server> resultList = new ArrayList<>(commonServers);
        resultList = resultList.stream()
                .sorted(Comparator
                        .comparingInt(Server::getPromotionPoints)
                        .thenComparingInt(s -> s.getVotes().size()).reversed())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), resultList.size());

        return new PageImpl<>(resultList.subList(start, end), pageable, resultList.size());
    }
}
