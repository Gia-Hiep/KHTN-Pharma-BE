package com.pharmacy.auth_service.service;

import com.pharmacy.auth_service.dto.*;
import com.pharmacy.auth_service.entity.User;
import com.pharmacy.auth_service.repository.UserRepository;
import com.pharmacy.auth_service.repository.UserRoleRepository;
import com.pharmacy.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest req) {
        var user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!user.isActive()) {
            throw new RuntimeException("User is disabled");
        }

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        List<String> roles = userRoleRepo.findRoleCodesByUserId(user.getId());
        String token = jwtService.generateToken(user.getId(), user.getUsername(), roles);

        return new LoginResponse(token, user.getId(), roles);
    }

    /* ═══ REGISTER (BUYER self-signup) ═══ */

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (req.username() == null || req.username().isBlank()) {
            throw new RuntimeException("Username là bắt buộc");
        }
        if (req.password() == null || req.password().length() < 6) {
            throw new RuntimeException("Mật khẩu phải từ 6 ký tự trở lên");
        }
        if (userRepo.existsByUsername(req.username())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        User u = new User();
        u.setUsername(req.username());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setFullName(req.fullName());
        u.setPhone(req.phone());
        u.setEmail(req.email());
        u.setAddress(req.address());
        u.setActive(true);

        User saved = userRepo.save(u);

        // Gán role BUYER mặc định
        userRoleRepo.replaceUserRoles(saved.getId(), List.of("BUYER"));

        List<String> roles = List.of("BUYER");
        String token = jwtService.generateToken(saved.getId(), saved.getUsername(), roles);

        return new LoginResponse(token, saved.getId(), roles);
    }

    /* ═══ PROFILE (self-service) ═══ */

    @Transactional(readOnly = true)
    public UserDetailResponse getProfile(Long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tìm thấy"));
        List<String> roles = userRoleRepo.findRoleCodesByUserId(u.getId());
        return new UserDetailResponse(
                u.getId(), u.getUsername(), u.getFullName(),
                u.getPhone(), u.getEmail(), u.getAddress(),
                u.isActive(), roles
        );
    }

    @Transactional
    public UserDetailResponse updateProfile(Long userId, UpdateUserRequest req) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tìm thấy"));
        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.phone() != null) u.setPhone(req.phone());
        if (req.email() != null) u.setEmail(req.email());
        if (req.address() != null) u.setAddress(req.address());
        userRepo.save(u);
        return getProfile(userId);
    }

    @Transactional
    public void changeMyPassword(Long userId, ChangePasswordRequest req) {
        if (req.newPassword() == null || req.newPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải từ 6 ký tự trở lên");
        }
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tìm thấy"));
        u.setPasswordHash(encoder.encode(req.newPassword()));
        userRepo.save(u);
    }
}
