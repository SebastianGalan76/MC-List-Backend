package com.coresaken.mcserverlist.auth.dto.request;

public record ResetPasswordDto(String token, String newPassword) {
}
