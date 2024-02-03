package com.ecommerce.project.repository;

import com.ecommerce.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserByEmail(String email);

    List<User> findByVerifiedTrue();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.verified = true AND r.name = 'ROLE_USER'")
    List<User> findVerifiedUsersWithRoleUser();

}
