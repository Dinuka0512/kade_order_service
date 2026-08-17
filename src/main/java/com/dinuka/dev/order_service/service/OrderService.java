package com.dinuka.dev.order_service.service;

import com.dinuka.dev.order_service.client.ProductServiceClient;
import com.dinuka.dev.order_service.dto.PlaceOrderRequest;
import com.dinuka.dev.order_service.model.Order;
import com.dinuka.dev.order_service.model.OrderItem;
import com.dinuka.dev.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productClient;

    public OrderService(OrderRepository orderRepository, ProductServiceClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    public List<Order> findAll() {
        return orderRepository.findAllByOrderByPlacedAtDesc();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order create(PlaceOrderRequest input) {
        Order order = new Order();
        order.setOrderNumber("KD-" + (10000 + ThreadLocalRandom.current().nextInt(89999)));
        order.setCustomerName(input.getName());
        order.setEmail(input.getEmail());
        order.setCustomerId(1L);
        order.setAddressLine1(input.getAddress().getLine1());
        order.setAddressCity(input.getAddress().getCity());
        order.setAddressZip(input.getAddress().getZip());
        order.setStatus("pending");

        double subtotal = 0;
        List<OrderItem> items = new ArrayList<>();
        for (PlaceOrderRequest.OrderItemInput itemInput : input.getItems()) {
            Long productId = Long.parseLong(itemInput.getProductId());
            ProductServiceClient.ProductInfo productInfo = productClient.getProduct(productId);

            OrderItem item = new OrderItem(productId, productInfo.name(), productInfo.image(), productInfo.price(), itemInput.getQty());
            item.setOrder(order);
            items.add(item);
            subtotal += productInfo.price() * itemInput.getQty();
        }

        order.setItems(items);

        double shippingFee = subtotal >= 50000 || subtotal == 0 ? 0 : 600;
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotal(subtotal + shippingFee);

        return orderRepository.save(order);
    }

    public Order updateStatus(Long id, String status) {
        Order order = findById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
