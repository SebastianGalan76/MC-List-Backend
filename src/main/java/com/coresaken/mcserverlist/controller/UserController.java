package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.ChangePasswordDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.Banner;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.database.repository.BannerRepository;
import com.coresaken.mcserverlist.database.repository.server.ServerUserRoleRepository;
import com.coresaken.mcserverlist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    final UserService userService;
    final BannerRepository bannerRepository;
    final ServerUserRoleRepository serverUserRoleRepository;

    @ResponseBody
    @GetMapping("/user/find")
    public ResponseEntity<Response> findUserByLoginOrEmail(@RequestParam("identifier") String identifier){
        User user = userService.getUserByEmailOrLogin(identifier);

        if(user!=null){
            return Response.ok("Użytkownik istnieje");
        }

        return Response.badRequest(1, "Nie ma takiego użytkownika o podanym adresie e-mail lub login");
    }

    @RequestMapping("/user/profile")
    public String getUserProfilePage(Model model){
        User user = userService.getLoggedUser();
        if(user == null){
            return "auth/signIn";
        }

        model.addAttribute("user", user);
        return "user/manageInfo";
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

    @RequestMapping("/user/server")
    public String getUserServerPage(Model model){
        User user = userService.getLoggedUser();
        if(user == null){
            return "auth/signIn";
        }

        model.addAttribute("user", user);

        List<ServerUserRole> serverUserRoleList = serverUserRoleRepository.findByUser(user);
        model.addAttribute("servers", serverUserRoleList.stream().map(ServerUserRole::getServer).toList());
        return "user/manageServer";
    }

    @ResponseBody
    @PostMapping("/user/change-password")
    public ResponseEntity<Response> updateUserPassword(@RequestBody ChangePasswordDto changePasswordDto){
        return  userService.changePassword(changePasswordDto);
    }

    @ResponseBody
    @PostMapping("/user/change-login")
    public ResponseEntity<Response> updateUserLogin(@RequestBody String login){
        return  userService.changeLogin(login.trim());
    }

    @ResponseBody
    @PostMapping("/user/change-email")
    public ResponseEntity<Response> updateUserEmail(@RequestBody String email){
        return  userService.changeEmail(email.trim());
    }
}
