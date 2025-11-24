package org.itss.controller;

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
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest request, HttpServletResponse res) {
        return authService.login(request, res);
    }

    @PostMapping("/refresh")
    public Result refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PutMapping("/logout")
    public Result logout(HttpServletResponse res) {
        return authService.logout(res);
    }
}
