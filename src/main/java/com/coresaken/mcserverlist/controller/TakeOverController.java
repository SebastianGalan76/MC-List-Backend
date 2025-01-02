package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.service.server.ServerService;
import com.coresaken.mcserverlist.service.server.TakeOverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TakeOverController {
    final TakeOverService takeOverService;
    final ServerService serverService;
    final UserService userService;

    @GetMapping("/take-over/{id}")
    public ResponseEntity<Response> takeOverServer(@PathVariable("id") Long id){
        return takeOverService.takeOver(id);
    }
}
