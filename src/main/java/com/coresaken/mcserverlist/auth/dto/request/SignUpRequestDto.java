package com.coresaken.mcserverlist.auth.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public record SignUpRequestDto(String login, String email, String password) {
}
