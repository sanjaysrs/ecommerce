package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUser(User user);

    List<Order> findByOrderStatusIdNot(int id);

    long countByOrderStatusIdNot(int id);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.orderStatus.id != :orderStatusId")
    double sumTotalPriceByOrderStatusIdNot(@Param("orderStatusId") int orderStatusId);

    long countByOrderStatusId(int id);

    long count();

    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.date) = :date AND o.orderStatus.id <> 6")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE DATE(o.date) = :date AND o.orderStatus.id <> 6")
    double sumTotalPriceByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(o) FROM Order o WHERE YEARWEEK(o.date) = YEARWEEK(CURDATE()) AND o.orderStatus.id <> 6")
    long countOrdersForThisWeek();

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE YEARWEEK(o.date) = YEARWEEK(CURRENT_DATE) AND o.orderStatus.id != 6")
    double sumTotalPriceForThisWeek();

    @Query("SELECT COUNT(o) FROM Order o WHERE MONTH(o.date) = MONTH(CURDATE()) AND YEAR(o.date) = YEAR(CURDATE()) AND o.orderStatus.id != 6")
    long countOrdersForThisMonth();

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE MONTH(o.date) = MONTH(CURDATE()) AND YEAR(o.date) = YEAR(CURDATE()) AND o.orderStatus.id <> 6")
    double sumTotalPriceForThisMonth();

    @Query("SELECT COUNT(o) FROM Order o WHERE YEAR(o.date) = YEAR(CURRENT_DATE) AND o.orderStatus.id <> 6")
    long countOrdersForThisYear();

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE YEAR(o.date) = YEAR(CURDATE()) AND o.orderStatus.id <> 6")
    double sumTotalPriceForThisYear();

//    @Query(value = "SELECT DAYNAME(o.date), COUNT(*) " +
//            "FROM customer_order o " +
//            "WHERE o.date BETWEEN CURDATE() - INTERVAL 6 DAY AND CURDATE() AND o.order_status_id <> 6 " +
//            "GROUP BY DAYNAME(o.date)", nativeQuery = true)
//    List<List<Object>> countOrdersLastSevenDays();

    @Query(value = "SELECT all_days.name, COUNT(o.id) " +
            "FROM (SELECT 'Sunday' AS name, 1 AS order_num UNION SELECT 'Monday', 2 UNION SELECT 'Tuesday', 3 " +
            "UNION SELECT 'Wednesday', 4 UNION SELECT 'Thursday', 5 UNION SELECT 'Friday', 6 " +
            "UNION SELECT 'Saturday', 7) all_days " +
            "LEFT JOIN customer_order o ON DAYOFWEEK(o.date) = order_num " +
            "AND o.date BETWEEN CURDATE() - INTERVAL 6 DAY AND CURDATE() AND o.order_status_id <> 6 " +
            "GROUP BY all_days.name, order_num " +
            "ORDER BY CASE WHEN order_num > DAYOFWEEK(CURDATE()) THEN 1 ELSE 2 END", nativeQuery = true)
    List<List<Object>> countOrdersLastSevenDays();

    @Query(value = "SELECT all_days.name, COALESCE(SUM(o.total_price), 0) " +
            "FROM (SELECT 'Sunday' AS name, 1 AS order_num UNION SELECT 'Monday', 2 UNION SELECT 'Tuesday', 3 " +
            "UNION SELECT 'Wednesday', 4 UNION SELECT 'Thursday', 5 UNION SELECT 'Friday', 6 " +
            "UNION SELECT 'Saturday', 7) all_days " +
            "LEFT JOIN customer_order o ON DAYOFWEEK(o.date) = order_num " +
            "AND o.date BETWEEN CURDATE() - INTERVAL 6 DAY AND CURDATE() AND o.order_status_id <> 6 " +
            "GROUP BY all_days.name, order_num " +
            "ORDER BY CASE WHEN order_num > DAYOFWEEK(CURDATE()) THEN 1 ELSE 2 END", nativeQuery = true)
    List<List<Object>> sumTotalPriceLastSevenDays();

}






































