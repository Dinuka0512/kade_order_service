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
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody com.dinuka.dev.order_service.dto.PlaceOrderRequest input) {
        return ResponseEntity.ok(orderService.create(input));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusInput input) {
        return ResponseEntity.ok(orderService.updateStatus(id, input.getStatus()));
    }
}
