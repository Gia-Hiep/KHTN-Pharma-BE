package com.pharmacy.auth_service.controller;

import com.pharmacy.auth_service.dto.*;
import com.pharmacy.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse res = authService.login(req);
        return ApiResponse.ok("Đăng nhập thành công", res);
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@RequestBody RegisterRequest req) {
        LoginResponse res = authService.register(req);
        return ApiResponse.ok("Đăng ký thành công", res);
    }

    /* ═══ SELF-PROFILE (authenticated) ═══ */

    @GetMapping("/me")
    public ApiResponse<UserDetailResponse> getProfile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok("OK", authService.getProfile(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserDetailResponse> updateProfile(@RequestBody UpdateUserRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok("Cập nhật thông tin thành công", authService.updateProfile(userId, req));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changeMyPassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        authService.changeMyPassword(userId, req);
        return ApiResponse.ok("Đổi mật khẩu thành công");
    }
}
