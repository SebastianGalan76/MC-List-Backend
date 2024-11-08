package com.coresaken.mcserverlist.auth.dto.response;

import com.coresaken.mcserverlist.data.response.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TokenResponse extends Response {
    String token;

    public TokenResponse(boolean success, String message, int errorCode){
        super(success, message, errorCode);
    }
    public TokenResponse(String message, String token){
        super(true, message, -1);
        this.token = token;
    }

    public static ResponseEntity<TokenResponse> ok(String message, String token){
        return ResponseEntity.ok(new TokenResponse(message, token));
    }

    public static ResponseEntity<TokenResponse> badRequestToken(int code, String message){
        return ResponseEntity.badRequest().body(new TokenResponse(false, message, code));
    }
}
