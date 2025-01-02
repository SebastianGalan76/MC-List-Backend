package com.coresaken.mcserverlist.controller.server;

import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Link;
import com.coresaken.mcserverlist.service.server.ServerLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ServerLinkController {
    final ServerLinkService serverLinkService;

    @PostMapping("/server/{id}/manage/link/all")
    public ResponseEntity<Response> saveAllLinks(@PathVariable("id") Long serverId, @RequestBody List<Link> links){
        return serverLinkService.saveAllLinks(serverId, links);
    }

    @PostMapping("/server/{id}/manage/link")
    public ResponseEntity<ObjectResponse<Link>> createLink(@PathVariable("id") Long serverId, @RequestBody Link link){
        return serverLinkService.createLink(serverId, link);
    }

    @PutMapping("/server/{id}/manage/link")
    public ResponseEntity<Response> editLink(@PathVariable("id") Long serverId, @RequestBody Link link){
        return serverLinkService.editLink(serverId, link);
    }

    @PostMapping("/server/{id}/manage/link/delete")
    public ResponseEntity<Response> deleteLink(@PathVariable("id") Long serverId, @RequestBody Link link){
        return serverLinkService.deleteLink(serverId, link);
    }
}
