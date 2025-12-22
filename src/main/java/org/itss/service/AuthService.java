package org.itss.service;

import org.springframework.lang.NonNull;

import org.itss.dto.request.auth.LoginRequest;
import org.itss.dto.request.auth.RegisterRequest;
import org.itss.dto.request.auth.TokenRefreshRequest;
import org.itss.dto.response.Result;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    Result register(@NonNull RegisterRequest request);

    Result login(@NonNull LoginRequest request, @NonNull HttpServletResponse res);

    Result refreshToken(@NonNull TokenRefreshRequest request);

    Result logout(@NonNull HttpServletResponse res);
}
