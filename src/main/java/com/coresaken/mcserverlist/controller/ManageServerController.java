package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.*;
import com.coresaken.mcserverlist.data.response.RedirectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Link;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.service.server.ManageServerService;
import com.coresaken.mcserverlist.service.server.ServerService;
import com.coresaken.mcserverlist.util.PermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ManageServerController {
    final ManageServerService manageServerService;
    final ServerService serverService;

    final UserService userService;

    @RequestMapping("/server/{id}/manage")
    public String getManagePage(@PathVariable("id") Long serverId, Model model){
        Server server = serverService.getServerById(serverId);
        User user = userService.getLoggedUser();

        if(server==null){
            return "error/404";
        }
        if(!PermissionChecker.hasPermissionForServer(user, server, ServerUserRole.Role.HELPER)){
            return "error/403";
        }

        model.addAttribute("user", user);
        model.addAttribute("server", server);
        model.addAttribute("role", PermissionChecker.getRoleForServer(user, server));

        return "manage/manageHome";
    }

    @RequestMapping("/server/{id}/manage/info")
    public String getManageInfoPage(@PathVariable("id") Long serverId, Model model){
        Server server = serverService.getServerById(serverId);
        User user = userService.getLoggedUser();

        if(server==null){
            return "error/404";
        }
        if(!PermissionChecker.hasPermissionForServer(user, server, ServerUserRole.Role.MODERATOR)){
            return "error/403";
        }

        model.addAttribute("user", user);
        model.addAttribute("server", server);
        model.addAttribute("role", PermissionChecker.getRoleForServer(user, server));

        return "manage/manageInfo";
    }

    @PostMapping("/server/{id}/manage/info")
    public ResponseEntity<RedirectResponse> saveServerInfo(@PathVariable("id") Long serverId, @RequestBody BasicServerDto serverDto){
        return manageServerService.saveServerInfo(serverId, serverDto);
    }

    @RequestMapping("/server/{id}/manage/description")
    public String getManageDescriptionPage(@PathVariable("id") Long serverId, Model model){
        Server server = serverService.getServerById(serverId);
        User user = userService.getLoggedUser();

        if(server==null){
            return "error/404";
        }
        if(!PermissionChecker.hasPermissionForServer(user, server, ServerUserRole.Role.HELPER)){
            return "error/403";
        }

        model.addAttribute("user", user);
        model.addAttribute("server", server);
        model.addAttribute("role", PermissionChecker.getRoleForServer(user, server));

        return "manage/manageDescription";
    }

    @PostMapping("/server/{id}/manage/description")
    public ResponseEntity<Response> saveServerDescription(@PathVariable("id") Long serverId, @RequestBody String description){
        return manageServerService.saveServerDescription(serverId, description);
    }

    @RequestMapping("/server/{id}/manage/banner")
    public String getManageBannerPage(@PathVariable("id") Long serverId, Model model){
        Server server = serverService.getServerById(serverId);
        User user = userService.getLoggedUser();

        if(server==null){
            return "error/404";
        }
        if(!PermissionChecker.hasPermissionForServer(user, server, ServerUserRole.Role.MODERATOR)){
            return "error/403";
        }

        model.addAttribute("user", user);
        model.addAttribute("server", server);
        model.addAttribute("role", PermissionChecker.getRoleForServer(user, server));

        return "manage/manageBanner";
    }

    @PostMapping("/server/{id}/manage/banner")
    public ResponseEntity<Response> saveServerBanner(@PathVariable("id") Long serverId, @Param("file") MultipartFile file, @Param("url") String url){
        return manageServerService.saveServerBanner(serverId, file, url);
    }
}
