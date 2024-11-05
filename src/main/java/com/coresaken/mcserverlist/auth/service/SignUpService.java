package com.coresaken.mcserverlist.auth.service;

import com.coresaken.mcserverlist.auth.dto.request.SignUpRequestDto;
import com.coresaken.mcserverlist.auth.dto.response.TokenResponse;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import com.coresaken.mcserverlist.service.async.AsyncAccountService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignUpService {
    final AsyncAccountService asyncAccountService;

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<Response> signUp(SignUpRequestDto request) {
        String login = request.login().trim();
        String email = request.email().trim();
        String password = request.password().trim();

        if(login.length()>30){
            return Response.badRequest(1,"Login jest zbyt długi");
        }
        if(login.length()<4){
            return Response.badRequest(2,"Login jest zbyt krótki");
        }
        if(password.length()<4){
            return Response.badRequest(3,"Hasło jest zbyt krótkie");
        }
        if(email.length()>60){
            return Response.badRequest(4,"Email jest zbyt długi");
        }
        User savedUser = userRepository.findByEmailOrLogin(email, login).orElse(null);
        if(savedUser != null){
            return Response.badRequest(5,"Login lub email jest już zajęty!");
        }

        User user = User.builder()
                .login(request.login())
                .email(request.email())
                .uuid(UUID.randomUUID().toString())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.USER)
                .build();

        try{
            user = userRepository.save(user);
        }catch (DataIntegrityViolationException e){
            return TokenResponse.badRequest(5,"Login lub email jest już zajęty!");
        }

        String activeAccountToken = UUID.randomUUID().toString();
        asyncAccountService.processAccountActivation(user.getId(), email, activeAccountToken);

        return Response.ok("Zarejestrowano prawidłowo");
    }
}
