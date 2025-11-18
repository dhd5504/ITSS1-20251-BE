package org.itss.service;

import lombok.RequiredArgsConstructor;
import org.itss.dto.request.LoginRequest;
import org.itss.dto.request.RegisterRequest;
import org.itss.entity.User;
import org.itss.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(RegisterRequest req) {

        if (userRepository.existsByUsername(req.getUsername()))
            return "Username đã tồn tại!";

        if (userRepository.existsByEmail(req.getEmail()))
            return "Email đã tồn tại!";

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user);
        return "Đăng ký thành công!";
    }

    public String login(LoginRequest req) {

        User user = userRepository.findByUsername(req.getUsername())
                .orElse(null);

        if (user == null)
            return "Sai username hoặc mật khẩu!";

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            return "Sai username hoặc mật khẩu!";

        return "Đăng nhập thành công!";
    }
}
