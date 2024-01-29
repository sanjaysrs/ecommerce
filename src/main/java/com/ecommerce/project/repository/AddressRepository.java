package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Address;
import com.ecommerce.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUser(User user);
    List<Address> findByUserAndDeletedFalse(User user);
    Optional<Address> findByIdAndDeletedFalse(Long id);
}