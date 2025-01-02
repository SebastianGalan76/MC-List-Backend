package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.RateServerDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.service.server.RateServerService;
import com.coresaken.mcserverlist.service.server.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RateServerController {
    final RateServerService rateServerService;
    final ServerService serverService;

    @PostMapping("/server/{id}/rate")
    public ResponseEntity<Response> getServerRatePage(@PathVariable("id") Long serverId, @RequestBody List<RateServerDto> playerRatings){
        return rateServerService.rateServer(serverId, playerRatings);
    }
}
