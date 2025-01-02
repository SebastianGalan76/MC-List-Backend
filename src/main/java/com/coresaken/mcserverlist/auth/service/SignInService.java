package com.coresaken.mcserverlist.auth.service;

import com.coresaken.mcserverlist.auth.dto.request.SignInRequestDto;
import com.coresaken.mcserverlist.auth.dto.response.TokenResponse;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.ActiveAccountTokenRepository;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignInService {
    final JwtService jwtService;
    final PasswordEncoder passwordEncoder;
    final AuthenticationManager authenticationManager;

    final UserRepository userRepository;
    final ActiveAccountTokenRepository activeAccountTokenRepository;

    public ResponseEntity<TokenResponse> signIn(SignInRequestDto request) {
        String identifier = request.identifier();

        User user = userRepository.findByEmailOrLogin(identifier, identifier).orElse(null);
        if(user == null){
            return TokenResponse.badRequestToken(1,"Niepoprawne dane logowania!");
        }

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            return TokenResponse.badRequestToken(2,"Niepoprawne dane logowania!");
        }

        if(activeAccountTokenRepository.findByUserId(user.getId()).isPresent()){
            return TokenResponse.badRequestToken(3,"Konto nie zostało aktywowane. Wyszukaj email i aktywuj konto!");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                identifier,
                request.password()
        ));

        var jwtToken = jwtService.generateToken(user);

        return TokenResponse.ok("Zalogowano prawidłowo", jwtToken);
    }
}
