package com.pharmacy.auth_service.controller;

import com.pharmacy.auth_service.dto.*;
import com.pharmacy.auth_service.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService service;

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> listRoles(){
        return ApiResponse.ok("Lấy danh sách role thành công", service.listRoles());
    }

    @GetMapping("/users")
    public ApiResponse<List<UserListRow>> listUsers(@RequestParam(required = false) String q,
                                                    @RequestParam(required = false) String role){
        return ApiResponse.ok("Lấy danh sách user thành công", service.listUsers(q, role));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserDetailResponse> getUser(@PathVariable Long id){
        return ApiResponse.ok("Lấy chi tiết user thành công", service.getUser(id));
    }

    @PostMapping("/users")
    public ApiResponse<UserDetailResponse> create(@RequestBody CreateUserRequest req){
        return ApiResponse.ok("Tạo nhân viên thành công", service.createUser(req));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<UserDetailResponse> update(@PathVariable Long id, @RequestBody UpdateUserRequest req){
        return ApiResponse.ok("Cập nhật nhân viên thành công", service.updateUser(id, req));
    }

    @PutMapping("/users/{id}/password")
    public ApiResponse<Void> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest req){
        service.changePassword(id, req);
        return ApiResponse.ok("Đổi mật khẩu thành công");
    }

    @PutMapping("/users/{id}/roles")
    public ApiResponse<UserRolesResponse> setRoles(@PathVariable Long id, @RequestBody SetRolesRequest req){
        return ApiResponse.ok("Cập nhật role thành công", service.setRoles(id, req));
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> setStatus(@PathVariable Long id, @RequestBody SetUserStatusRequest req){
        service.setStatus(id, req);
        return ApiResponse.ok(req.active() ? "Kích hoạt tài khoản thành công" : "Vô hiệu hoá tài khoản thành công");
    }
}
