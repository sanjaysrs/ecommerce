package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUser(User user);

    List<Order> findByOrderStatusIdNot(int id);

    int countByOrderStatusIdNot(int id);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.orderStatus.id != :orderStatusId")
    double sumTotalPriceByOrderStatusIdNot(@Param("orderStatusId") int orderStatusId);

    int countByOrderStatusId(int id);

    long count();
}
