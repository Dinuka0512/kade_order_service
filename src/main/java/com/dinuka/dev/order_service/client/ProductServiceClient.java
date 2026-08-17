package com.dinuka.dev.order_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ProductServiceClient {

    private static final String PRODUCT_SERVICE_URL = "http://localhost:8002/products";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductInfo getProduct(Long productId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PRODUCT_SERVICE_URL + "/" + productId))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                return new ProductInfo(
                        json.get("name").asText(),
                        json.get("images").get(0).asText(),
                        json.get("price").asDouble()
                );
            }
        } catch (Exception e) {
            // Fall back to placeholder if product service is unavailable
        }

        return new ProductInfo("Product #" + productId,
                "https://picsum.photos/seed/product-" + productId + "/300/300",
                0.0);
    }

    public record ProductInfo(String name, String image, double price) {}
}
