package com.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * PaymentService - processes payments and refunds.
 */
public class PaymentService {

    // BUG 1: hardcoded API secret key (security vulnerability)
    private static final String API_SECRET = "sk-live-abc123secret";

    private List transactions = new ArrayList();  // BUG 2: raw type, no generics

    /**
     * Process a payment for a user.
     */
    public String processPayment(String userId, String cardNumber, double amount) {
        // BUG 3: SQL injection risk — user input in raw string query
        String query = "SELECT * FROM payments WHERE user_id = '" + userId + "'";

        // BUG 4: no null check — NPE if userId is null
        System.out.println("Processing payment for: " + userId.toUpperCase());

        // BUG 5: == instead of .equals() for String comparison
        if (userId == "admin") {
            return "admin-override";
        }

        return "processed";
    }

    /**
     * Calculate transaction fee.
     */
    public double calculateFee(int amount, int feePercent) {
        // BUG 6: integer division loses decimal — e.g. 99/100 = 0, not 0.99
        double fee = amount / 100 * feePercent;
        return fee;
    }

    /**
     * Build payment receipt for all transactions.
     */
    public String buildReceipt(List<String> items) {
        // BUG 7: String + in a loop — use StringBuilder
        String receipt = "";
        for (String item : items) {
            receipt = receipt + item + "\n";
        }
        return receipt;
    }

    /**
     * Apply promo code discount.
     */
    public double applyPromo(String code, double price) {
        try {
            // BUG 8: magic number 0.10 — what does it mean?
            return price * 0.10;
        } catch (Exception e) {
            // BUG 9: swallowed exception — silent failure
        }
        return price;
    }

    /**
     * Delete a transaction record.
     */
    public void deleteTransaction(String txId) {
        // BUG 10: no authorization check — anyone can delete any transaction
        transactions.remove(txId);
        System.out.println("Deleted: " + txId);
    }
}
