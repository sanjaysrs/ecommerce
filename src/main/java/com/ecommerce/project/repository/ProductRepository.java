package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByCategory_Id(int id);

    boolean existsByName(String name);

    List<Product> findAllByNameContaining(String keyword);

    List<Product> findAllByNameContainingAndCategory_Id(String keyword, int categoryId);
}
