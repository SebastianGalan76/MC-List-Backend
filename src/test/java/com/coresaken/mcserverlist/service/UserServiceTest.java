package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    public UserServiceTest() {
        openMocks(this);
    }

    @Test
    public void changePassword_newPasswordIsTooShort() {
        String newPassword = "aaa";

        ResponseEntity<Response> responseEntity = userService.changePassword("currentPassword", newPassword);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(1, response.getErrorCode());
    }

    @Test
    public void changePassword_currentPasswordIsIncorrect() {
        String newPassword = "newPassword";
        String currentPassword = "incorrectPassword";

        User user = new User();

        UserService spyUserService = spy(userService);
        Mockito.doReturn(user).when(spyUserService).getLoggedUser();

        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(false);

        ResponseEntity<Response> responseEntity = spyUserService.changePassword(currentPassword, newPassword);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(3, response.getErrorCode());
    }

    @Test
    public void changePassword_successfully(){
        String newPassword = "newPassword";
        String currentPassword = "incorrectPassword";

        User user = new User();

        UserService spyUserService = spy(userService);
        Mockito.doReturn(user).when(spyUserService).getLoggedUser();

        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);

        ResponseEntity<Response> responseEntity = spyUserService.changePassword(currentPassword, newPassword);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Response response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(-1, response.getErrorCode());
    }
}
