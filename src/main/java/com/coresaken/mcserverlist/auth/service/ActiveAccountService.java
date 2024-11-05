package com.coresaken.mcserverlist.auth.service;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.repository.ActiveAccountTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActiveAccountService {
    private final ActiveAccountTokenRepository activeAccountTokenRepository;

    public ResponseEntity<Response> activeAccount(String code){
        activeAccountTokenRepository.deleteByCode(code);
        return Response.ok("Aktywowano konto");
    }
}
