package com.example.Shoppingbackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Shoppingbackend.entity.Product;
import com.example.Shoppingbackend.repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Get all products / filter - PUBLIC
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        if (category != null && !category.isBlank() && search != null && !search.isBlank()) {
            return ResponseEntity.ok(
                productRepository.findByCategoryIgnoreCaseAndNameContainingIgnoreCase(category.trim(), search.trim())
            );
        } else if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(
                productRepository.findByCategoryIgnoreCase(category.trim())
            );
        } else if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(
                productRepository.findByNameContainingIgnoreCase(search.trim())
            );
        }

        return ResponseEntity.ok(productRepository.findAll());
    }

    // Get specific product details - PUBLIC
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create new product entry - ADMIN
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Product name is required");
        }
        if (product.getPrice() == null) {
            return ResponseEntity.badRequest().body("Product price is required");
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // Update product details/stock - ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        return productRepository.findById(id)
                .map(existingProduct -> {
                    if (product.getName() != null) existingProduct.setName(product.getName());
                    if (product.getDescription() != null) existingProduct.setDescription(product.getDescription());
                    if (product.getPrice() != null) existingProduct.setPrice(product.getPrice());
                    if (product.getStockQuantity() != null) existingProduct.setStockQuantity(product.getStockQuantity());
                    if (product.getImageUrl() != null) existingProduct.setImageUrl(product.getImageUrl());
                    if (product.getCategory() != null) existingProduct.setCategory(product.getCategory());

                    Product updated = productRepository.save(existingProduct);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Remove product from store - ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        productRepository.deleteById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}
