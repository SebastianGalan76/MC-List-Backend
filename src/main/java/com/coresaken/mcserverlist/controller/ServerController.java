package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.ServerDto;
import com.coresaken.mcserverlist.data.dto.ServerListDto;
import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.service.server.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ServerController {
    final ServerService serverService;

    @GetMapping("/server/{ip}")
    public ResponseEntity<ServerDto> getServer(@PathVariable("ip") String ip){
        return new ResponseEntity<>(serverService.getServer(ip), HttpStatus.OK);
    }

    @GetMapping("/server/list/{page}")
    public Page<ServerListDto> getServers(@PathVariable("page") int page){
        return serverService.getServers(page);
    }

    @DeleteMapping("/server/{id}")
    public ResponseEntity<Response> deleteServer(@PathVariable("id") Long id){
        return serverService.delete(id);
    }

    @GetMapping("/random")
    public ResponseEntity<ObjectResponse<String>> getRandomServerIp(){
        return serverService.getRandomServerIp();
    }
}
