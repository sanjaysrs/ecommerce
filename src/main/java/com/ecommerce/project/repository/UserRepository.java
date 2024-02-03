package com.ecommerce.project.repository;

import com.ecommerce.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserByEmail(String email);

    List<User> findByVerifiedTrue();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.verified = true AND r.name = 'ROLE_USER'")
    List<User> findVerifiedUsersWithRoleUser();

    @Modifying
    @Query("UPDATE User u SET u.enabled = true WHERE u.id = :userId")
    void enableUser(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE User u SET u.enabled = false WHERE u.id = :userId")
    void disableUser(@Param("userId") Integer userId);

}
