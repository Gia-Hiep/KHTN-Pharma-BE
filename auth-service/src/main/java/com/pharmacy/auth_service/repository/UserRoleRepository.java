package com.pharmacy.auth_service.repository;

import com.pharmacy.auth_service.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query("""
        select r.code from Role r
        join UserRole ur on ur.roleId = r.id
        where ur.userId = :userId
    """)
    List<String> findRoleCodesByUserId(Long userId);

    @Modifying
    @Query(value = "delete from user_roles where user_id = :userId", nativeQuery = true)
    void deleteAllByUserIdNative(Long userId);

    @Modifying
    @Query(value = """
        insert into user_roles(user_id, role_id)
        select :userId, r.id from roles r where r.code = :code
    """, nativeQuery = true)
    void addRoleByCode(Long userId, String code);

    default void replaceUserRoles(Long userId, List<String> roleCodes){
        deleteAllByUserIdNative(userId);
        if (roleCodes == null) return;
        for (String c : roleCodes){
            addRoleByCode(userId, c);
        }
    }
}
