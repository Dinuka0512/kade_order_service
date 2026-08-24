package com.dinuka.dev.order_service.service;

import com.dinuka.dev.order_service.client.ProductServiceClient;
import com.dinuka.dev.order_service.dto.PlaceOrderRequest;
import com.dinuka.dev.order_service.model.Order;
import com.dinuka.dev.order_service.model.OrderItem;
import com.dinuka.dev.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productClient;

    public OrderService(OrderRepository orderRepository, ProductServiceClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    /**
     * Lists orders visible to the caller:
     * - vendors see only orders containing items sold on their storefront
     * - customers see only their own orders
     * - admins see everything
     * - anonymous callers see nothing
     */
    public List<Order> findVisibleOrders(String userId, String role) {
        if (userId == null || role == null || role.isBlank()) {
            return List.of();
        }
        List<Order> all = orderRepository.findAllByOrderByPlacedAtDesc();

        return switch (role) {
            case "admin" -> all;
            case "customer" -> {
                Long customerId = parseId(userId);
                yield all.stream()
                        .filter(o -> customerId != null && customerId.equals(o.getCustomerId()))
                        .toList();
            }
            case "vendor" -> {
                Long vendorId = currentVendorId(userId, role);
                yield all.stream()
                        .filter(o -> o.getItems().stream()
                                .anyMatch(i -> vendorId.equals(i.getVendorId())))
                        .map(o -> scopedToVendor(o, vendorId))
                        .toList();
            }
            default -> List.of();
        };
    }

    /**
     * Fetches a single order, enforcing the same visibility rules as listing.
     */
    public Order findVisibleOrder(Long id, String userId, String role) {
        Order order = findById(id);

        if (userId == null || role == null || role.isBlank()) {
            throw new RuntimeException("Sign in to view this order");
        }

        switch (role) {
            case "admin" -> { /* platform admins can view any order */ }
            case "customer" -> {
                Long customerId = parseId(userId);
                if (!customerId.equals(order.getCustomerId())) {
                    throw new RuntimeException("You do not have permission to view this order");
                }
            }
            case "vendor" -> {
                Long vendorId = currentVendorId(userId, role);
                boolean sellsHere = order.getItems().stream()
                        .anyMatch(i -> vendorId.equals(i.getVendorId()));
                if (!sellsHere) {
                    throw new RuntimeException("You do not have permission to view this order");
                }
                return scopedToVendor(order, vendorId);
            }
            default -> throw new RuntimeException("You do not have permission to view this order");
        }

        return order;
    }

    /**
     * Status changes are for sellers/admins only; vendors may only update
     * orders that contain items from their own storefront.
     */
    public Order updateStatus(Long id, String status, String userId, String role) {
        if (userId == null || role == null || role.isBlank()) {
            throw new RuntimeException("Sign in to update orders");
        }

        switch (role) {
            case "admin" -> { /* allowed */ }
            case "vendor" -> {
                Long vendorId = currentVendorId(userId, role);
                Order order = findById(id);
                boolean sellsHere = order.getItems().stream()
                        .anyMatch(i -> vendorId.equals(i.getVendorId()));
                if (!sellsHere) {
                    throw new RuntimeException("You can only update orders on your own storefront");
                }
            }
            default -> throw new RuntimeException("You do not have permission to update orders");
        }

        Order order = findById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order create(PlaceOrderRequest input, String userId) {
        Order order = new Order();
        order.setOrderNumber("KD-" + (10000 + ThreadLocalRandom.current().nextInt(89999)));
        order.setCustomerName(input.getName());
        order.setEmail(input.getEmail());
        Long customerId = parseId(userId); // null for guest checkouts
        order.setCustomerId(customerId);
        order.setAddressLine1(input.getAddress().getLine1());
        order.setAddressCity(input.getAddress().getCity());
        order.setAddressZip(input.getAddress().getZip());
        order.setStatus("pending");

        double subtotal = 0;
        List<OrderItem> items = new ArrayList<>();
        for (PlaceOrderRequest.OrderItemInput itemInput : input.getItems()) {
            Long productId = Long.parseLong(itemInput.getProductId());
            ProductServiceClient.ProductInfo productInfo = productClient.getProduct(productId);

            OrderItem item = new OrderItem(productId, productInfo.vendorId(), productInfo.name(),
                    productInfo.image(), productInfo.price(), itemInput.getQty());
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

    /**
     * Returns a copy of the order trimmed to the given vendor's line items so
     * other sellers' products and buyer totals stay private between vendors.
     */
    private Order scopedToVendor(Order source, Long vendorId) {
        Order copy = new Order();
        copy.setId(source.getId());
        copy.setOrderNumber(source.getOrderNumber());
        copy.setCustomerId(source.getCustomerId());
        copy.setCustomerName(source.getCustomerName());
        copy.setEmail(source.getEmail());
        copy.setSubtotal(source.getSubtotal());
        copy.setShippingFee(source.getShippingFee());
        copy.setTotal(source.getTotal());
        copy.setStatus(source.getStatus());
        copy.setPlacedAt(source.getPlacedAt());
        copy.setAddressLine1(source.getAddressLine1());
        copy.setAddressCity(source.getAddressCity());
        copy.setAddressZip(source.getAddressZip());

        List<OrderItem> mine = source.getItems().stream()
                .filter(i -> Objects.equals(vendorId, i.getVendorId()))
                .toList();
        copy.setItems(new ArrayList<>(mine));
        return copy;
    }

    private Long currentVendorId(String userId, String role) {
        ProductServiceClient.VendorRef vendor = productClient.getMyVendor(userId, null, null, role);
        if (vendor == null) {
            throw new RuntimeException("Vendor account required");
        }
        return vendor.id();
    }

    private Long parseId(String value) {
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
