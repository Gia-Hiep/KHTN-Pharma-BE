package com.pharmacy.auth_service.service;

import com.pharmacy.auth_service.dto.*;
import com.pharmacy.auth_service.entity.User;
import com.pharmacy.auth_service.repository.RoleRepository;
import com.pharmacy.auth_service.repository.UserRepository;
import com.pharmacy.auth_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserRoleRepository userRoleRepo;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepo.findAll().stream()
                .map(r -> new RoleResponse(r.getId(), r.getCode(), r.getCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserListRow> listUsers(String q, String role) {
        return userRepo.searchUsers(q, role).stream()
                .map(u -> new UserListRow(
                        u.getId(),
                        u.getUsername(),
                        u.getFullName(),
                        u.getPhone(),
                        u.getEmail(),
                        u.getAddress(),
                        u.isActive(),
                        userRoleRepo.findRoleCodesByUserId(u.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUser(Long id) {
        User u = userRepo.findById(id).orElseThrow();

        return new UserDetailResponse(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getPhone(),
                u.getEmail(),
                u.getAddress(),
                u.isActive(),
                userRoleRepo.findRoleCodesByUserId(u.getId())
        );
    }

    @Transactional
    public UserDetailResponse createUser(CreateUserRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new RuntimeException("Username already exists");
        }
        if (req.password() == null || req.password().isBlank()) {
            throw new RuntimeException("password required");
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

        if (req.roles() != null && !req.roles().isEmpty()) {
            userRoleRepo.replaceUserRoles(saved.getId(), req.roles());
        }

        return getUser(saved.getId());
    }

    @Transactional
    public UserDetailResponse updateUser(Long id, UpdateUserRequest req) {
        User u = userRepo.findById(id).orElseThrow();

        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.phone() != null) u.setPhone(req.phone());
        if (req.email() != null) u.setEmail(req.email());
        if(req.address() != null) u.setAddress(req.address());

        userRepo.save(u);
        return getUser(id);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest req) {
        if (req.newPassword() == null || req.newPassword().isBlank()) {
            throw new RuntimeException("newPassword required");
        }

        User u = userRepo.findById(id).orElseThrow();
        u.setPasswordHash(encoder.encode(req.newPassword()));
        userRepo.save(u);
    }

    @Transactional
    public UserRolesResponse setRoles(Long id, SetRolesRequest req) {
        userRepo.findById(id).orElseThrow();
        userRoleRepo.replaceUserRoles(id, req.roles());
        return new UserRolesResponse(id, userRoleRepo.findRoleCodesByUserId(id));
    }

    @Transactional
    public void setStatus(Long id, SetUserStatusRequest req) {
        User u = userRepo.findById(id).orElseThrow();
        u.setActive(req.active());
        userRepo.save(u);
    }
}
