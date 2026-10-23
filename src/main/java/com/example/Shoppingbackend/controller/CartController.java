package com.example.Shoppingbackend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Shoppingbackend.dto.CartItemRequest;
import com.example.Shoppingbackend.entity.CartItem;
import com.example.Shoppingbackend.entity.Product;
import com.example.Shoppingbackend.entity.User;
import com.example.Shoppingbackend.repository.CartItemRepository;
import com.example.Shoppingbackend.repository.ProductRepository;
import com.example.Shoppingbackend.repository.UserRepository;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartController(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    // Retrieve user's shopping cart - CUSTOMER
    @GetMapping
    public ResponseEntity<?> getCart(Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        List<CartItem> items = cartItemRepository.findByUserOrderByIdAsc(user);
        return ResponseEntity.ok(items);
    }

    // Add item / update quantity - CUSTOMER
    @PostMapping("/items")
    public ResponseEntity<?> addOrUpdateItem(
            @RequestBody CartItemRequest request,
            Principal principal) {

        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        if (request.getProductId() == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }

        int quantity = (request.getQuantity() != null) ? request.getQuantity() : 1;

        Product product = productRepository.findById(request.getProductId()).orElse(null);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }

        CartItem existingItem = cartItemRepository.findByUserAndProduct(user, product).orElse(null);

        if (quantity <= 0) {
            if (existingItem != null) {
                cartItemRepository.delete(existingItem);
            }
            return ResponseEntity.ok("Item removed from cart");
        }

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(quantity);
            CartItem updated = cartItemRepository.save(existingItem);
            return ResponseEntity.ok(updated);
        } else {
            // New cart item
            CartItem newItem = new CartItem(user, product, quantity);
            CartItem saved = cartItemRepository.save(newItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        }
    }

    // Remove item from cart - CUSTOMER
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> removeItem(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        CartItem cartItem = cartItemRepository.findById(id).orElse(null);
        if (cartItem == null) {
            return ResponseEntity.notFound().build();
        }

        if (!cartItem.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        cartItemRepository.delete(cartItem);
        return ResponseEntity.ok("Item removed from cart");
    }
}
