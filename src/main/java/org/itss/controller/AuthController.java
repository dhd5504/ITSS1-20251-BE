package org.itss.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.itss.dto.request.LoginRequest;
import org.itss.dto.request.RegisterRequest;
import org.itss.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest req) {
        return userService.register(req);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest req,  HttpServletResponse res) {
        return userService.login(req, res);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        return ResponseEntity.ok(userService.logout(response));
    }
}
