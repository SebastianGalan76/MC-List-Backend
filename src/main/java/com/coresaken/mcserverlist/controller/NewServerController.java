package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.NewServerDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.service.NewServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class NewServerController {
    final NewServerService newServerService;

    @RequestMapping("/add-new-server")
    public String getAddNewServerPage(){
        return "subPage/addNewServer";
    }

    @ResponseBody
    @PostMapping("/add-new-server/post")
    public Response addNewServer(@RequestBody NewServerDto newServerDto){
        return newServerService.addNewServer(newServerDto);
    }
}
