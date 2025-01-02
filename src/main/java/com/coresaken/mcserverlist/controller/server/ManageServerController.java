package com.coresaken.mcserverlist.controller.server;

import com.coresaken.mcserverlist.data.dto.*;
import com.coresaken.mcserverlist.data.response.RedirectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.service.server.ManageServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ManageServerController {
    final ManageServerService manageServerService;

    @PostMapping("/server/{id}/manage/info")
    public ResponseEntity<RedirectResponse> saveServerInfo(@PathVariable("id") Long serverId, @RequestBody BasicServerDto serverDto){
        return manageServerService.saveServerInfo(serverId, serverDto);
    }

    @PostMapping("/server/{id}/manage/description")
    public ResponseEntity<Response> saveServerDescription(@PathVariable("id") Long serverId, @RequestBody String description){
        return manageServerService.saveServerDescription(serverId, description);
    }

    @PostMapping("/server/{id}/manage/banner")
    public ResponseEntity<Response> saveServerBanner(@PathVariable("id") Long serverId, @Param("file") MultipartFile file, @Param("url") String url){
        return manageServerService.saveServerBanner(serverId, file, url);
    }
}
