package com.coresaken.mcserverlist.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
public class Response {
    protected final boolean success;
    protected final String message;
    protected final int errorCode;

    public static ResponseEntity<Response> ok(String message){
        return ResponseEntity.ok(new Response(true, message, -1));
    }

    public static ResponseEntity<Response> badRequest(int errorCode, String message){
        return ResponseEntity.badRequest().body(new Response(false, message, errorCode));
    }
}
