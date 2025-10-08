package com.chs.webapp.controller;

import com.chs.webapp.dto.ProductCreateRequest;
import com.chs.webapp.dto.ProductResponse;
import com.chs.webapp.dto.ProductUpdateRequest;
import com.chs.webapp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateRequest request, Authentication authentication) {
        log.info("Creating product with SKU: {}", request.getSku());

        String authenticatedEmail = authentication.getName();
        ProductResponse productResponse = productService.createProduct(request, authenticatedEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable UUID id) {
        log.info("Getting product info for ID: {}", id);

        ProductResponse productResponse = productService.getProductById(id);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        log.info("Getting all products");

        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserProducts(Authentication authentication) {
        log.info("Getting products for user");

        String authenticatedEmail = authentication.getName();
        List<ProductResponse> products = productService.getProductsByUser(authenticatedEmail);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request, Authentication authentication) {
        log.info("Updating product with ID: {}", id);

        String authenticatedEmail = authentication.getName();
        ProductResponse productResponse = productService.updateProduct(id, request, authenticatedEmail);
        return ResponseEntity.ok(productResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID id, Authentication authentication) {
        log.info("Deleting product with ID: {}", id);

        String authenticatedEmail = authentication.getName();
        productService.deleteProduct(id, authenticatedEmail);
        return ResponseEntity.noContent().build();
    }
}
