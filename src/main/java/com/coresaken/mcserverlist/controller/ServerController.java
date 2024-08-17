package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.SearchServerDto;
import com.coresaken.mcserverlist.data.dto.StringDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ratings.PlayerRating;
import com.coresaken.mcserverlist.database.repository.PlayerRatingRepository;
import com.coresaken.mcserverlist.database.repository.RatingCategoryRepository;
import com.coresaken.mcserverlist.service.UserService;
import com.coresaken.mcserverlist.service.server.ServerService;
import com.coresaken.mcserverlist.util.PermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            return "error/404";
        }

        model.addAttribute("user", userService.getLoggedUser());
        model.addAttribute("server", server);
        model.addAttribute("ratings", playerRatingRepository.findByServer(server));
        model.addAttribute("categories", ratingCategoryRepository.findAll());
        model.addAttribute("role", PermissionChecker.getRoleForServer(userService.getLoggedUser(), server));
        return "subPage/server";
    }

    @ResponseBody
    @DeleteMapping("/server/{id}")
    public Response deleteServer(@PathVariable("id") Long id){
        Server server = serverService.getServerById(id);

        if(server==null){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił nieoczekiwany błąd #8741. Możesz zgłosić go do Administracji strony.").build();
        }

        return serverService.deleteServer(server);
    }

    @ResponseBody
    @PostMapping("/server/search/{page}")
    public Page<Server> searchServer(@RequestBody SearchServerDto searchServerDto, @PathVariable("page") int page){
        Pageable pageable = PageRequest.of(page - 1, 30);

        return serverService.searchServer(searchServerDto, pageable);
    }

    @ResponseBody
    @GetMapping("/random")
    public String getRandomServerIp(){
        return serverService.getRandomServerIp();
    }
}
