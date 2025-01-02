package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.service.BannerService;
import com.coresaken.mcserverlist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    final UserService userService;
    final BannerService bannerService;

    @RequestMapping("/rules")
    public String getRulesPage(Model model){
        model.addAttribute("user", userService.getLoggedUser());

        return "subPage/rules";
    }

    @RequestMapping("/privacy-policy")
    public String getPrivacyPolicyPage(Model model){
        model.addAttribute("user", userService.getLoggedUser());

        return "subPage/privacyPolicy";
    }

    @RequestMapping("/rewards")
    public String getRewardsPage(Model model){
        model.addAttribute("user", userService.getLoggedUser());

        return "subPage/rewards";
    }

    @RequestMapping("/connectionError")
    public String getConnectionErrorPage(Model model){
        model.addAttribute("user", userService.getLoggedUser());

        return "subPage/connectionError";
    }
}
