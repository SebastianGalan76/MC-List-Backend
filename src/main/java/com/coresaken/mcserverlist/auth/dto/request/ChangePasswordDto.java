package com.coresaken.mcserverlist.auth.dto.request;

public record ChangePasswordDto(String token, String newPassword) {
}
