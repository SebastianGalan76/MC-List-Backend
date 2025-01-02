package com.coresaken.mcserverlist.controller.server;

import com.coresaken.mcserverlist.data.dto.SubServerDto;
import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.SubServer;
import com.coresaken.mcserverlist.service.server.SubServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubServerController {
    final SubServerService subServerService;

    @PostMapping("/server/{id}/manage/subserver/all")
    public ResponseEntity<Response> saveAllSubServers(@PathVariable("id") Long serverId, @RequestBody List<SubServer> subServers){
        return subServerService.saveAllSubServers(serverId, subServers);
    }

    @PostMapping("/server/{id}/manage/subserver")
    public ResponseEntity<ObjectResponse<SubServer>> createSubServer(@PathVariable("id") Long serverId, @RequestBody SubServerDto subServer){
        return subServerService.createSubServer(serverId, subServer);
    }

    @PutMapping("/server/{id}/manage/subserver")
    public ResponseEntity<Response> editSubServer(@PathVariable("id") Long serverId, @RequestBody SubServerDto subServer){
        return subServerService.editSubServer(serverId, subServer);
    }

    @PostMapping("/server/{id}/manage/subserver/delete")
    public ResponseEntity<Response> deleteSubServer(@PathVariable("id") Long serverId, @RequestBody SubServer subServer){
        return subServerService.deleteSubServer(serverId, subServer);
    }
}
