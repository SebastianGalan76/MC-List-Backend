package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.database.model.User;
import com.coresaken.mcserverlist.database.repository.UserRepository;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    final UserRepository userRepository;

    @Nullable
    public User getLoggedUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User userDetails) {
            return userDetails;
        }
        return null;
    }

    @Nullable
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }
}
