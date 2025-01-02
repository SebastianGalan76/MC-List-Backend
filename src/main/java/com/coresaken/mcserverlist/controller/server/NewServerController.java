package com.coresaken.mcserverlist.controller.server;

import com.coresaken.mcserverlist.data.dto.BasicServerDto;
import com.coresaken.mcserverlist.data.response.RedirectResponse;
import com.coresaken.mcserverlist.service.NewServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NewServerController {
    final NewServerService newServerService;

    @PostMapping("/add-new-server")
    public ResponseEntity<RedirectResponse> addNewServer(@RequestBody BasicServerDto basicServerDto){
        return newServerService.addNewServer(basicServerDto);
    }
}
