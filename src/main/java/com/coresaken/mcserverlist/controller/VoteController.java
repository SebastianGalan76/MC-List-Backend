package com.coresaken.mcserverlist.controller;

import com.coresaken.mcserverlist.data.dto.VoteDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.service.server.VoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoteController {
    final VoteService voteService;

    @PostMapping("/vote")
    public Response vote(@RequestBody VoteDto voteDto, HttpServletRequest request){
        return voteService.vote(voteDto, request);
    }
}
