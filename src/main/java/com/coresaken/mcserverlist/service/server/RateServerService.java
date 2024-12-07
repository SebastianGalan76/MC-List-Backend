package com.coresaken.mcserverlist.service.server;

import com.coresaken.mcserverlist.data.dto.RateServerDto;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ratings.PlayerRating;
import com.coresaken.mcserverlist.database.model.server.ratings.RatingCategory;
import com.coresaken.mcserverlist.database.repository.PlayerRatingRepository;
import com.coresaken.mcserverlist.database.repository.RatingCategoryRepository;
import com.coresaken.mcserverlist.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RateServerService {
    final UserService userService;
    final ServerService serverService;

    final PlayerRatingRepository playerRatingRepository;
    final RatingCategoryRepository ratingCategoryRepository;

    @Transactional
    public ResponseEntity<Response> rateServer(Long serverId, List<RateServerDto> playerRatings) {
        User user = userService.getLoggedUser();

        if(user==null){
            return Response.badRequest(1,"Twoja sesja wygasła. Zaloguj się ponownie, aby ocenić serwer!");
        }

        Server server = serverService.getServerById(serverId);
        if(server==null){
            return Response.badRequest(2, "Wystąpił nieoczekiwany błąd. Serwer o podanym ID nie istnieje!");
        }

        //Remove ratings if all ratings are equal to 1
        boolean antiKid = false;
        for(RateServerDto pr:playerRatings){
            if (pr.rate() > 1) {
                antiKid = true;
                break;
            }
        }
        if(!antiKid){
            return Response.ok("Oceniono prawidłowo serwer");
        }

        for(RateServerDto pr:playerRatings){
            RatingCategory ratingCategory;
            try{
                ratingCategory = ratingCategoryRepository.getReferenceById(pr.categoryId());
            }catch (EntityNotFoundException e){
                continue;
            }

            if(pr.rate() <=0){
                playerRatingRepository.deleteByUserAndServerAndCategory(user, server,ratingCategory);
                continue;
            }

            Optional<PlayerRating> existingRating = playerRatingRepository.findByUserAndServerAndCategory(user, server, ratingCategory);
            PlayerRating playerRating;
            if (existingRating.isPresent()) {
                playerRating = existingRating.get();
                playerRating.setRate(pr.rate());
            } else {
                playerRating = new PlayerRating();
                playerRating.setUser(user);
                playerRating.setServer(server);
                playerRating.setCategory(ratingCategory);
                playerRating.setRate(pr.rate());
            }
            playerRatingRepository.save(playerRating);
        }

        return Response.ok("Oceniono prawidłowo serwer");
    }
}
