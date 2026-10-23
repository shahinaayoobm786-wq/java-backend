package com.example.Shoppingbackend.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.example.Shoppingbackend.dto.CheckoutRequest;
import com.example.Shoppingbackend.entity.*;
import com.example.Shoppingbackend.repository.CartItemRepository;
import com.example.Shoppingbackend.repository.OrderRepository;
import com.example.Shoppingbackend.repository.ProductRepository;
import com.example.Shoppingbackend.repository.UserRepository;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
public class OrderController {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderController(
            OrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
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

    // Submit order and clear cart - CUSTOMER
    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(
            @RequestBody(required = false) CheckoutRequest request,
            Principal principal) {

        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        List<CartItem> cartItems = cartItemRepository.findByUserOrderByIdAsc(user);
        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Your shopping cart is empty");
        }

        // Validate stock availability for all items before placing order
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                return ResponseEntity.badRequest().body(
                    "Insufficient stock for " + product.getName() + ". Available: " + product.getStockQuantity()
                );
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal itemTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        String shippingAddress = (request != null && request.getFormattedAddress() != null && !request.getFormattedAddress().isBlank())
                ? request.getFormattedAddress()
                : (request != null && request.getAddress() != null) ? request.getAddress() : "Standard Delivery";

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PLACED);
        order.setShippingAddress(shippingAddress);
        order.setCreatedAt(LocalDateTime.now());

        // Deduct stock and create order items
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(
                order,
                product,
                item.getQuantity(),
                product.getPrice()
            );
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear user's cart
        cartItemRepository.deleteByUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    // Get logged-in user's order history - CUSTOMER
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(orders);
    }

    // Get all orders - ADMIN
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(orders);
    }

    // Update order status - ADMIN
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return ResponseEntity.ok(updated);
    }
}
