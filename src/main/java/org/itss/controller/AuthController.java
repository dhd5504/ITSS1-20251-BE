package org.itss.controller;

import org.springframework.lang.NonNull;

import org.itss.dto.request.auth.LoginRequest;
import org.itss.dto.request.auth.RegisterRequest;
import org.itss.dto.request.auth.TokenRefreshRequest;
import org.itss.dto.response.Result;
import org.itss.service.AuthService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result register(@RequestBody @NonNull RegisterRequest request) {
        return authService.register(java.util.Objects.requireNonNull(request));
    }

    @PostMapping("/login")
    public Result login(@RequestBody @NonNull LoginRequest request, @NonNull HttpServletResponse res) {
        return authService.login(java.util.Objects.requireNonNull(request), java.util.Objects.requireNonNull(res));
    }

    @PostMapping("/refresh")
    public Result refresh(@Valid @RequestBody @NonNull TokenRefreshRequest request) {
        return authService.refreshToken(java.util.Objects.requireNonNull(request));
    }

    @PutMapping("/logout")
    public Result logout(@NonNull HttpServletResponse res) {
        return authService.logout(java.util.Objects.requireNonNull(res));
    }
}
