package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.StaffDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.service.server.ManageServerService;
import com.coresaken.mcserverlist.service.server.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ManageServerController {
    final ManageServerService manageServerService;
    final ServerService serverService;

    @RequestMapping("/server/{id}/manage/staff")
    public String getManageStaffPage(@PathVariable("id") Long serverId, Model model){
        Server server = serverService.getServerById(serverId);

        if(server==null){

        }

        model.addAttribute("server", server);

        //TODO Check permissions
        return "manage/manageStaff";
    }

    @ResponseBody
    @PostMapping("/server/{id}/manage/staff/save")
    public Response saveServerStaff(@PathVariable("id") Long serverId, @RequestBody StaffDto staffDto){
        Server server = serverService.getServerById(serverId);

        if(server==null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił nieoczekiwany błąd #4121. Możesz zgłosić go do Administracji strony.").build();
        }

        //TODO Check permissions

        return manageServerService.saveServerStaff(server, staffDto);
    }
}
