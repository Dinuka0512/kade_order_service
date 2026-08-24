package com.dinuka.dev.order_service.config;

import com.dinuka.dev.order_service.model.Order;
import com.dinuka.dev.order_service.model.OrderItem;
import com.dinuka.dev.order_service.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final OrderRepository orderRepository;

    public DataInitializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) return;

        String img = "https://picsum.photos/seed/%s/300/300";

        Order order1 = new Order();
        order1.setOrderNumber("KD-10524");
        order1.setCustomerId(1L);
        order1.setCustomerName("Sanduni Perera");
        order1.setEmail("sanduni@example.com");
        order1.setStatus("delivered");
        order1.setPlacedAt(LocalDateTime.now().minusDays(12));
        order1.setAddressLine1("24 Park Road");
        order1.setAddressCity("Colombo 05");
        order1.setAddressZip("00500");
        order1.setSubtotal(76900);
        order1.setShippingFee(600);
        order1.setTotal(77500);

        OrderItem item1a = new OrderItem(1L, 1L, "Aurora Wireless Headphones", img.formatted("aurora-headphones"), 68500, 1);
        OrderItem item1b = new OrderItem(10L, 4L, "Organic Ceylon Tea Sampler", img.formatted("tea-sampler"), 4200, 2);
        item1a.setOrder(order1);
        item1b.setOrder(order1);
        order1.getItems().add(item1a);
        order1.getItems().add(item1b);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setOrderNumber("KD-10542");
        order2.setCustomerId(1L);
        order2.setCustomerName("Sanduni Perera");
        order2.setEmail("sanduni@example.com");
        order2.setStatus("shipped");
        order2.setPlacedAt(LocalDateTime.now().minusDays(3));
        order2.setAddressLine1("24 Park Road");
        order2.setAddressCity("Colombo 05");
        order2.setAddressZip("00500");
        order2.setSubtotal(12100);
        order2.setShippingFee(400);
        order2.setTotal(12500);

        OrderItem item2a = new OrderItem(4L, 2L, "Linen Resort Shirt", img.formatted("linen-shirt"), 8900, 1);
        OrderItem item2b = new OrderItem(6L, 2L, "Canvas Tote - Coconut Grove", img.formatted("canvas-tote"), 3200, 1);
        item2a.setOrder(order2);
        item2b.setOrder(order2);
        order2.getItems().add(item2a);
        order2.getItems().add(item2b);
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setOrderNumber("KD-10558");
        order3.setCustomerId(1L);
        order3.setCustomerName("Sanduni Perera");
        order3.setEmail("sanduni@example.com");
        order3.setStatus("pending");
        order3.setPlacedAt(LocalDateTime.now().minusDays(1));
        order3.setAddressLine1("24 Park Road");
        order3.setAddressCity("Colombo 05");
        order3.setAddressZip("00500");
        order3.setSubtotal(45000);
        order3.setShippingFee(600);
        order3.setTotal(45600);

        OrderItem item3a = new OrderItem(3L, 1L, "Nimbus Smart Watch", img.formatted("smart-watch"), 45000, 1);
        item3a.setOrder(order3);
        order3.getItems().add(item3a);
        orderRepository.save(order3);
    }
}
