package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    @Query("SELECT w.amount, u.email FROM Wallet w JOIN w.user u WHERE EXISTS (SELECT 1 FROM u.roles r WHERE r.name = 'ROLE_USER')")
    List<List<Object>> findAmountAndUserEmail();

}
