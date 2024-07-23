package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.service.server.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ServerController {
    final ServerService serverService;

    @ResponseBody
    @GetMapping("/server/list/{page}")
    public Page<Server> getServers(@PathVariable("page") int page){
        return serverService.getServers(page);
    }

    @RequestMapping("/server/{ip}")
    public String getServerPage(@PathVariable("ip") String ip, Model model){
        Server server = serverService.getServerByIp(ip);

        if(server==null){
            //TODO error page
        }
        model.addAttribute("server", server);
        return "subPage/server";
    }
}
