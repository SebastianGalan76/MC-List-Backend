package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.repository.PlayerRatingRepository;
import com.coresaken.mcserverlist.database.repository.RatingCategoryRepository;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.service.server.ServerService;
import com.coresaken.mcserverlist.util.PermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ServerController {
    final ServerService serverService;
    final PlayerRatingRepository playerRatingRepository;
    final RatingCategoryRepository ratingCategoryRepository;

    final UserService userService;

    /*@RequestMapping("/server/{ip}")
    public String getServerPage(@PathVariable("ip") String ip, Model model){
        Server server = serverService.getServerByIp(ip);

        if(server==null){
            return "error/404";
        }

        model.addAttribute("user", userService.getLoggedUser());
        model.addAttribute("server", server);
        model.addAttribute("ratings", playerRatingRepository.findByServer(server));
        model.addAttribute("categories", ratingCategoryRepository.findAll());
        model.addAttribute("role", PermissionChecker.getRoleForServer(userService.getLoggedUser(), server));
        return "subPage/server";
    }*/

    @GetMapping("/server/{ip}")
    public ResponseEntity<Server> getServer(@PathVariable("ip") String ip){
        return new ResponseEntity<>(serverService.getServerByIp(ip), HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/server/list/{page}")
    public Page<Server> getServers(@PathVariable("page") int page){
        return serverService.getServers(page);
    }

    @ResponseBody
    @DeleteMapping("/server/{id}")
    public ResponseEntity<Response> deleteServer(@PathVariable("id") Long id){
        Server server = serverService.getServerById(id);

        if(server==null){
            return Response.badRequest(1, "Wystąpił nieoczekiwany błąd. Serwer o podanym ID nie istnieje");
        }

        return serverService.delete(server);
    }

    @ResponseBody
    @GetMapping("/random")
    public String getRandomServerIp(){
        return serverService.getRandomServerIp();
    }
}
