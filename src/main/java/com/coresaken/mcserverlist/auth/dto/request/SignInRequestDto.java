package com.coresaken.mcserverlist.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public record SignInRequestDto(String identifier, String password) {
}
