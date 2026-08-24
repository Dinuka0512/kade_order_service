package com.dinuka.dev.order_service.controller;

import com.dinuka.dev.order_service.client.ProductServiceClient;
import com.dinuka.dev.order_service.dto.DashboardStatsResponse;
import com.dinuka.dev.order_service.model.Order;
import com.dinuka.dev.order_service.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productClient;

    public DashboardController(OrderRepository orderRepository, ProductServiceClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    /**
     * Stats are scoped to the caller: vendors see their own storefront
     * performance, admins see the whole platform, anonymous callers get zeros.
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (userId == null || role == null || role.isBlank()) {
            return ResponseEntity.ok(new DashboardStatsResponse(0, 0, 0, 0, 0, 0));
        }

        return switch (role) {
            case "vendor" -> ResponseEntity.ok(vendorStats(userId));
            case "admin" -> ResponseEntity.ok(platformStats());
            case "customer" -> ResponseEntity.ok(customerStats(userId));
            default -> ResponseEntity.ok(new DashboardStatsResponse(0, 0, 0, 0, 0, 0));
        };
    }

    private DashboardStatsResponse vendorStats(String userId) {
        ProductServiceClient.VendorRef vendor = productClient.getMyVendor(userId, null, null, "vendor");
        if (vendor == null) {
            return new DashboardStatsResponse(0, 0, 0, 0, 0, 0);
        }
        Long vendorId = vendor.id();

        List<Order> all = orderRepository.findAllByOrderByPlacedAtDesc();
        List<Order> mine = all.stream()
                .filter(o -> o.getItems().stream().anyMatch(i -> vendorId.equals(i.getVendorId())))
                .toList();

        double revenue = mine.stream()
                .flatMap(o -> o.getItems().stream())
                .filter(i -> vendorId.equals(i.getVendorId()))
                .mapToDouble(i -> i.getPrice() * i.getQty())
                .sum();

        long pending = mine.stream().filter(o -> "pending".equals(o.getStatus())).count();
        long customers = mine.stream().map(Order::getEmail).distinct().count();
        double avg = mine.isEmpty() ? 0 : revenue / mine.size();
        long products = productClient.countProductsForVendor(vendorId);

        return new DashboardStatsResponse(revenue, mine.size(), pending, products, customers, avg);
    }

    private DashboardStatsResponse platformStats() {
        double revenue = orderRepository.sumTotalRevenue();
        long orders = orderRepository.countAllOrders();
        long pendingOrders = orderRepository.countPendingOrders();
        double avgOrderValue = orders > 0 ? revenue / orders : 0;
        long products = productClient.countAllProducts();
        long customers = orderRepository.findAllByOrderByPlacedAtDesc().stream()
                .map(Order::getEmail)
                .distinct()
                .count();

        return new DashboardStatsResponse(revenue, orders, pendingOrders, products, customers, avgOrderValue);
    }

    private DashboardStatsResponse customerStats(String userId) {
        try {
            Long customerId = Long.parseLong(userId);
            List<Order> mine = orderRepository.findAllByOrderByPlacedAtDesc().stream()
                    .filter(o -> customerId.equals(o.getCustomerId()))
                    .toList();

            double revenue = mine.stream().mapToDouble(Order::getTotal).sum();
            long pending = mine.stream().filter(o -> "pending".equals(o.getStatus())).count();
            double avg = mine.isEmpty() ? 0 : revenue / mine.size();

            return new DashboardStatsResponse(revenue, mine.size(), pending, 0, mine.size(), avg);
        } catch (NumberFormatException e) {
            return new DashboardStatsResponse(0, 0, 0, 0, 0, 0);
        }
    }
}
