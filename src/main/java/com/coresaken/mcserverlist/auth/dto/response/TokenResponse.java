package com.coresaken.mcserverlist.auth.dto.response;

import com.coresaken.mcserverlist.data.response.Response;
import lombok.EqualsAndHashCode;
import org.springframework.http.ResponseEntity;

@EqualsAndHashCode(callSuper = true)
public class TokenResponse extends Response {
    String token;

    public TokenResponse(boolean success, String message, int errorCode){
        super(success, message, errorCode);
    }
    public TokenResponse(String message, String token){
        super(true, message, 0);
        this.token = token;
    }

    public static ResponseEntity<TokenResponse> ok(String message, String token){
        return ResponseEntity.badRequest().body(new TokenResponse(message, token));
    }

    public static ResponseEntity<TokenResponse> badRequestToken(int code, String message){
        return ResponseEntity.badRequest().body(new TokenResponse(false, message, code));
    }
}
