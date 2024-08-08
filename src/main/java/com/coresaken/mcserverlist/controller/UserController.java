package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.Banner;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.BannerRepository;
import com.coresaken.mcserverlist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {
    final UserService userService;
    final BannerRepository bannerRepository;

    @ResponseBody
    @GetMapping("/user/find")
    public Response findUserByLoginOrEmail(@RequestParam("identifier") String identifier){
        User user = userService.getUserByEmailOrLogin(identifier);

        if(user!=null){
            return Response.builder().status(HttpStatus.OK).build();
        }

        return Response.builder().status(HttpStatus.BAD_REQUEST).message("Nie ma takiego użytkownika o podanym adresie e-mail lub login").build();
    }

    @RequestMapping("/user/banner")
    public String getUserBannersPage(Model model){
        User user = userService.getLoggedUser();
        if(user == null){
            return "auth/signIn";
        }

        model.addAttribute("user", user);
        model.addAttribute("banners", bannerRepository.findByOwnerId(user.getId()));
        return "user/manageBanner";
    }
}
