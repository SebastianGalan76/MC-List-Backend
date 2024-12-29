package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.dto.ServerListDto;
import com.coresaken.mcserverlist.data.mapper.ServerListMapper;
import com.coresaken.mcserverlist.data.response.ObjectResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.Banner;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.model.server.Server;
import com.coresaken.mcserverlist.database.model.server.ServerUserRole;
import com.coresaken.mcserverlist.database.repository.BannerRepository;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import com.coresaken.mcserverlist.database.repository.server.ServerUserRoleRepository;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    final ServerUserRoleRepository serverUserRoleRepository;
    final BannerRepository bannerRepository;

    @Nullable
    public User getLoggedUser(){
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal() instanceof User userDetails ? userDetails : null;
    }

    @Nullable
    public User getUserByEmailOrLogin(String identifier){
        return userRepository.findByEmailOrLogin(identifier, identifier).orElse(null);
    }

    public ResponseEntity<Response> changePassword(String currentPassword, String newPassword) {
        if(newPassword.length()<4){
            return Response.badRequest(1, "Hasło jest zbyt krótkie");
        }

        User user = getLoggedUser();
        if(user == null){
            return Response.badRequest(2, "Twoja sesja wygasła. Zaloguj się ponownie");
        }

        if(!passwordEncoder.matches(currentPassword, user.getPassword())){
            return Response.badRequest(3, "Twoje obecne hasło jest niepoprawne.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Response.ok("Hasło zostało prawidłowo zmienione");
    }
    public ResponseEntity<Response> changeLogin(String newLogin){
        if(newLogin.length()>30){
            return Response.badRequest(1, "Login jest zbyt długi");
        }
        if(newLogin.length()<4){
            return Response.badRequest(2, "Login jest zbyt krótki");
        }

        User user = getLoggedUser();
        if(user == null){
            return Response.badRequest(3, "Twoja sesja wygasła. Zaloguj się ponownie");
        }

        Optional<User> userWithLogin = userRepository.findByLogin(newLogin);
        if(userWithLogin.isPresent()){
            return Response.badRequest(4, "Login jest już zajęty");
        }

        user.setLogin(newLogin);
        userRepository.save(user);
        return Response.ok("Login został prawidłowo zmieniony");
    }
    public ResponseEntity<Response> changeEmail(String newEmail) {
        if(newEmail.length()>60){
            return Response.badRequest(1, "Email jest zbyt długi");
        }

        User user = getLoggedUser();
        if(user == null){
            return Response.badRequest(2, "Twoja sesja wygasła. Zaloguj się ponownie");
        }

        Optional<User> userWithEmail = userRepository.findByEmail(newEmail);
        if(userWithEmail.isPresent()){
            return Response.badRequest(3, "E-mail jest już zajęty");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
        return Response.ok("E-mail został prawidłowo zmieniony");
    }

    public Page<ServerListDto> findServersByUser() {
        User user = getLoggedUser();

        if(user == null){
            return null;
        }

        List<ServerUserRole> sur = serverUserRoleRepository.findByUser(user);
        List<ServerListDto> servers = sur.stream().map(ServerUserRole::getServer).map(ServerListMapper::toDTO).toList();

        Pageable pageable = PageRequest.of(0, 30);
        return new PageImpl<ServerListDto>(servers, pageable, 0);
    }

    public List<Banner> findBannersByUser() {
        User user = getLoggedUser();

        if(user == null){
            return null;
        }

        List<Banner> list = bannerRepository.findByOwnerId(user.getId());
        list.sort(Comparator.comparingLong(Banner::getId));

        return list;
    }
}
