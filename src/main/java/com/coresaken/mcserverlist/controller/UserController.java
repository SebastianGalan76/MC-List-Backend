package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    final UserService userService;

    @GetMapping("/user/find")
    public Response findUserByLoginOrEmail(@RequestParam("identifier") String identifier){
        User user = userService.getUserByEmailOrLogin(identifier);

        if(user!=null){
            return Response.builder().status(HttpStatus.OK).build();
        }

        return Response.builder().status(HttpStatus.BAD_REQUEST).message("Nie ma takiego użytkownika o podanym adresie e-mail lub login").build();
    }
}
