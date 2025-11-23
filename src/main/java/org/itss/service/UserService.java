package org.itss.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.itss.dto.request.LoginRequest;
import org.itss.dto.request.RegisterRequest;
import org.itss.entity.User;
import org.itss.repository.UserRepository;
import org.itss.security.JwtUtil; // Import class bạn vừa tạo
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil; // 1. Inject JwtUtil

    // Lưu ý: Tốt nhất nên cấu hình PasswordEncoder là Bean trong SecurityConfig,
    // nhưng để đơn giản mình giữ nguyên cách new trực tiếp của bạn.
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

    // 2. Thêm HttpServletResponse vào tham số để gửi Cookie về trình duyệt
    public String login(LoginRequest req, HttpServletResponse response) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElse(null);

        if (user == null)
            return "Sai username hoặc mật khẩu!";

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            return "Sai username hoặc mật khẩu!";

        // 3. Sinh token từ JwtUtil
        String token = jwtUtil.generateToken(user.getUsername());

        // 4. Tạo Cookie tên là "playfinder_token"
        Cookie cookie = new Cookie("playfinder_token", token);
        cookie.setHttpOnly(true); // Quan trọng: Chặn JS đọc cookie này (Bảo mật)
        cookie.setSecure(false); // Đặt true nếu chạy trên HTTPS
        cookie.setPath("/"); // Cookie có hiệu lực trên toàn bộ trang web
        cookie.setMaxAge(24 * 60 * 60); // Thời gian sống: 1 ngày (tính bằng giây)

        // Gắn cookie vào phản hồi
        response.addCookie(cookie);

        return "Đăng nhập thành công! Token đã được lưu trong Cookie.";
    }

    // 5. Chức năng Logout
    public String logout(HttpServletResponse response) {
        // Tạo một cookie cùng tên nhưng giá trị null
        Cookie cookie = new Cookie("playfinder_token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Đặt thời gian sống = 0 để trình duyệt xóa ngay lập tức

        response.addCookie(cookie);
        return "Đăng xuất thành công!";
    }
}