package com.coresaken.mcserverlist.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
public class RedirectResponse {
    final boolean success;
    final String message;
    final int errorCode;
    final String destination;

    public static ResponseEntity<RedirectResponse> ok(String message, String destination){
        return ResponseEntity.ok(new RedirectResponse(true, message, -1, destination));
    }

    public static ResponseEntity<RedirectResponse> badRequest(int errorCode, String message, String destination){
        return ResponseEntity.badRequest().body(new RedirectResponse(false, message, errorCode, destination));
    }
}
