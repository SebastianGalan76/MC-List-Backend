package com.coresaken.mcserverlist.auth.service;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.ResetPasswordToken;
import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.ResetPasswordTokenRepository;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import com.coresaken.mcserverlist.service.EmailSenderService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {
    private final EmailSenderService emailSenderService;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    public ResponseEntity<Response> resetPassword(String email){
        User user = userRepository.findByEmail(email).orElse(null);
        if(user==null){
            return Response.badRequest(1, "Użytkownik z podanym adresem e-mail nie istnieje.");
        }

        String token = UUID.randomUUID().toString();

        ResetPasswordToken resetToken = resetPasswordTokenRepository.findByUserId(user.getId())
                .orElse(null);
        if(resetToken!=null){
            resetToken.setToken(token);
        }
        else{
            resetToken = new ResetPasswordToken(user, token);
        }

        resetPasswordTokenRepository.save(resetToken);

        try{
            emailSenderService.sendResetPasswordEmail(email, token);
        }
        catch (MessagingException e){
            e.printStackTrace();
            return Response.badRequest(2, "Wystąpił błąd podczas wysyłania wiadomości. Spróbuj ponownie później");
        }

        return Response.ok("Link resetujący hasło wysłaliśmy na podany adres e-mail");
    }

    public ResponseEntity<Response> changePassword(@RequestParam String token, @RequestParam String newPassword) {
        if(newPassword.length()<4){
            return Response.badRequest(3,"Hasło jest zbyt krótkie");
        }

        ResetPasswordToken resetToken = resetPasswordTokenRepository.findByToken(token).orElse(null);
        if(resetToken==null || resetToken.getExpiredAt().isBefore(LocalDateTime.now())){
            if(resetToken!=null){
                resetPasswordTokenRepository.delete(resetToken);
            }

            return Response.badRequest(2, "Twój token wygasł. Zresetuj hasło ponownie!");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetPasswordTokenRepository.delete(resetToken);
        return Response.ok("Hasło zostało prawidłowo zmienione");
    }
}
