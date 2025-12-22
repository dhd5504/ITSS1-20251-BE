package org.itss.service.impl;

import org.itss.dto.request.auth.LoginRequest;
import org.itss.dto.request.auth.RegisterRequest;
import org.itss.dto.request.auth.TokenRefreshRequest;
import org.itss.dto.response.Result;
import org.itss.dto.response.auth.AuthUserResponse;
import org.itss.dto.response.auth.LoginResponse;
import org.itss.dto.response.auth.TokenResponse;
import org.itss.entity.User;
import org.itss.repository.UserRepository;
import org.itss.security.JwtUtil;
import org.itss.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    @Override
    public Result register(@NonNull RegisterRequest request) {

        if (userRepository.existsByEmail(Objects.requireNonNull(request.getEmail())))
            return Result.error("Email already exists");

        if (userRepository.existsByUsername(Objects.requireNonNull(request.getUsername())))
            return Result.error("Username already exists");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);

        return Result.ok("Register success");
    }

    @Override
    public Result login(@NonNull LoginRequest request, @NonNull HttpServletResponse res) {

        User user = userRepository.findByEmail(Objects.requireNonNull(request.getEmail()))
                .orElseThrow(() -> new RuntimeException("Email does not exist"));

        if (!encoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Wrong password");

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // lưu refresh token vào DB
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // set cookie access_token
        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        res.addCookie(cookie);

        AuthUserResponse authUser = AuthUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getUsername())
                .avatar(null)
                .role(null)
                .build();

        return Result.ok(new LoginResponse(authUser, accessToken, refreshToken));
    }

    @Override
    public Result refreshToken(@NonNull TokenRefreshRequest request) {

        User user = userRepository.findByRefreshToken(Objects.requireNonNull(request.getRefreshToken()))
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!jwtUtil.validateToken(request.getRefreshToken()))
            return Result.error("Refresh token expired");

        String newAccess = jwtUtil.generateAccessToken(user.getUsername());
        String newRefresh = jwtUtil.generateRefreshToken(user.getUsername());

        user.setRefreshToken(newRefresh);
        userRepository.save(user);

        return Result.ok(new TokenResponse(newAccess, newRefresh));
    }

    @Override
    public Result logout(@NonNull HttpServletResponse res) {

        Cookie cookie = new Cookie("access_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        res.addCookie(cookie);

        return Result.ok("Logged out");
    }
}
