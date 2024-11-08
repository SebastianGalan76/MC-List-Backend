package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

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
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return Response.badRequest(3, "Twoje obecne hasło jest niepoprawne.");
        }

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
}
