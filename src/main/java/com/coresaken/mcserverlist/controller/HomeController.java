package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    final UserService userService;

    @RequestMapping("/")
    public String getHomePage(Model model){
        model.addAttribute("user", userService.getLoggedUser());

        return "home";
    }
}
