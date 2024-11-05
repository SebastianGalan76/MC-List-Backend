package com.coresaken.mcserverlist.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public record ResetPasswordDto(String token, String newPassword) {
}
