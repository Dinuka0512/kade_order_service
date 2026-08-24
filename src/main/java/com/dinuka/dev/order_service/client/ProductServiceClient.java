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

    private static final String PRODUCT_SERVICE_URL = "http://localhost:8002";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductInfo getProduct(Long productId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PRODUCT_SERVICE_URL + "/products/" + productId))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                Long vendorId = json.hasNonNull("vendorId") ? json.get("vendorId").asLong() : null;
                return new ProductInfo(
                        json.get("name").asText(),
                        json.get("images").get(0).asText(),
                        json.get("price").asDouble(),
                        vendorId
                );
            }
        } catch (Exception e) {
            // Fall back to placeholder if product service is unavailable
        }

        return new ProductInfo("Product #" + productId,
                "https://picsum.photos/seed/product-" + productId + "/300/300",
                0.0,
                null);
    }

    /**
     * Resolves the storefront owned by the authenticated vendor by forwarding
     * the trusted identity headers to product_service (/vendors/me).
     */
    public VendorRef getMyVendor(String userId, String email, String userName, String role) {
        if (userId == null || !"vendor".equals(role)) {
            return null;
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(PRODUCT_SERVICE_URL + "/vendors/me"))
                .header("Accept", "application/json")
                .header("X-User-Id", userId)
                .header("X-User-Role", role);
        if (email != null) builder.header("X-User-Email", email);
        if (userName != null) builder.header("X-User-Name", userName);

        try {
            HttpResponse<String> response = httpClient.send(
                    builder.GET().build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                return new VendorRef(json.get("id").asLong(), json.get("name").asText());
            }
        } catch (Exception e) {
            // fall through
        }
        return null;
    }

    /** Counts live products for a storefront. */
    public long countProductsForVendor(Long vendorId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PRODUCT_SERVICE_URL + "/products?vendorId=" + vendorId))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                return json.isArray() ? json.size() : 0;
            }
        } catch (Exception e) {
            // fall through
        }
        return 0;
    }

    /** Counts every product on the platform (for admin overview). */
    public long countAllProducts() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PRODUCT_SERVICE_URL + "/products"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                return json.isArray() ? json.size() : 0;
            }
        } catch (Exception e) {
            // fall through
        }
        return 0;
    }

    public record ProductInfo(String name, String image, double price, Long vendorId) {}

    public record VendorRef(Long id, String name) {}
}
