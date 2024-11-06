package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ratings.PlayerRating;
import com.coresaken.mcserverlist.service.server.RateServerService;
import com.coresaken.mcserverlist.service.server.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RateServerController {
    final RateServerService rateServerService;
    final ServerService serverService;

    @PostMapping("/server/{ip}/rate/save")
    public ResponseEntity<Response> getServerRatePage(@PathVariable("ip") String ip, @RequestBody List<PlayerRating> playerRatings){
        Server server = serverService.getServerByIp(ip);

        if(server==null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Serwer o podanym IP nie istnieje");
        }

        return rateServerService.rateServer(server, playerRatings);
    }
}
