package com.dinuka.dev.order_service.dto;

import jakarta.validation.constraints.Pattern;

public class OrderStatusInput {

    @Pattern(regexp = "pending|confirmed|shipped|delivered|cancelled", message = "Invalid status")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
