package com.dinuka.dev.order_service.repository;

import com.dinuka.dev.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByPlacedAtDesc();

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o")
    double sumTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o")
    long countAllOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'pending'")
    long countPendingOrders();
}
