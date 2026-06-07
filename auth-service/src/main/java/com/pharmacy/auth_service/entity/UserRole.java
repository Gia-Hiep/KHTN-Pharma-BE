package com.pharmacy.auth_service.entity;

import com.pharmacy.auth_service.entity.UserRoleId;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_roles")
@Data
@IdClass(UserRoleId.class)
public class UserRole {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "role_id")
    private Long roleId;
}
