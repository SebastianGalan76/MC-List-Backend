package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ratings.PlayerRating;
import com.coresaken.mcserverlist.database.repository.PlayerRatingRepository;
import com.coresaken.mcserverlist.database.repository.RatingCategoryRepository;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.service.server.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ServerController {
    final UserService userService;
    final ServerService serverService;
    final PlayerRatingRepository playerRatingRepository;
    final RatingCategoryRepository ratingCategoryRepository;

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

        model.addAttribute("user", userService.getLoggedUser());
        model.addAttribute("server", server);
        model.addAttribute("ratings", playerRatingRepository.findByServer(server));
        model.addAttribute("categories", ratingCategoryRepository.findAll());
        return "subPage/server";
    }

    @ResponseBody
    @PostMapping("/server/{ip}/rate/save")
    public Response getServerRatePage(@PathVariable("ip") String ip, @RequestBody List<PlayerRating> playerRatings){
        Server server = serverService.getServerByIp(ip);

        if(server==null){
            //TODO error page
        }

        return serverService.rateServer(server, playerRatings);
    }
}
