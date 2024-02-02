package com.ecommerce.project.repository;

import com.ecommerce.project.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT c.name, COALESCE(SUM(oi.quantity), 0) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN p.category c " +
            "JOIN oi.order o " +
            "WHERE o.orderStatus.id <> 6 " +
            "GROUP BY c.id")
    List<List<Object>> productsSoldByCategory();

    @Query("SELECT c.name, COALESCE(SUM(oi.quantity * p.price), 0) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN p.category c " +
            "JOIN oi.order o " +
            "WHERE o.orderStatus.id <> 6 " +
            "GROUP BY c.id")
    List<List<Object>> revenueByCategory();

}
