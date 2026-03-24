package com.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * OrderService - handles order processing logic.
 * Intentionally contains bugs for AI code review demo.
 */
public class OrderService {

    private static final String DB_PASSWORD = "admin123";  // hardcoded credential

    // Bug: using raw List without generics properly
    private List orders = new ArrayList();

    /**
     * Find order by customer email using raw SQL (SQL injection risk).
     */
    public String findOrderByEmail(String email) {
        // Bug: SQL injection - user input concatenated directly
        String query = "SELECT * FROM orders WHERE email = '" + email + "'";
        System.out.println("Running query: " + query);
        return query;
    }

    /**
     * Calculate discount for an order.
     */
    public double calculateDiscount(int totalPrice, int discountPercent) {
        // Bug: integer division loses decimal precision
        double discount = totalPrice / 100 * discountPercent;
        return discount;
    }

    /**
     * Check if two order IDs match.
     */
    public boolean ordersMatch(String id1, String id2) {
        // Bug: == instead of .equals() for String comparison
        return id1 == id2;
    }

    /**
     * Get order status with no null check.
     */
    public String getOrderStatus(String orderId) {
        // Bug: no null check — NPE if orderId is null
        return "Status for: " + orderId.toUpperCase();
    }

    /**
     * Build a large order summary (performance issue).
     */
    public String buildOrderSummary(List<String> items) {
        // Bug: String concatenation in a loop — should use StringBuilder
        String summary = "";
        for (String item : items) {
            summary = summary + item + ", ";
        }
        return summary;
    }

    /**
     * Apply coupon code — swallows exception silently.
     */
    public double applyCoupon(String couponCode, double price) {
        try {
            // Magic number — what does 0.15 mean?
            double discounted = price * 0.15;
            return discounted;
        } catch (Exception e) {
            // Bug: swallowed exception, no logging
        }
        return price;
    }

    /**
     * Delete order — no authorization check.
     */
    public void deleteOrder(String orderId) {
        // Bug: no authorization — any caller can delete any order
        orders.remove(orderId);
        System.out.println("Deleted order: " + orderId);
    }
}
