package com.dinuka.dev.order_service.controller;

import com.dinuka.dev.order_service.dto.DashboardStatsResponse;
import com.dinuka.dev.order_service.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final OrderRepository orderRepository;

    public DashboardController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        double revenue = orderRepository.sumTotalRevenue();
        long orders = orderRepository.countAllOrders();
        long pendingOrders = orderRepository.countPendingOrders();
        double avgOrderValue = orders > 0 ? revenue / orders : 0;

        return ResponseEntity.ok(new DashboardStatsResponse(
                revenue, orders, pendingOrders, 0, 0, avgOrderValue
        ));
    }
}
