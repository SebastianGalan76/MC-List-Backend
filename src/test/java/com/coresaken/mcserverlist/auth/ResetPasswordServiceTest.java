package com.coresaken.mcserverlist.auth;

import com.coresaken.mcserverlist.auth.service.ResetPasswordService;
import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.ResetPasswordToken;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.ResetPasswordTokenRepository;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import com.coresaken.mcserverlist.service.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class ResetPasswordServiceTest {
    @Mock
    private EmailSenderService emailSenderService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @InjectMocks
    private ResetPasswordService resetPasswordService;

    public ResetPasswordServiceTest(){
        openMocks(this);
    }

    @Test
    public void resetPassword_invalidEmail(){
        String email = "contact@email.pl";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<Response> responseEntity = resetPasswordService.resetPassword(email);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(1, response.getErrorCode());
    }

    @Test
    public void resetPassword_refreshToken(){
        String email = "contact@email.pl";

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResetPasswordToken resetPasswordToken = mock(ResetPasswordToken.class);
        when(resetPasswordTokenRepository.findByUserId(user.getId())).thenReturn(Optional.of(resetPasswordToken));

        ResponseEntity<Response> responseEntity = resetPasswordService.resetPassword(email);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(-1, response.getErrorCode());

        verify(resetPasswordToken, times(1)).setToken(anyString());
    }

    @Test
    public void resetPassword_createNewToken(){
        String email = "contact@email.pl";

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(resetPasswordTokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        ResponseEntity<Response> responseEntity = resetPasswordService.resetPassword(email);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(-1, response.getErrorCode());
    }

    @Test
    public void handleResetPassword_tokenExpired(){
        String token = "resetPasswordToken";

        ResetPasswordToken resetPasswordToken = mock(ResetPasswordToken.class);
        when(resetPasswordTokenRepository.findByToken(token)).thenReturn(Optional.of(resetPasswordToken));
        when(resetPasswordToken.getExpiredAt()).thenReturn(LocalDateTime.now().minusDays(1));

        ResponseEntity<Response> responseEntity = resetPasswordService.changePassword(token, "newPassword");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(2, response.getErrorCode());
    }
}
