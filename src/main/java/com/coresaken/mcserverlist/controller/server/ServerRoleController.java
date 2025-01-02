package com.coresaken.mcserverlist.controller.server;

import com.coresaken.mcserverlist.data.dto.ServerRoleDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.service.server.ServerRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ServerRoleController {
    final ServerRoleService serverRoleService;

    @PostMapping("/server/{id}/manage/role")
    public ResponseEntity<Response> createRole(@PathVariable("id") Long serverId, @RequestBody ServerRoleDto serverRoleDto){
        return serverRoleService.createRole(serverId, serverRoleDto);
    }

    @PostMapping("/server/{id}/manage/role/delete")
    public ResponseEntity<Response> deleteRole(@PathVariable("id") Long serverId, @RequestBody ServerRoleDto serverRoleDto){
        return serverRoleService.deleteRole(serverId, serverRoleDto);
    }
}
