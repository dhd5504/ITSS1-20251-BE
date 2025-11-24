package org.itss.service;

import org.itss.dto.request.auth.LoginRequest;
import org.itss.dto.request.auth.RegisterRequest;
import org.itss.dto.request.auth.TokenRefreshRequest;
import org.itss.dto.response.Result;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    Result register(RegisterRequest request);

    Result login(LoginRequest request, HttpServletResponse res);

    Result refreshToken(TokenRefreshRequest request);

    Result logout(HttpServletResponse res);
}
