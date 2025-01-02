package com.coresaken.mcserverlist.auth.controller;

import com.coresaken.mcserverlist.auth.dto.request.ChangePasswordDto;
import com.coresaken.mcserverlist.auth.dto.request.SignInRequestDto;
import com.coresaken.mcserverlist.auth.dto.request.SignUpRequestDto;
import com.coresaken.mcserverlist.auth.dto.response.TokenResponse;
import com.coresaken.mcserverlist.auth.service.*;
import com.coresaken.mcserverlist.data.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final SignInService signInService;
    private final SignUpService signUpService;
    private final LogoutService logoutService;
    private final ActiveAccountService activeAccountService;
    private final ResetPasswordService resetPasswordService;

    @PostMapping("/signIne")
    public ResponseEntity<TokenResponse> signIn(@RequestBody SignInRequestDto request){
        return signInService.signIn(request);
    }

    @PostMapping("/signUp")
    public ResponseEntity<Response> signUp(@RequestBody SignUpRequestDto request){
        return signUpService.signUp(request);
    }

    @GetMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletRequest request, HttpServletResponse response){
        return logoutService.logout(request, response);
    }

    @PostMapping("/active/{code}")
    public ResponseEntity<Response> activeAccount(@PathVariable("code") String code){
        return activeAccountService.activeAccount(code);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Response> resetPassword(@RequestParam String email) {
        return resetPasswordService.resetPassword(email);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Response> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        return resetPasswordService.changePassword(changePasswordDto.token(), changePasswordDto.newPassword());
    }
}
