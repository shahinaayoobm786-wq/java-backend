package com.example.Shoppingbackend.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.Shoppingbackend.entity.Product;
import com.example.Shoppingbackend.entity.Role;
import com.example.Shoppingbackend.entity.User;
import com.example.Shoppingbackend.repository.ProductRepository;
import com.example.Shoppingbackend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@shopping.com")) {
            User admin = new User(
                "admin",
                "admin@shopping.com",
                passwordEncoder.encode("admin123"),
                Role.ROLE_ADMIN
            );
            userRepository.save(admin);
            System.out.println("Default Admin created: admin@shopping.com / admin123");
        }

        if (!userRepository.existsByEmail("customer@shopping.com")) {
            User customer = new User(
                "customer",
                "customer@shopping.com",
                passwordEncoder.encode("customer123"),
                Role.ROLE_CUSTOMER
            );
            userRepository.save(customer);
            System.out.println("Default Customer created: customer@shopping.com / customer123");
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            List<Product> defaultProducts = List.of(
                new Product(
                    "iPhone 15",
                    "Apple iPhone 15 with advanced dual-camera system, Dynamic Island, and A16 Bionic chip.",
                    new BigDecimal("65000.00"),
                    10,
                    "https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=600&auto=format&fit=crop&q=80",
                    "Mobile"
                ),
                new Product(
                    "Samsung Galaxy S24",
                    "Samsung Galaxy S24 with powerful AI features, bright AMOLED display, and pro-grade camera.",
                    new BigDecimal("70000.00"),
                    8,
                    "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=600&auto=format&fit=crop&q=80",
                    "Mobile"
                ),
                new Product(
                    "HP Laptop 15s",
                    "HP 15.6-inch laptop with Intel Core i5 processor, 16GB RAM, 512GB SSD, fast performance.",
                    new BigDecimal("55000.00"),
                    5,
                    "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600&auto=format&fit=crop&q=80",
                    "Laptop"
                ),
                new Product(
                    "Sony WH-1000XM4 Headphones",
                    "Industry-leading noise cancelling wireless headphones with premium sound and 30-hour battery life.",
                    new BigDecimal("8000.00"),
                    15,
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
                    "Audio"
                ),
                new Product(
                    "Apple Watch SE",
                    "Smart watch with health metrics, workout tracking, sleep stages, and water resistance.",
                    new BigDecimal("12000.00"),
                    12,
                    "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80",
                    "Accessories"
                ),
                new Product(
                    "Logitech MX Master 3S Wireless Mouse",
                    "Ergonomic wireless mouse with 8K DPI sensor, ultra-quiet clicks, and hyper-fast scroll wheel.",
                    new BigDecimal("1500.00"),
                    20,
                    "https://images.unsplash.com/photo-1527814050087-3793815479db?w=600&auto=format&fit=crop&q=80",
                    "Accessories"
                )
            );

            productRepository.saveAll(defaultProducts);
            System.out.println("Default products seeded successfully.");
        }
    }
}
