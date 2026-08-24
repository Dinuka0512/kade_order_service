package com.dinuka.dev.order_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "product_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;

    /** Storefront this line item was bought from (used for vendor scoping). */
    @Column(name = "vendor_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long vendorId;

    private String name;
    private String image;
    private double price;
    private int qty;

    public OrderItem() {}

    public OrderItem(Long productId, Long vendorId, String name, String image, double price, int qty) {
        this.productId = productId;
        this.vendorId = vendorId;
        this.name = name;
        this.image = image;
        this.price = price;
        this.qty = qty;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}
