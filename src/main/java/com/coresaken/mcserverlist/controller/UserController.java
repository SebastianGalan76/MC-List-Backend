package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.ChangePasswordDto;
import com.coresaken.mcserverlist.data.dto.ServerListDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.Banner;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.BannerRepository;
import com.coresaken.mcserverlist.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    final UserService userService;
    final BannerRepository bannerRepository;

    @GetMapping("/user")
    public ResponseEntity<User> getLoggedUser(){
        return ResponseEntity.ok(userService.getLoggedUser());
    }

    @GetMapping("/user/find")
    public ResponseEntity<User> findUserByLoginOrEmail(@RequestParam("identifier") String identifier){
        User user = userService.getUserByEmailOrLogin(identifier);

        if(user!=null){
            return ResponseEntity.ok(user);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/servers")
    public Page<ServerListDto> findServersByUser(){
        return userService.findServersByUser();
    }

    @GetMapping("/user/banners")
    public List<Banner> findBannersByUser(){
        return userService.findBannersByUser();
    }

    @PostMapping("/user/change-password")
    public ResponseEntity<Response> updateUserPassword(@RequestBody ChangePasswordDto changePasswordDto){
        return userService.changePassword(changePasswordDto.currentPassword(), changePasswordDto.newPassword());
    }

    @PostMapping("/user/change-login")
    public ResponseEntity<Response> updateUserLogin(@RequestBody String login){
        return userService.changeLogin(login.trim());
    }

    @PostMapping("/user/change-email")
    public ResponseEntity<Response> updateUserEmail(@RequestBody String email){
        return userService.changeEmail(email.trim());
    }
}
