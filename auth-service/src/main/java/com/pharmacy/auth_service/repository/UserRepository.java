package com.pharmacy.auth_service.repository;

import com.pharmacy.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
        select u from User u
        where (:q is null or :q = '' or
              lower(u.username) like lower(concat('%', :q, '%')) or
              lower(u.fullName) like lower(concat('%', :q, '%')) or
              lower(u.phone) like lower(concat('%', :q, '%')) or
              lower(u.email) like lower(concat('%', :q, '%')))
          and (:role is null or :role = '' or exists (
                select 1 from UserRole ur
                join Role r on r.id = ur.roleId
                where ur.userId = u.id and r.code = :role
          ))
        order by u.id desc
    """)
    List<User> searchUsers(String q, String role);
}
