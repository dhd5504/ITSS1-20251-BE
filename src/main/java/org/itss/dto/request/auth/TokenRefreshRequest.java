package org.itss.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {

    @NotBlank(message = "refresh_token cannot be empty")
    private String refreshToken;
}
