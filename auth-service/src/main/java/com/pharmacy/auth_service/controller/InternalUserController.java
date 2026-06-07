package com.pharmacy.auth_service.controller;

import com.pharmacy.auth_service.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal endpoint — any authenticated service/user can look up basic user info.
 * Used by sales-service to backfill buyerName for old orders.
 */
@RestController
@RequestMapping("/auth/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserAdminService service;

    /** Simple name lookup — returns just {id, fullName} */
    @GetMapping("/users/{id}/name")
    public Map<String, Object> getUserName(@PathVariable Long id) {
        var user = service.getUser(id);
        return Map.of("id", user.id(), "fullName", user.fullName() != null ? user.fullName() : "");
    }
}
