package org.itss.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private AuthUserResponse user;
    private String accessToken;
    private String refreshToken;
}
