package com.coresaken.mcserverlist.auth;

import com.coresaken.mcserverlist.auth.dto.request.SignInRequestDto;
import com.coresaken.mcserverlist.auth.dto.response.TokenResponse;
import com.coresaken.mcserverlist.auth.service.JwtService;
import com.coresaken.mcserverlist.auth.service.SignInService;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.ActiveAccountToken;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.ActiveAccountTokenRepository;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class SignInServiceTest {
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ActiveAccountTokenRepository activeAccountTokenRepository;

    @InjectMocks
    private SignInService signInService;

    public SignInServiceTest(){
        openMocks(this);
    }

    @Test
    public void signIn_givenIncorrectIdentifier(){
        String identifier = "login";
        SignInRequestDto signUpRequestDto = new SignInRequestDto(identifier, "password");

        when(userRepository.findByEmailOrLogin(identifier, identifier)).thenReturn(Optional.empty());

        ResponseEntity<TokenResponse> responseEntity = signInService.signIn(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(1, response.getErrorCode());
    }

    @Test
    public void signIn_givenIncorrectPassword(){
        String identifier = "login";
        String password = "password";
        SignInRequestDto signUpRequestDto = new SignInRequestDto(identifier, password);

        when(userRepository.findByEmailOrLogin(identifier, identifier)).thenReturn(Optional.of( new User()));

        ResponseEntity<TokenResponse> responseEntity = signInService.signIn(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(2, response.getErrorCode());
    }

    @Test
    public void signIn_accountIsNotActivated(){
        String identifier = "login";
        String password = "password";
        SignInRequestDto signUpRequestDto = new SignInRequestDto(identifier, password);

        User user = mock(User.class);
        when(userRepository.findByEmailOrLogin(identifier, identifier)).thenReturn(Optional.of(user));
        when(user.getPassword()).thenReturn("encodedPassword");

        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
        when(activeAccountTokenRepository.findByUserId(user.getId())).thenReturn(Optional.of(new ActiveAccountToken()));

        ResponseEntity<TokenResponse> responseEntity = signInService.signIn(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(3, response.getErrorCode());
    }

    @Test
    public void signIn_successfully(){
        String identifier = "login";
        String password = "password";
        String generatedToken = "mockedJwtToken";

        SignInRequestDto signUpRequestDto = new SignInRequestDto(identifier, password);

        User user = mock(User.class);
        when(userRepository.findByEmailOrLogin(identifier, identifier)).thenReturn(Optional.of(user));
        when(user.getPassword()).thenReturn("encodedPassword");

        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
        when(activeAccountTokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        when(jwtService.generateToken(user)).thenReturn(generatedToken);

        ResponseEntity<TokenResponse> responseEntity = signInService.signIn(signUpRequestDto);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        TokenResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(-1, response.getErrorCode());
        assertEquals(generatedToken, response.getToken());
    }
}
