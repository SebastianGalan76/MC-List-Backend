package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.VoteDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.Vote;
import com.coresaken.mcserverlist.database.repository.ServerRepository;
import com.coresaken.mcserverlist.database.repository.server.VoteRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Service
@RequiredArgsConstructor
public class VoteService {
    final VoteRepository voteRepository;
    final ServerRepository serverRepository;

    public Response vote(VoteDto voteDto, HttpServletRequest request){
        LocalDate today = LocalDate.now();
        String ip = request.getRemoteAddr();

        Optional<Server> server = serverRepository.findById(voteDto.getServerId());
        if(server.isEmpty()){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Wystąpił nieoczekiwany błąd. Spróbuj ponownie później").build();
        }

        List<Vote> votes = voteRepository.findByIpOrNickAndDateAndServerId(ip, voteDto.getNick(), today, voteDto.getServerId());
        if(votes != null && !votes.isEmpty()){
            return Response.builder().status(HttpStatus.BAD_REQUEST).message("Głosowałeś/aś już dzisiaj na serwer").build();
        }

        Vote vote = new Vote();
        vote.setVotedAt(today);
        vote.setReceived(false);
        vote.setServer(server.get());

        vote.setIp(ip);
        vote.setNick(voteDto.getNick());

        voteRepository.save(vote);
        return Response.builder().status(HttpStatus.OK).message("Głos został oddany").build();
    }
}
