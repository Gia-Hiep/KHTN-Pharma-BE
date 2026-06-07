package com.pharmacy.auth_service.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class UserRoleId implements Serializable {
    private Long userId;
    private Long roleId;
}
