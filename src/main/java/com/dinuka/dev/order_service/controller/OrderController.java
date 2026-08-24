package com.dinuka.dev.order_service.controller;

import com.dinuka.dev.order_service.dto.OrderStatusInput;
import com.dinuka.dev.order_service.model.Order;
import com.dinuka.dev.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(orderService.findVisibleOrders(userId, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(orderService.findVisibleOrder(id, userId, role));
    }

    @PostMapping
    public ResponseEntity<Order> create(
            @Valid @RequestBody com.dinuka.dev.order_service.dto.PlaceOrderRequest input,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(orderService.create(input, userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusInput input,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(orderService.updateStatus(id, input.getStatus(), userId, role));
    }
}
