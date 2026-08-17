package com.dinuka.dev.order_service.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class PlaceOrderRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotNull(message = "Address is required")
    private AddressInput address;

    @NotEmpty(message = "Items are required")
    private List<OrderItemInput> items;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public AddressInput getAddress() { return address; }
    public void setAddress(AddressInput address) { this.address = address; }

    public List<OrderItemInput> getItems() { return items; }
    public void setItems(List<OrderItemInput> items) { this.items = items; }

    public static class AddressInput {
        @NotBlank
        private String line1;
        @NotBlank
        private String city;
        @NotBlank
        private String zip;

        public String getLine1() { return line1; }
        public void setLine1(String line1) { this.line1 = line1; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }
    }

    public static class OrderItemInput {
        @NotBlank
        private String productId;
        @NotNull @Min(1)
        private Integer qty;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }
    }
}
