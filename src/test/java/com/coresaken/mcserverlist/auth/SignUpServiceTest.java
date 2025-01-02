package com.coresaken.mcserverlist.auth;

import com.coresaken.mcserverlist.auth.dto.request.SignUpRequestDto;
import com.coresaken.mcserverlist.auth.service.SignUpService;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import com.coresaken.mcserverlist.service.async.AsyncAccountService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class SignUpServiceTest {
    @Mock
    private AsyncAccountService asyncAccountService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignUpService signUpService;

    public SignUpServiceTest(){
        openMocks(this);
    }

    @Test
    public void signUp_givenTooLongLogin(){
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("a".repeat(31), "contact@email.pl", "password");

        ResponseEntity<Response> responseEntity = signUpService.signUp(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(1, response.getErrorCode());
    }

    @Test
    public void signUp_givenTooShortLogin(){
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("a".repeat(3), "contact@email.pl", "password");

        ResponseEntity<Response> responseEntity = signUpService.signUp(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(2, response.getErrorCode());
    }

    @Test
    public void signUp_givenTooShortPassword(){
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("login", "contact@email.pl", "pas");

        ResponseEntity<Response> responseEntity = signUpService.signUp(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(3, response.getErrorCode());
    }

    @Test
    public void signUp_givenTooLongEmail(){
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("login", "a".repeat(61), "password");

        ResponseEntity<Response> responseEntity = signUpService.signUp(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(4, response.getErrorCode());
    }

    @Test
    public void signUp_givenLoginIsAlreadyTaken(){
        String login = "login";
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto(login, "contact@email.pl", "password");

        when(userRepository.findByEmailOrLogin(anyString(), eq(login))).thenReturn(Optional.of(new User()));

        ResponseEntity<Response> responseEntity = signUpService.signUp(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(5, response.getErrorCode());
    }

    @Test
    public void signUp_givenEmailIsAlreadyTaken(){
        String email = "contact@email.pl";
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("login", email, "password");

        when(userRepository.findByEmailOrLogin(eq(email), anyString())).thenReturn(Optional.of(new User()));

        ResponseEntity<Response> responseEntity = signUpService.signUp(signUpRequestDto);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(5, response.getErrorCode());
    }

    @Test
    public void signUp_successfully(){
        String login = "login";
        String email = "contact@email.pl";
        String password = "password";
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto(login, email, password);

        when(userRepository.findByEmailOrLogin(email, login)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        ResponseEntity<Response> responseEntity = signUpService.signUp(signUpRequestDto);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(-1, response.getErrorCode());

        verify(userRepository, times(1)).save(any(User.class));
        verify(asyncAccountService, times(1)).processAccountActivation(any(), eq(email), any());
    }
}
